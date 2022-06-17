package cn.ifafu.ifafu.ui.view.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * WebView的适配方案
 * <p>
 * 不在xml中定义 Webview，而是在需要的时候在Activity中创建，对传入WebView的Context
 * 使用ApplicationContext而不是ActivityContext。因为这样做可以在onDestory()里销毁
 * 掉webview及时清理内存；创建webview需要使用ApplicationContext而不是Activity的
 * context，销毁时不再占有Activity对象
 * <p>
 * 在 Activity销毁WebView的时，需要在[Activity.onDestroy]中：调用[destroyIt]最后
 * WebView置空。
 */
public class JustWebView extends WebView {

    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;

    private OnTitleChangedListener mOnTitleChangedListener;

    public JustWebView(Context context) {
        this(context, null);
    }

    public JustWebView(Context context, AttributeSet attrs) {
        this(context, attrs, Resources.getSystem().getIdentifier("webViewStyle", "attr", "android"));
    }

    public JustWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSetting();
        setWebChromeClient(new JustWebChromeClient());
        setWebViewClient(new JustWebViewClient());
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initSetting() {
        WebSettings settings = this.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccessFromFileURLs(true);
        // 缓存
        String cachePath = getContext().getDir("cache", Context.MODE_PRIVATE).getPath();
        settings.setAppCachePath(cachePath);
    }

    public OnTitleChangedListener getOnTitleChangedListener() {
        return mOnTitleChangedListener;
    }

    public void setOnTitleChangedListener(@Nullable OnTitleChangedListener l) {
        mOnTitleChangedListener = l;
        if (mWebViewClient != null && mWebViewClient instanceof JustWebViewClient) {
            ((JustWebViewClient) mWebViewClient).setOnTitleChangedListener(l);
        }
        if (mWebChromeClient != null && mWebChromeClient instanceof JustWebChromeClient) {
            ((JustWebChromeClient) mWebChromeClient).setOnTitleChangedListener(l);
        }
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        super.setWebChromeClient(client);
        mWebChromeClient = client;
        if (mWebChromeClient instanceof JustWebChromeClient) {
            ((JustWebChromeClient) mWebChromeClient).setOnTitleChangedListener(mOnTitleChangedListener);
        }
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        super.setWebViewClient(client);
        mWebViewClient = client;
        if (mWebViewClient instanceof JustWebViewClient) {
            ((JustWebViewClient) mWebViewClient).setContext(getContext());
            ((JustWebViewClient) mWebViewClient).setOnTitleChangedListener(mOnTitleChangedListener);
        }
    }

    /**
     * 销毁WebView时调用，同时在Activity或者Fragment中将WebView置空
     */
    public void destroyIt() {
        this.destroy();
    }

    @Override
    public void destroy() {
        this.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        this.stopLoading();
        this.clearHistory();
        this.setWebChromeClient(null);
        this.setWebViewClient(null);
        ViewParent parent = getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(this);
        }
        super.destroy();
    }

    public abstract static class OnTitleChangedListener {

        private String mLastTitle = null;

        public final void shouldOnTitleChanged(String title) {
            if (title == null) return;
            if (!Objects.equals(title, mLastTitle)) {
                mLastTitle = title;
                onTitleChanged(title);
            }
        }

        /**
         * 监听Title变化
         */
        public abstract void onTitleChanged(String title);
    }
}
