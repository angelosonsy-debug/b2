package com.b2tracker.app

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.core.content.FileProvider
import com.b2tracker.app.core.ActionException
import com.b2tracker.app.core.AppInfo
import com.b2tracker.app.core.SystemActions
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/** التنفيذ الحقيقي على أندرويد. أي فشل بيتحوّل لـ ActionException بدل ما يتبلع. */
class AndroidSystemActions(private val activity: Activity) : SystemActions {

    private val pm get() = activity.packageManager

    override fun canLaunch(pkg: String): Boolean = pm.getLaunchIntentForPackage(pkg) != null

    override fun launch(pkg: String) {
        val intent = pm.getLaunchIntentForPackage(pkg)
            ?: throw ActionException("not_installed", "التطبيق ($pkg) مش مثبت أو مش ظاهر للتطبيق")
        onUi { start(intent) }
    }

    override fun shareText(targetPkg: String?, text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        if (targetPkg != null) {
            send.setPackage(targetPkg)
            onUi { start(send) }
        } else {
            onUi { start(Intent.createChooser(send, null)) }
        }
    }

    override fun copyToClipboard(text: String) {
        val cm = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            ?: throw ActionException("no_clipboard", "خدمة النسخ مش متاحة")
        onUi { cm.setPrimaryClip(ClipData.newPlainText("prompt", text)) }
    }

    override fun shareFile(fileName: String, mime: String, content: String) {
        val dir = File(activity.cacheDir, "backups")
        if (!dir.exists() && !dir.mkdirs()) {
            throw ActionException("io", "فشل إنشاء مجلد النسخ الاحتياطية")
        }
        val file = File(dir, fileName)
        try {
            file.writeText(content, Charsets.UTF_8)
        } catch (e: IOException) {
            throw ActionException("io", "فشل كتابة ملف النسخة الاحتياطية", e)
        }
        val uri = try {
            FileProvider.getUriForFile(activity, activity.packageName + ".fileprovider", file)
        } catch (e: IllegalArgumentException) {
            throw ActionException("file_provider", "فشل تجهيز الملف للمشاركة", e)
        }
        val send = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("backup", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        onUi { start(Intent.createChooser(send, null)) }
    }

    override fun launchableApps(): List<AppInfo> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val found = pm.queryIntentActivities(query, 0)
        val apps = ArrayList<AppInfo>()
        for (ri in found) {
            val pkg = ri.activityInfo?.packageName ?: continue
            if (pkg == activity.packageName) continue
            apps.add(AppInfo(ri.loadLabel(pm).toString(), pkg))
        }
        return apps.distinctBy { it.pkg }
    }

    private fun start(intent: Intent) {
        try {
            activity.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            throw ActionException("not_found", "مفيش تطبيق يقدر ينفّذ الطلب", e)
        } catch (e: SecurityException) {
            throw ActionException("security", "النظام رفض فتح التطبيق", e)
        }
    }

    /** بينفّذ على الـ UI thread ويستنى النتيجة الحقيقية (أو الاستثناء). */
    private fun <T> onUi(block: () -> T): T {
        if (Looper.myLooper() == Looper.getMainLooper()) return block()
        val task = FutureTask<T>(Callable { block() })
        activity.runOnUiThread(task)
        try {
            return task.get(4, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            throw e.cause ?: e
        } catch (e: TimeoutException) {
            throw ActionException("timeout", "النظام اتأخر في الرد", e)
        }
    }
}
