package com.goodideas.pixelparade.ui

import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.ButterKnife
import com.crashlytics.android.Crashlytics
import com.goodideas.pixelparade.R
import com.goodideas.pixelparade.SharedPreferencesHelper
import com.goodideas.pixelparade.data.ApiClient
import com.goodideas.pixelparade.data.analityc.AnalyticHelper
import com.goodideas.pixelparade.data.analityc.event.GoHome
import com.goodideas.pixelparade.data.analityc.event.PackOpened
import com.goodideas.pixelparade.ui.adapters.StickersAdapter
import com.goodideas.pixelparade.ui.screen.*
import com.livetyping.permission.PermissionBinder
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(), StickersAdapter.OnStickerSelectedListener {
    companion object {
        @kotlin.jvm.JvmStatic
        var instance: MainActivity? = null
            private set
    }

    lateinit var permissionBinder: PermissionBinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        Fabric.with(this, Crashlytics())
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        permissionBinder = PermissionBinder()

        Timber.plant(Timber.DebugTree())
        instance = this

        if (savedInstanceState != null) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                onBackPressed()
            }
        }
        showLoginFragment()

        fl_plus.setOnClickListener { onNewPackClick() }
    }

    override fun onStart() {
        super.onStart()
        permissionBinder.attach(this)
    }

    override fun onStop() {
        permissionBinder.detach(this)
        super.onStop()
    }

    fun onNewPackClick() {
        if (ApiClient.getInstance(this).selectedTab != -1) {
            ApiClient.getInstance(this).selectedTab = -1
            AnalyticHelper.sendEvent(GoHome(GoHome.Source.MAIN))

            val packsToBuyCount = ApiClient.getInstance(this).packsToBuy.size
            replaceFragment(if (packsToBuyCount != 0) MainFragment() else AllLoadedFragment())

            val lastPosition = ApiClient.getInstance(this).previousSelectedTab
            Handler().postDelayed({
                rv_downloaded_stickers.adapter?.notifyItemChanged(lastPosition)
                fl_plus.setBackgroundResource(R.drawable.img_plus_bg)
            }, 100)
        }
    }

    override fun onBackPressed() {
        if (MainFragment.instance != null) {
            if (MainFragment.instance!!.search_view.isSearchOpen) {
                MainFragment.instance!!.search_view.closeSearch()
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
        if (supportFragmentManager.backStackEntryCount <= 1) {
            setTabsVisibility(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionBinder.onActivityResult(requestCode, data, this)
        if (requestCode == MainFragment.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val photoFilePath = SharedPreferencesHelper.getPhotoFilePath(this)
            val photoFileUri = Uri.parse(photoFilePath)
            val fragment = StickerOnPhotoFragment.newInstance(photoFileUri)
            addFragment(fragment)
        }
        if (requestCode == MainFragment.REQUEST_IMAGE_LOAD && resultCode == RESULT_OK) {
            data?.data?.let {
                val realUrl = getRealPathFromURI(it)
                val fragment = StickerOnPhotoFragment.newInstance(realUrl)
                addFragment(fragment)
            }
        }
    }

    fun getRealPathFromURI(uri: Uri): Uri {
        val projection = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        val column_index = cursor
            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val realUrl = cursor.getString(column_index)
        return Uri.parse(realUrl)
    }

    public override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment, fragment.javaClass.simpleName)
        transaction.addToBackStack(fragment.javaClass.simpleName)
        transaction.commit()
    }

    fun replaceFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment, fragment.javaClass.simpleName)
        transaction.commitAllowingStateLoss()
    }

    fun createTabs() {
        rv_downloaded_stickers.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        val pack = ApiClient.getInstance(this).createInstalledPacksList(this)
        val horzAdapter = StickersAdapter("", pack, false, true)
        horzAdapter.setOnStickerSelectedListener(this)
        horzAdapter.itemsInRow = 1
        horzAdapter.firstVisibleItem = 0
        horzAdapter.lastVisibleItem = pack.stickers.size
        rv_downloaded_stickers.adapter = horzAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionBinder.onRequestPermissionResult(requestCode, grantResults)
    }

    fun showLoginFragment() {
        clearAllImages()
        replaceFragment(LoginFragment())
    }

    private fun clearAllImages() {
        val filename = Environment.getExternalStorageDirectory().toString() + "/PixelParade"
        val dir = File(filename)
        val children = dir.list()
        if (children != null) {
            for (i in children.indices) {
                val subdir = File(dir, children[i])
                val subdirChildren = subdir.list()
                for (j in subdirChildren.indices) {
                    File(subdir, subdirChildren[j]).delete()
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (StickerFragment.getInstance() != null) {
            if (event.y >= StickerFragment.getInstance().bottomSheetLayout.y + resources.getDimensionPixelSize(
                    R.dimen.shares_top_margin
                )
            ) {
                val ev1 = MotionEvent.obtain(event)
                ev1.setLocation(
                    event.x,
                    event.y - StickerFragment.getInstance().bottomSheetLayout.y
                )
                StickerFragment.getInstance().bottomSheetLayout.dispatchTouchEvent(ev1)
                return true
            } else {
                val v = StickerFragment.getInstance().imageView
                if (!(event.x >= v.left && event.x <= v.right && event.y >= v.top && event.y <= v.bottom)) {
                    onBackPressed()
                    return true
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onStickerSelected(currentPosition: Int) {
        ApiClient.getInstance(this).selectedTab = currentPosition
        val lastPosition = ApiClient.getInstance(this).previousSelectedTab
        val packIndex = ApiClient.getInstance(this).installedPacks.size - currentPosition - 1
        val pack = ApiClient.getInstance(this).installedPacks[packIndex]
        AnalyticHelper.sendEvent(PackOpened(pack.name, pack.quantity, PackOpened.Source.MAIN))
        val fragment = PackFragment.newInstance(pack)
        replaceFragment(fragment)
        Handler().postDelayed({
            rv_downloaded_stickers.adapter?.notifyItemChanged(lastPosition)
            rv_downloaded_stickers.adapter?.notifyItemChanged(currentPosition)
            fl_plus.setBackgroundColor(Color.WHITE)
        }, 100)
    }

    fun setTabsVisibility(isVisible: Boolean) {
        ll_bottom_divider.visibility = if (isVisible) View.VISIBLE else View.GONE
        ll_bottom.visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}
