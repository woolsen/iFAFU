package cn.ifafu.ifafu.ui.view.webview;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Objects;

public class JustWebViewClient extends WebViewClient {

    private static final String TAG = "JustWebViewClient";

    private Context mContext;
    private JustWebView.OnTitleChangedListener mOnTitleChangedListener;

    void setContext(Context context) {
        mContext = context;
    }

    void setOnTitleChangedListener(JustWebView.OnTitleChangedListener l) {
        mOnTitleChangedListener = l;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        CookieManager manager = CookieManager.getInstance();
        String cookie = manager.getCookie(url);
        Log.d(TAG, "url: " + url);
        Log.d(TAG, "cookie: " + cookie);
        super.onPageFinished(view, url);
        if (mOnTitleChangedListener != null) {
            String title = view.getTitle();
            if (!Objects.equals(title, url)) {
                mOnTitleChangedListener.shouldOnTitleChanged(title);
            } else {
                mOnTitleChangedListener.shouldOnTitleChanged(null);
            }
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (url.startsWith("tel:")) {
            actionDial(url);
            return true;
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    private void actionDial(String url) {
        if (mContext == null) return;
        Intent intent = new Intent((Intent.ACTION_DIAL));
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}