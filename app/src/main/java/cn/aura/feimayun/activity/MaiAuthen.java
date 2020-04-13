package cn.aura.feimayun.activity;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import cn.aura.feimayun.R;
import cn.aura.feimayun.util.Util;

public class MaiAuthen extends BaseActivity implements View.OnClickListener {
    private WebView mai_authen_webView;
    private boolean canFinish = false;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mai_authen);

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
                        findViewById(R.id.view).getLayoutParams().height = top;
                    }
                    return windowInsets.consumeSystemWindowInsets();
                }
            });
        }

        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        headtitle_textview.setText("脉脉认证");
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(this);

        mai_authen_webView = findViewById(R.id.mai_authen_webView);
        WebSettings webSettings = mai_authen_webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mai_authen_webView.setWebViewClient(new WebViewController());
        String apud = Util.getUid();
        if (!TextUtils.isEmpty(apud)) {
            mai_authen_webView.loadUrl("https://yun.aura.cn/Maimai/showSyncMaimai/uid/" + apud + ".html");
        } else {
            Toast.makeText(this, "请登录~", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.headtitle_layout) {
            if (mai_authen_webView.canGoBack()) {
                if (canFinish) {
                    finish();
                } else {
                    mai_authen_webView.goBack();
                }
            } else {
                finish();
            }
        }
    }

    class WebViewController extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        //2644
        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            Uri uri;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                uri = request.getUrl();
                String scheme = uri.getScheme();
                String host = uri.getHost();
                String path = uri.getPath();
                String urlString = scheme + "://" + host + path;
                Log.d("191107", "url: " + urlString);
                if (urlString.equals("https://yun.aura.cn/Maimai/showSyncMaimai/uid/" + Util.getUid() + ".html") ||
                        urlString.equals("https://yun.aura.cn/Maimai/showSyncMaimai/uid/" + Util.getUid() + "/lids/")) {
                    canFinish = true;
                }
                if (urlString.equals("https://maimai.cn/oauth_login")) {
                    canFinish = false;
                }
            }
            return super.shouldInterceptRequest(view, request);
        }
    }

    @Override
    public void onBackPressed() {
        if (mai_authen_webView.canGoBack()) {
            if (canFinish) {
                finish();
            } else {
                mai_authen_webView.goBack();
            }
        } else {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mai_authen_webView.canGoBack()) {
                if (canFinish) {
                    finish();
                } else {
                    mai_authen_webView.goBack();
                }
            } else {
                finish();
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onDestroy() {
        if (mai_authen_webView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = mai_authen_webView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mai_authen_webView);
            }
            mai_authen_webView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mai_authen_webView.getSettings().setJavaScriptEnabled(false);
            mai_authen_webView.clearHistory();
            mai_authen_webView.removeAllViews();
            mai_authen_webView.destroy();
        }
        super.onDestroy();
    }
}
