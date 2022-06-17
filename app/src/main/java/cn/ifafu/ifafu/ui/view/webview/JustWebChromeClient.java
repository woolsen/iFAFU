package cn.ifafu.ifafu.ui.view.webview;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class JustWebChromeClient extends WebChromeClient {

    private JustWebView.OnTitleChangedListener mOnTitleChangedListener;

    void setOnTitleChangedListener(JustWebView.OnTitleChangedListener l) {
        mOnTitleChangedListener = l;
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        if (mOnTitleChangedListener != null && !title.startsWith("http")) {
            mOnTitleChangedListener.shouldOnTitleChanged(title);
        }
    }
}
