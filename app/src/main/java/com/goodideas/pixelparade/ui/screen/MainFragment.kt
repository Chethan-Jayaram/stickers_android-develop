package com.goodideas.pixelparade.ui.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goodideas.pixelparade.BuildConfig
import com.goodideas.pixelparade.Consts
import com.goodideas.pixelparade.R
import com.goodideas.pixelparade.SharedPreferencesHelper
import com.goodideas.pixelparade.data.ApiClient
import com.goodideas.pixelparade.data.analityc.AnalyticHelper
import com.goodideas.pixelparade.data.analityc.event.*
import com.goodideas.pixelparade.ext.browse
import com.goodideas.pixelparade.ui.MainActivity
import com.goodideas.pixelparade.ui.adapters.StickerPacksAdapter
import com.goodideas.pixelparade.ui.adapters.TagAdapter
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainFragment : Fragment() {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1000
        const val REQUEST_IMAGE_LOAD = 2000
        private const val PRIVACY_POLICY_URL = "https://goodideas.io/privacy"

        @kotlin.jvm.JvmStatic
        var instance: MainFragment? = null
            private set
    }

    val stickerPacksAdapter: StickerPacksAdapter
        get() = rv_sticker_packs.adapter as StickerPacksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initTags()
        searchListener()
    }

    private fun initToolbar() {
        val logo = layoutInflater.inflate(R.layout.pixel_parade_logo_bar, null)
        main_toolbar.addView(logo)
        main_toolbar.menu.removeGroup(0)
        main_toolbar.inflateMenu(R.menu.menu_main)
        val searchItem = main_toolbar.menu.findItem(R.id.main_menu_search)
        search_view.setMenuItem(searchItem)
        search_view.setOnSearchViewListener(object : MaterialSearchView.SearchViewListener {
            override fun onSearchViewShown() {
                AnalyticHelper.sendEvent(SearchFeature())
            }

            override fun onSearchViewClosed() {}
        })
        main_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.main_menu_camera -> {
                    onCameraClick()
                    true
                }
                R.id.main_menu_privacy_policy -> {
                    context?.browse(PRIVACY_POLICY_URL, false)
                    true
                }
                else -> false
            }
        }
    }

    private fun initTags() {
        val token = SharedPreferencesHelper.getUserToken(activity)
        ApiClient.getInstance(activity).apiService.getApiKeySearchWords(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
                val tagAdapter = TagAdapter { onTagClick(it) }
                tagAdapter.setAll(it)
                main_tags.adapter = tagAdapter
            }, {})
        main_tags.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (recyclerView.canScrollHorizontally(1)) {
                    showEndFog()
                } else {
                    hideEndFog()
                }
                if (recyclerView.canScrollHorizontally(-1)) {
                    showStartFog()
                } else {
                    hideStartFog()
                }
            }
        })
    }

    private fun onTagClick(tag: String) {
        AnalyticHelper.sendEvent(SearchTag(tag))
        search_view.showSearch()
        search_view.setQuery(tag, false)
    }

    private fun searchListener() {
        search_view.setOnQueryTextListener(object : MaterialSearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText?.trim())
                return false
            }
        })
    }

    private fun search(text: String?) {
        val packs = ApiClient.getInstance(context).packsToBuy
        if (text == null || text.isEmpty()) {
            stickerPacksAdapter.replacePacks(packs)
            showContent()
        } else {
            AnalyticHelper.sendEvent(SearchResults(text))
            val foundPacks = ArrayList<ApiClient.StickersPackJSON>()
            for (pack in packs) {
                if (pack.name.contains(text, true)
                    || pack.tags.contains(text, true)
                ) {
                    foundPacks.add(pack)
                }
            }
            if (foundPacks.isEmpty()) {
                showStickersNotFound()
            } else {
                stickerPacksAdapter.replacePacks(foundPacks)
                showContent()
            }
        }
    }

    private fun showStickersNotFound() {
        rv_sticker_packs.visibility = View.GONE
        main_stickers_not_found.visibility = View.VISIBLE
    }

    private fun showContent() {
        rv_sticker_packs.visibility = View.VISIBLE
        main_stickers_not_found.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        instance = this

        rv_sticker_packs.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rv_sticker_packs.adapter = StickerPacksAdapter(activity)

        (activity as MainActivity).createTabs()
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setTabsVisibility(true)
    }

    private fun onCameraClick() {
        AnalyticHelper.sendEvent(PhotoFeature())
        when {
            (activity as MainActivity?)?.rv_downloaded_stickers?.adapter?.itemCount == 0 -> showNoStickerPackDialog()
            activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) == true -> showPhotoReceivingDialog()
            else -> openGalleryScreen()
        }
    }

    private fun showNoStickerPackDialog() {
        context?.let { context ->
            AlertDialog.Builder(context)
                .setTitle(R.string.main_no_sticker_pack_dialog_title)
                .setMessage(R.string.main_no_sticker_pack_dialog_text)
                .setPositiveButton(R.string.btn_ok) { _, _ -> }
                .show()
        }
    }

    private fun showPhotoReceivingDialog() {
        context?.let { context ->
            AlertDialog.Builder(context)
                .setTitle(R.string.photo_receiving_dialog_title)
                .setItems(R.array.options_for_taking_photos) { _, which ->
                    when (which) {
                        0 -> openCameraScreen()
                        1 -> openGalleryScreen()
                    }
                }
                .setNegativeButton(R.string.btn_cancel) { _, _ ->
                    AnalyticHelper.sendEvent(PhotoDialog(PhotoDialog.Source.CANCEL))
                }.show()
        }
    }

    private fun openCameraScreen() {
        AnalyticHelper.sendEvent(PhotoDialog(PhotoDialog.Source.CAMERA))
        activity?.let { activity ->
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                takePictureIntent.resolveActivity(activity.packageManager)?.also {
                    val photoFile: File? = try {
                        createImageFile()
                    } catch (ex: IOException) {
                        null
                    }
                    photoFile?.also {
                        context?.let { context ->
                            val photoURI: Uri = FileProvider.getUriForFile(
                                context,
                                "${BuildConfig.APPLICATION_ID}.provider",
                                it
                            )
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                            activity.startActivityForResult(
                                takePictureIntent,
                                REQUEST_IMAGE_CAPTURE
                            )
                        }
                    }
                }
            }
        }
    }

    private fun openGalleryScreen() {
        AnalyticHelper.sendEvent(PhotoDialog(PhotoDialog.Source.LIB))
        (activity as MainActivity).permissionBinder.activePermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            getString(R.string.permission_write_external_storage_rationale),
            getString(R.string.permission_write_external_storage_button)
        ) { isGranted ->
            if (isGranted) {
                activity?.let { activity ->
                    val photoPickerIntent = Intent(Intent.ACTION_PICK)
                    photoPickerIntent.type = "image/*"
                    activity.startActivityForResult(photoPickerIntent, REQUEST_IMAGE_LOAD)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            SharedPreferencesHelper.setPhotoFilePath(context, toURI().toString())

        }
    }

    private fun showStartFog() {
        showFog(main_start_tags_fog)
    }

    private fun hideStartFog() {
        hideFog(main_start_tags_fog)
    }

    private fun showEndFog() {
        showFog(main_end_tags_fog)
    }

    private fun hideEndFog() {
        hideFog(main_end_tags_fog)
    }

    private fun showFog(view: View) {
        if (view.visibility == View.VISIBLE) return

        view.visibility = View.VISIBLE
        val animation = AlphaAnimation(0.1f, 1.0f)
        animation.duration = 500
        view.startAnimation(animation)
    }

    private fun hideFog(view: View) {
        if (view.visibility == View.GONE) return

        val animation = AlphaAnimation(1.0f, 0.1f)
        animation.duration = 500
        animation.setOnAnimationEndListener { view.visibility = View.GONE }
        view.startAnimation(animation)
    }

    private fun AlphaAnimation.setOnAnimationEndListener(action: () -> Unit) {
        setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                action.invoke()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }
}
