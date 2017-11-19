package com.oneupsquad.mediadisplay;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

/**
 * Created by User on 19.11.2017.
 */

public class WebViewActivity extends Activity {
    private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        webView = (WebView) findViewById(R.id.rainRadar);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("https://www.sataako.fi/");

    }
}
