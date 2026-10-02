package com.b2tracker.app

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.webkit.WebViewAssetLoader
import com.b2tracker.app.core.ErrorPage
import com.b2tracker.app.core.LinkAction
import com.b2tracker.app.core.LinkPolicy

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var fileCallback: ValueCallback<Array<Uri>>? = null

    // اختيار ملف النسخة الاحتياطية (زرار "استيراد" في الصفحة)
    private val pickFile =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            fileCallback?.onReceiveValue(if (uri != null) arrayOf(uri) else null)
            fileCallback = null
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        // debugging من كروم (chrome://inspect) في نسخة الـ debug بس
        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        // بيقدّم ملفات assets على عنوان https آمن: التخزين المحلي (localStorage) بيفضل ثابت
        val assetLoader = WebViewAssetLoader.Builder()
            .setDomain(LinkPolicy.APP_HOST)
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = false
            allowContentAccess = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request?.url ?: return null
                return assetLoader.shouldInterceptRequest(uri)
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = route(request?.url?.toString())

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val detail = error?.description?.toString()
                Log.w(TAG, "load error: ${request?.url} -> $detail")
                if (request?.isForMainFrame == true) {
                    webView.loadDataWithBaseURL(
                        null, ErrorPage.build(detail), "text/html", "utf-8", null
                    )
                }
            }

            override fun onReceivedHttpError(
                view: WebView?,
                request: WebResourceRequest?,
                errorResponse: WebResourceResponse?
            ) {
                Log.w(TAG, "http ${errorResponse?.statusCode}: ${request?.url}")
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                fileCallback?.onReceiveValue(null)
                fileCallback = filePathCallback
                pickFile.launch("*/*")
                return true
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(
                    TAG,
                    "JS: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})"
                )
                return true
            }
        }

        webView.addJavascriptInterface(AppBridge(this), "AndroidBridge")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })

        webView.loadUrl(LinkPolicy.START_URL)
    }

    /** true = إحنا اتعاملنا مع الرابط (الـ WebView ما يفتحوش). */
    private fun route(url: String?): Boolean {
        return when (val action = LinkPolicy.decide(url)) {
            is LinkAction.LoadInApp -> false
            is LinkAction.OpenExternal -> {
                openExternal(action.url)
                true
            }
            is LinkAction.Block -> true
        }
    }

    private fun openExternal(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "no app can open $url")
        }
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }

    private companion object {
        const val TAG = "B2Tracker"
    }
}
