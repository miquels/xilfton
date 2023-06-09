package nl.z42.notflix

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.net.http.SslError
import android.net.http.SslError.SSL_EXPIRED
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.*
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewAssetLoader.AssetsPathHandler
import androidx.webkit.WebViewAssetLoader.ResourcesPathHandler
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

class MainActivity : android.app.Activity() {
    private var webView: WebView? = null
    private var backPressedTimeStamp: Long = 0
    private var backPressedCounter = 0
    private var develEnabled = AtomicBoolean(false)
    private var prefs: SharedPreferences? = null

    /*
    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackButton()
        }
    }
    */

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // https://developer.android.com/training/scheduling/wakelock#screen
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // val dispatcher =  getOnBackPressedDispatcher()
        // dispatcher.addCallback(this, onBackPressedCallback)

        // Get settings.
        prefs = getPreferences(MODE_PRIVATE)
        val defaultPwaUrl = defaultPwaUrl
        val bootIntoChooser = bootIntoChooser!!
        if (bootIntoChooser) {
            // bootIntoChooser is a one-time override.
            this.bootIntoChooser = false
        }
        develEnabled.set(prefs!!.getBoolean("develEnabled", false))
        if (develEnabled.get() || BuildConfig.DEBUG) {
            // This is a static method.
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // adds support for https://appassets.androidplatform.net/assets/index.html
        val assetLoader = WebViewAssetLoader.Builder()
                .addPathHandler("/res/", ResourcesPathHandler(this))
                .addPathHandler("/", AssetsPathHandler(this))
                .build()

        // Build and enable webview.
        webView = findViewById(R.id.main_webview)
        val webSettings = webView!!.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.domStorageEnabled = true
        webView!!.addJavascriptInterface(this, "androidApp")
        webView!!.webViewClient = MyWebViewClient(assetLoader, develEnabled)

        // This stops the screen from flickering to white while loading the Webview.
        webView!!.setBackgroundColor(Color.TRANSPARENT)

        // Find out what URL to load.
        val url: String = if (defaultPwaUrl!!.isNotEmpty() && !bootIntoChooser) {
            defaultPwaUrl
        } else {
            "https://appassets.androidplatform.net/index.html"
        }
        webView!!.loadUrl(url)
        Log.d("MainActivity", "onCreate: URL loaded into webview.")
    }

    override fun onResume() {
        super.onResume()
        webView!!.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (webView != null) {
            webView!!.stopLoading()
            // webView!!.setWebViewClient(null)
            webView!!.destroy()
        }
        webView = null
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {

        // Press "back" six times in one second, and we'll reload.
        val now = System.currentTimeMillis()
        if (backPressedTimeStamp == 0L) {
            backPressedTimeStamp = now
        }
        // Log.d("MainActivity", "BackPressedTimeStamp " + backPressedTimeStamp + " now " + now);
        if (backPressedTimeStamp + 1000 > now) {
            backPressedCounter++
            if (backPressedCounter >= 5) {
                // On next reload, do NOT load the PWA.
                bootIntoChooser = true
                // Destroy and recreate activity.
                recreate()
            }
        } else {
            backPressedTimeStamp = now
            backPressedCounter = 0
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
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ESCAPE))
            dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ESCAPE))
        }
    }

    @JavascriptInterface
    fun exitApplication() {
        moveTaskToBack(true)
        Process.killProcess(Process.myPid())
        exitProcess(1)
    }

    @get:JavascriptInterface
    @set:JavascriptInterface
    var defaultPwaUrl: String?
        get() = prefs!!.getString("defaultPwaUrl", "")
        set(value) {
            val editor = prefs!!.edit()
            editor.putString("defaultPwaUrl", value)
            editor.apply()
        }

    @JavascriptInterface
    fun getDevelEnabled(): Int {
        return if (prefs!!.getBoolean("develEnabled", false)) 1 else 0
    }

    @JavascriptInterface
    fun setDevelEnabled(value: String) {
        val editor = prefs!!.edit()
        editor.putBoolean("develEnabled", value == "true")
        editor.apply()
    }

    @JavascriptInterface
    fun setDevelEnabledOnce(value: String) {
        if (BuildConfig.DEBUG) {
            // Already enabled.
            return
        }
        develEnabled.set(value == "true")
        // "Toggling of Web Contents Debugging must be done on the UI thread"
        runOnUiThread { WebView.setWebContentsDebuggingEnabled(develEnabled.get()) }
    }

    private var bootIntoChooser: Boolean?
        get() = prefs!!.getBoolean("bootIntoChooser", false)
        set(value) {
            val editor = prefs!!.edit()
            editor.putBoolean("bootIntoChooser", value!!)
            editor.apply()
        }
}

internal class MyWebViewClient(private val mAssetLoader: WebViewAssetLoader, private var mDevelMode: AtomicBoolean) : WebViewClient() {
    override fun shouldInterceptRequest(view: WebView,
                                        request: WebResourceRequest): WebResourceResponse? {
        return mAssetLoader.shouldInterceptRequest(request.url)
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        // Only during development!
        val e = error.primaryError
        if ((mDevelMode.get() || BuildConfig.DEBUG) && ((e == SslError.SSL_UNTRUSTED) || (e == SSL_EXPIRED))) {
            if (e == SslError.SSL_UNTRUSTED) {
                Log.d("MainActivity", "Untrusted SSL cert - accepting anyway")
            }
            if (e == SSL_EXPIRED) {
                Log.d("MainActivity", "Expired SSL cert - accepting anyway")
            }
            handler.proceed()
        } else {
            Log.d("MainActivity", "SSL error, rejecting: " + error.primaryError)
            handler.cancel()
        }
    }
}