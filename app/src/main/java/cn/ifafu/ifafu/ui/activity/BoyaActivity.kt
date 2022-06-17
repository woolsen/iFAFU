package cn.ifafu.ifafu.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.FrameLayout
import cn.ifafu.ifafu.R
import cn.ifafu.ifafu.ui.common.BaseActivity
import cn.ifafu.ifafu.ui.view.webview.JustWebView
import cn.ifafu.ifafu.constant.Constants
import cn.ifafu.ifafu.databinding.BoyaActivityBinding

class BoyaActivity : BaseActivity(R.layout.boya_activity) {

    private val url = Constants.BOYA_URL

    private lateinit var webView: JustWebView
    private lateinit var binding: BoyaActivityBinding

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightUiBar()
        binding = BoyaActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initWebView()

        binding.btnBack.setOnClickListener { onBackPressed() }
        binding.btnClose.setOnClickListener { finish() }
    }

    private fun initWebView() {
        val webView = JustWebView(applicationContext).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            onTitleChangedListener = object : JustWebView.OnTitleChangedListener() {
                override fun onTitleChanged(title: String?) {
                    binding.tvTitle.text = title
                }
            }
        }.also { this.webView = it }
        binding.layoutRoot.addView(webView, 0)

        webView.loadUrl(url)
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
        webView.destroyIt()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack() && webView.url != url) {
            webView.goBack()
            return
        }
        super.onBackPressed()
    }
}