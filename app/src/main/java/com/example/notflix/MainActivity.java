package com.example.notflix;

import android.app.Activity;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // https://developer.android.com/training/scheduling/wakelock#screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        WebView.setWebContentsDebuggingEnabled(true);

        webView = findViewById(R.id.main_webview);
        webView.setWebViewClient(new MyWebViewClient());
        // This stops the screen from flickering to white while
        // loading the Webview.
        webView.setBackgroundColor(Color.TRANSPARENT);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setAppCacheEnabled(true);

        webView.loadUrl("https://192.168.178.24:8080/");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.requestFocus();
    }

    @Override
    public void onBackPressed() {
        if (webView != null) { // && webView.canGoBack())
            // let i = Instrumentation();
            // if there is previous page open it
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
            // webView.goBack();
        } else {
            // if there is no previous page, close app
            // super.onBackPressed();
        }
    }

    public static class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Only during development!
            if (error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                Log.d("MainActivity", "Untrusted SSL cert - accepting anyway");
                handler.proceed();
            } else {
                Log.d("MainActivity", "SSL error, rejecting: " + error.getPrimaryError());
                handler.cancel();
            }
        }
    }
}
