package com.b2tracker.app.core

/** فشل عملية على مستوى النظام — code بيوصل للصفحة عشان تعرض سبب حقيقي. */
class ActionException(val code: String, message: String, cause: Throwable? = null) :
    Exception(message, cause)

data class AppInfo(val label: String, val pkg: String)

/** كل اللي التطبيق بيطلبه من أندرويد. أي دالة بترمي ActionException لو العملية فشلت فعلاً. */
interface SystemActions {
    fun canLaunch(pkg: String): Boolean
    fun launch(pkg: String)
    fun shareText(targetPkg: String?, text: String)
    fun copyToClipboard(text: String)
    fun shareFile(fileName: String, mime: String, content: String)
    fun launchableApps(): List<AppInfo>
}
