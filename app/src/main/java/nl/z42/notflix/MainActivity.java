package nl.z42.notflix;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    WebView webView;
    long backPressedTimeStamp;
    int backPressedCounter;
    boolean developMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // https://developer.android.com/training/scheduling/wakelock#screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        developMode = prefs.getBoolean("devel", true);

        if (developMode) {
            // This is a static method.
            WebView.setWebContentsDebuggingEnabled(true);
        }

        webView = findViewById(R.id.main_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new JsWebInterface(this), "androidApp");
        webView.setWebViewClient(new MyWebViewClient(developMode));

        // This stops the screen from flickering to white while loading the Webview.
        webView.setBackgroundColor(Color.TRANSPARENT);

        // Finally load PWA.
        String url = "https://192.168.178.24:8080/";
        boolean loadPwa = prefs.getBoolean("loadPwa", false);
        if (loadPwa) {
            url += "?loadPwa=1";
        }
        webView.loadUrl("https://192.168.178.24:8080/");
        // webView.loadUrl("https://www.google.com/");
        // webView.loadUrl("file:///android_asset/index.html");
        Log.d("MainActivity", "onCreate: URL loaded into webview.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
    }

    @Override
    public void onBackPressed() {

        // Press "back" six times in one second, and we'll reload.
        long now = System.currentTimeMillis();
        if (backPressedTimeStamp == 0) {
            backPressedTimeStamp = now;
        }
        if (backPressedTimeStamp + 1000 > now) {
            backPressedCounter++;
            if (backPressedCounter >= 5) {
                // On next reload, do NOT load the PWA.
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("loadPwa", false);
                editor.apply();
                // Destroy and recreate activity.
                recreate();
            }
        } else {
            backPressedTimeStamp = now;
            backPressedCounter = 0;
        }

        // Normally we'd do here something like
        // if webView.canGoBack() {
        //     webview.goBack();
        // } else {
        //     super.onBackPressed();
        // }
        //
        // However, we want to remap the "back" key to Escape, so we do that here.
        if (webView != null) {
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE));
            dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE));
        }
    }

    public static class MyWebViewClient extends WebViewClient {
        boolean develMode;

        public MyWebViewClient(boolean develMode) {
            this.develMode = develMode;
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            // Only during development!
            if (develMode && error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
                Log.d("MainActivity", "Untrusted SSL cert - accepting anyway");
                handler.proceed();
            } else {
                Log.d("MainActivity", "SSL error, rejecting: " + error.getPrimaryError());
                handler.cancel();
            }
        }
    }
}

class JsWebInterface {
    SharedPreferences prefs;
    public JsWebInterface(Activity activity) {
        this.prefs = activity.getPreferences(Context.MODE_PRIVATE);
    }
    @JavascriptInterface
    public String getPref(String key) {
        return prefs.getString(key, "");
    }
    @JavascriptInterface
    public void setPref(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}