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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends Activity {

    WebView webView;
    long backPressedTimeStamp;
    int backPressedCounter;
    AtomicBoolean develEnabled= new AtomicBoolean(false);
    SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // https://developer.android.com/training/scheduling/wakelock#screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Get settings.
        prefs = getPreferences(Context.MODE_PRIVATE);
        String defaultPwaUrl = getDefaultPwaUrl();
        Boolean bootIntoChooser = getBootIntoChooser();
        if (bootIntoChooser) {
            // bootIntoChooser is a one-time override.
            setBootIntoChooser(false);
        }
        develEnabled.set(prefs.getBoolean("develEnabled", false));
        if (develEnabled.get()) {
            // This is a static method.
            WebView.setWebContentsDebuggingEnabled(true);
        }

        // adds support for https://appassets.androidplatform.net/assets/index.html
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .addPathHandler("/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        // Build and enable webview.
        webView = findViewById(R.id.main_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "androidApp");
        webView.setWebViewClient(new MyWebViewClient(assetLoader, develEnabled));

        // This stops the screen from flickering to white while loading the Webview.
        webView.setBackgroundColor(Color.TRANSPARENT);

        // Find out what URL to load.
        String url;
        if (!defaultPwaUrl.isEmpty() && !bootIntoChooser) {
            url = defaultPwaUrl;
        } else {
            url = "https://appassets.androidplatform.net/index.html";
        }
        webView.loadUrl(url);
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
                setBootIntoChooser(true);
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

    @JavascriptInterface
    public String getDefaultPwaUrl() {
        return prefs.getString("defaultPwaUrl", "");
    }

    @JavascriptInterface
    public void setDefaultPwaUrl(String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("defaultPwaUrl", value);
        editor.apply();
    }

    @JavascriptInterface
    public int getDevelEnabled() {
        return prefs.getBoolean("develEnabled", false) ? 1 : 0;
    }

    @JavascriptInterface
    public void setDevelEnabled(String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("develEnabled", value.equals("true"));
        editor.apply();
    }

    @JavascriptInterface
    public void setDevelEnabledOnce(String value) {
        develEnabled.set(value.equals("true"));
        // "Toggling of Web Contents Debugging must be done on the UI thread"
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WebView.setWebContentsDebuggingEnabled(develEnabled.get());
            }
        });
    }

    public Boolean getBootIntoChooser() {
        return prefs.getBoolean("bootIntoChooser", false);
    }

    public void setBootIntoChooser(Boolean value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("bootIntoChooser", value);
        editor.apply();
    }
}

class MyWebViewClient extends WebViewClient {
    AtomicBoolean mDevelMode;
    private final WebViewAssetLoader mAssetLoader;

    public MyWebViewClient(WebViewAssetLoader assetLoader, AtomicBoolean develMode) {
        this.mDevelMode = develMode;
        mAssetLoader = assetLoader;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        return mAssetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        // Only during development!
        if (mDevelMode.get() && error.getPrimaryError() == SslError.SSL_UNTRUSTED) {
            Log.d("MainActivity", "Untrusted SSL cert - accepting anyway");
            handler.proceed();
        } else {
            Log.d("MainActivity", "SSL error, rejecting: " + error.getPrimaryError());
            handler.cancel();
        }
    }
}

