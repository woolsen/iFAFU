package cn.ifafu.ifafu.ui.web

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.webkit.*
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import cn.ifafu.ifafu.bean.bo.ZFApiEnum
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.databinding.WebActivityBinding
import cn.ifafu.ifafu.repository.UserRepository
import cn.ifafu.ifafu.service.common.ZfUrlProvider
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.view.LoadingDialog
import cn.ifafu.ifafu.ui.view.webview.JustWebView
import com.blankj.utilcode.util.SPUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WebActivity : BaseActivity() {

    @Inject
    lateinit var userRepository: UserRepository

    companion object {
        private const val PHOTO_REQUEST_CODE = 1023

        fun intentFor(
            context: Context,
            url: String? = null,
            title: String? = null,
            referer: String? = null,
            cookie: String? = null
        ): Intent {
            val intent = Intent(context, WebActivity::class.java)
            if (url != null) {
                intent.putExtra("url", url)
            }
            if (title != null) {
                intent.putExtra("title", title)
            }
            if (referer != null) {
                intent.putExtra("referer", referer)
            }
            if (cookie != null) {
                intent.putExtra("cookie", cookie)
            }
            return intent
        }
    }

    private var mLoadingDialog: LoadingDialog? = LoadingDialog(this, "加载中")

    private lateinit var webView: JustWebView
    private lateinit var binding: WebActivityBinding

    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

    private var first = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightUiBar()
        binding = WebActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initWebView()

        lifecycleScope.launchWhenCreated {
            val url: String = intent.getStringExtra("url") ?: let {
                val user = userRepository.getUser()
                if (user == null) {
                    showToast("出错了，无法找到用户")
                    return@launchWhenCreated
                }
                ZfUrlProvider.getUrl(ZFApiEnum.MAIN, user).apply {
                    setCookie(this, SPUtils.getInstance(Constants.SP_COOKIE).getString("cookie"))
                }
            }

            val cookie: String? = intent.getStringExtra("cookie")
            if (cookie != null) {
                setCookie(url, cookie)
            }

            val title: String = intent.getStringExtra("title") ?: "正方教务管理系统"
            binding.tbWeb1.title = title

            val referer: String? = intent.getStringExtra("referer")
            if (referer != null) {
                val extraHeaders = mapOf(
                    "referer" to referer
                )
                webView.loadUrl(url, extraHeaders)
            } else {
                webView.loadUrl(url)
            }
        }
    }


    private fun setCookie(url: String, cookie: String?) {
        val cookieManager: CookieManager = CookieManager.getInstance()
        cookieManager.flush()
        cookieManager.setAcceptCookie(true)
        cookieManager.setCookie(url, cookie)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView = JustWebView(this)
        binding.viewRoot.addView(
            webView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        )
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mLoadingDialog?.cancel()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                mLoadingDialog?.show()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (first && intent.hasExtra("referer")) {
                    val referer = intent.getStringExtra("referer")
                    request?.requestHeaders?.put("referer", referer)
                    first = false
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                mFilePathCallback = filePathCallback
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, PHOTO_REQUEST_CODE)
                return true
            }
        }

        binding.btnRefresh.setOnClickListener { webView.reload() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.run {
                mFilePathCallback?.onReceiveValue(arrayOf(this))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        webView.resumeTimers()
        webView.onResume()
        super.onResume()
    }

    override fun onPause() {
        webView.pauseTimers()
        webView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mLoadingDialog?.cancel()
        mLoadingDialog = null
        webView.destroy()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            //按返回键操作并且能回退网页
            if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                //后退
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

}
