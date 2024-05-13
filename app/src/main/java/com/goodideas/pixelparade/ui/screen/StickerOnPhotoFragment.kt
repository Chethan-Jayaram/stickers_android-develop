package com.goodideas.pixelparade.ui.screen

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.goodideas.pixelparade.R
import com.goodideas.pixelparade.SharedPreferencesHelper
import com.goodideas.pixelparade.Utils
import com.goodideas.pixelparade.data.ApiClient
import com.goodideas.pixelparade.data.analityc.AnalyticHelper
import com.goodideas.pixelparade.data.analityc.event.GoHome
import com.goodideas.pixelparade.data.analityc.event.PackOpened
import com.goodideas.pixelparade.data.analityc.event.PhotoShare
import com.goodideas.pixelparade.data.analityc.event.StickerToPhoto
import com.goodideas.pixelparade.ext.toast
import com.goodideas.pixelparade.html.GetBitmapFromUrl
import com.goodideas.pixelparade.ui.MainActivity
import com.goodideas.pixelparade.ui.PhotoProvider
import com.goodideas.pixelparade.ui.adapters.StickersAdapter
import com.goodideas.pixelparade.ui.adapters.StickersOnPhotoAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import ja.burhanrashid52.photoeditor.OnSaveBitmap
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.SaveSettings
import kotlinx.android.synthetic.main.fragment_sticker_on_photo.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class StickerOnPhotoFragment : Fragment(), StickersAdapter.OnStickerSelectedListener {

    companion object {
        const val IMAGE_URI = "StickerOnPhoto.ImageUri"

        fun newInstance(imageUri: Uri): StickerOnPhotoFragment {
            return StickerOnPhotoFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(IMAGE_URI, imageUri)
                }
            }
        }
    }

    private var stickersAdapter: StickersOnPhotoAdapter? = null
    private var photoEditor: PhotoEditor? = null
    private var stickers: List<ApiClient.Sticker>? = null
    private var packName = ""

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val imageUri = arguments?.getParcelable<Uri>(IMAGE_URI)!!
        val photo = rotateImage(imageUri)

        if (photo != null)
            sticker_on_photo_editor.source.setImageBitmap(photo)

        initEditor()
        initStickerPackList()
        initStickerList()
        initButtons()
    }

    private fun rotateImage(photoPath: Uri): Bitmap? {
        try {
            val imageBitmap = getBitmapFromUri(photoPath)!!
            val ei = ExifInterface(photoPath.path!!)
            val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
            )

            return when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(imageBitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(imageBitmap, 180);
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(imageBitmap, 270)
                ExifInterface.ORIENTATION_NORMAL -> imageBitmap
                else -> imageBitmap
            }
        }
        catch (e : java.lang.Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun getBitmapFromUri(photoPath: Uri): Bitmap? {
        val contentResolver = context!!.contentResolver
        val outputUri = PhotoProvider.getPhotoUri(photoPath)
        contentResolver.notifyChange(outputUri, null);
        return try {
            android.provider.MediaStore.Images.Media.getBitmap(contentResolver, outputUri)

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun rotateImage(source: Bitmap, angle: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle.toFloat())
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sticker_on_photo, container, false)
    }

    override fun onResume() {
        super.onResume()
        if (view == null) {
            return
        }
        view!!.isFocusableInTouchMode = true
        view!!.requestFocus()
        view!!.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                showExitDialog()
                false
            }
            true

        }
        (activity as MainActivity).setTabsVisibility(false)
    }

    override fun onStickerSelected(currentPosition: Int) {
        ApiClient.getInstance(context).selectedTab = currentPosition
        val lastPosition = ApiClient.getInstance(context).previousSelectedTab
        val packIndex = ApiClient.getInstance(context).installedPacks.size - currentPosition - 1
        val pack = ApiClient.getInstance(context).installedPacks[packIndex]
        AnalyticHelper.sendEvent(PackOpened(pack.name, pack.quantity, PackOpened.Source.PHOTO))
        setStickerList(pack.id)
        packName = pack.name

        sticker_on_photo_packs.adapter?.notifyItemChanged(lastPosition)
        sticker_on_photo_packs.adapter?.notifyItemChanged(currentPosition)
    }

    private fun initEditor() {
        photoEditor = PhotoEditor.Builder(context, sticker_on_photo_editor)
            .build()
    }

    private fun initStickerPackList() {
        sticker_on_photo_new_pack.setOnClickListener {
            AnalyticHelper.sendEvent(GoHome(GoHome.Source.PHOTO))
            onClose()
        }

        sticker_on_photo_packs.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        val packs = ApiClient.getInstance(context).createInstalledPacksList(context)
        val adapter = StickersAdapter("", packs, false, true)
        adapter.setOnStickerSelectedListener(this@StickerOnPhotoFragment)
        sticker_on_photo_packs.adapter = adapter
    }

    private fun initStickerList() {
        sticker_on_photo_sticker_list.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        stickersAdapter = StickersOnPhotoAdapter { position ->
            stickers?.let {
                AnalyticHelper.sendEvent(
                    StickerToPhoto(
                        packName,
                        it.size,
                        position,
                        AnalyticHelper.getStickerTypeByFileName(it[position].filename)
                    )
                )
                val image = Utils.getStickerDownloadURL(it[position].filename)
                val getBitmapFromUrl = GetBitmapFromUrl { photoEditor?.addImage(it) }
                getBitmapFromUrl.execute(image)
            }
        }
        sticker_on_photo_sticker_list.adapter = stickersAdapter

        val firstPack =
            ApiClient.getInstance(context).installedPacks[ApiClient.getInstance(context).installedPacks.size - 1]
        packName = firstPack.name
        setStickerList(firstPack.id)
        ApiClient.getInstance(context).selectedTab = 0
    }

    private fun initButtons() {
        sticker_on_photo_close.setOnClickListener { onClose() }
        sticker_on_photo_share.setOnClickListener { doShare() }
    }

    private fun onClose() {
        showExitDialog()
    }

    private fun showExitDialog() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.sticker_on_photo_close_dialog_title)
            .setMessage(R.string.sticker_on_photo_close_dialog_text)
            .setPositiveButton(R.string.btn_cancel) { _, _ -> }
            .setNegativeButton(R.string.btn_dismiss) { _, _ ->
                (activity as MainActivity?)?.onBackPressed()
            }
            .show()
    }


    private fun setStickerList(packId: Int) {
        ApiClient.getInstance(context)
            .apiService.getStickerPackById(SharedPreferencesHelper.getUserToken(context), packId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ stickers ->
                this.stickers = stickers
                stickersAdapter?.setAll(stickers)
                sticker_on_photo_sticker_list.layoutManager?.scrollToPosition(0)
            }, { })
    }

    private fun doShare() {
        AnalyticHelper.sendEvent(PhotoShare())
        val saveSettings = SaveSettings.Builder()
            .setClearViewsEnabled(false)
            .build()
        photoEditor?.saveAsBitmap(saveSettings, object : OnSaveBitmap {
            override fun onFailure(e: Exception?) {
                context.toast(getString(R.string.cache_error))
            }

            override fun onBitmapReady(saveBitmap: Bitmap?) {
                saveBitmap?.let {
                    val fileUri = saveImage(it)
                    fileUri?.let {
                        shareImageUri(it)
                    } ?: context.toast(getString(R.string.cache_error))
                } ?: context.toast(getString(R.string.processing_error))
            }
        }) ?: context.toast(getString(R.string.processing_error))
    }

    private fun saveImage(image: Bitmap): Uri? {
        var uri: Uri? = null
        context?.let { context ->
            val imagesFolder = File(context.cacheDir, "images")
            try {
                imagesFolder.mkdirs()
                val file = File(imagesFolder, "shared_image.png")

                val stream = FileOutputStream(file)
                image.compress(Bitmap.CompressFormat.PNG, 90, stream)
                stream.flush()
                stream.close()
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return uri
    }

    private fun shareImageUri(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.type = "image/png"
        startActivity(intent)
    }
}
