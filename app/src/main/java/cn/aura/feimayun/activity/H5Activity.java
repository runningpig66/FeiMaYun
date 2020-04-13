package cn.aura.feimayun.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
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
        //设置CutoutMode
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(params);
        }
        if (Build.VERSION.SDK_INT >= 28) {
            findViewById(R.id.root0).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets windowInsets) {
                    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
                    if (displayCutout != null) {
                        int left = displayCutout.getSafeInsetLeft();
                        int top = displayCutout.getSafeInsetTop();
                        int right = displayCutout.getSafeInsetRight();
                        int bottom = displayCutout.getSafeInsetBottom();
                        findViewById(R.id.playdeatil_view).getLayoutParams().height = top;
                    }
                    return windowInsets.consumeSystemWindowInsets();
                }
            });
        }

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
