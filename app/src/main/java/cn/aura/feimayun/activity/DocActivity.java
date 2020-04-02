package cn.aura.feimayun.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.aura.feimayun.R;
import cn.aura.feimayun.util.OnClickListener;

public class DocActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doc);
        TextView headtitle_textview = findViewById(R.id.headtitle_textview);
        RelativeLayout headtitle_layout = findViewById(R.id.headtitle_layout);
        headtitle_layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        WebView web_view = findViewById(R.id.web_view);
        web_view.setInitialScale(200);
        WebSettings webSettings = web_view.getSettings();
        webSettings.setJavaScriptEnabled(true);
        web_view.setWebViewClient(new WebViewController());
        String type = getIntent().getStringExtra("type");//a1和a2两个文件
        if (type.equals("a1")) {
            headtitle_textview.setText("用户协议");
            web_view.loadUrl("file:///android_asset/a1.html");
        } else {
            headtitle_textview.setText("隐私政策");
            web_view.loadUrl("file:///android_asset/a2.html");
        }
    }

    public static class WebViewController extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(android.webkit.WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
