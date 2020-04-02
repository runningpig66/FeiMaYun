package cn.aura.feimayun.activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cn.aura.feimayun.R;

public class H5Activity extends BaseActivity {
    WebView web_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5);
        web_view = findViewById(R.id.web_view);
        WebSettings webSettings = web_view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        web_view.setWebViewClient(new WebViewController());
        String link_url = getIntent().getStringExtra("link_url");
        web_view.loadUrl(link_url);
    }

    static class WebViewController extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    public void onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack();
        } else {
            finish();
        }
    }
}
