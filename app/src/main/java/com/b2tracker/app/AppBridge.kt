package com.b2tracker.app

import android.app.Activity
import android.webkit.JavascriptInterface
import com.b2tracker.app.core.BridgeLogic

/**
 * الجسر بين صفحة الـ HTML وأندرويد (AndroidBridge في JavaScript).
 * كل الدوال بترجّع JSON: { ok, code, message, data? } — بنتيجة حقيقية مش تخمين.
 */
class AppBridge(activity: Activity) {

    private val logic = BridgeLogic(AndroidSystemActions(activity))

    @JavascriptInterface
    fun hasilteePackage(): String = BuildConfig.HASILTEE_PACKAGE

    @JavascriptInterface
    fun listApps(): String = logic.listApps().toJson()

    @JavascriptInterface
    fun openApp(packageName: String?): String = logic.openApp(packageName).toJson()

    @JavascriptInterface
    fun shareToChatGPT(text: String?): String = logic.shareToChatGPT(text).toJson()

    @JavascriptInterface
    fun copyText(text: String?): String = logic.copyText(text).toJson()

    @JavascriptInterface
    fun shareBackup(fileName: String?, json: String?): String = logic.shareBackup(fileName, json).toJson()
}
