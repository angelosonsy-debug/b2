package com.b2tracker.app.core

/**
 * منطق الأزرار — من غير أي كلاس أندرويد، عشان يتختبر على JVM.
 * القاعدة: ما نرجّعش نجاح إلا لو SystemActions ما رمتش استثناء.
 */
class BridgeLogic(private val actions: SystemActions) {

    companion object {
        const val MAX_BACKUP_CHARS = 5_000_000
    }

    fun listApps(): BridgeResult {
        return try {
            val sorted = AppList.sorted(actions.launchableApps())
            BridgeResult.success("ok", "", AppList.toJson(sorted))
        } catch (e: ActionException) {
            BridgeResult.failure(e.code, e.message ?: e.code)
        } catch (e: Exception) {
            BridgeResult.failure("unexpected", e.message ?: "خطأ غير متوقع")
        }
    }

    fun openApp(pkg: String?): BridgeResult {
        val p = pkg?.trim() ?: ""
        if (!PackageNames.isValid(p)) {
            return BridgeResult.failure("invalid_package", "اسم الـ package غير صالح")
        }
        return try {
            if (!actions.canLaunch(p)) {
                BridgeResult.failure("not_installed", "التطبيق ($p) مش مثبت أو مش ظاهر للتطبيق")
            } else {
                actions.launch(p)
                BridgeResult.success("launched", "تم طلب فتح التطبيق")
            }
        } catch (e: ActionException) {
            BridgeResult.failure(e.code, e.message ?: e.code)
        } catch (e: Exception) {
            BridgeResult.failure("unexpected", e.message ?: "خطأ غير متوقع")
        }
    }

    fun shareToChatGPT(text: String?): BridgeResult {
        val safe = PromptText.sanitize(text)
            ?: return BridgeResult.failure("empty_text", "مفيش نص للإرسال")
        try {
            actions.shareText(PackageNames.CHATGPT, safe)
            return BridgeResult.success("chatgpt_app", "اتبعت لتطبيق ChatGPT")
        } catch (e: ActionException) {
            // تطبيق ChatGPT مش متاح — نجرّب قايمة المشاركة
        } catch (e: Exception) {
        }
        return try {
            actions.shareText(null, safe)
            BridgeResult.success("chooser", "تطبيق ChatGPT مش متاح — فتحنا قايمة المشاركة")
        } catch (e: Exception) {
            BridgeResult.failure("share_failed", "ما قدرناش نفتح ChatGPT ولا قايمة المشاركة")
        }
    }

    fun copyText(text: String?): BridgeResult {
        val safe = PromptText.sanitize(text)
            ?: return BridgeResult.failure("empty_text", "مفيش نص للنسخ")
        return try {
            actions.copyToClipboard(safe)
            BridgeResult.success("copied", "اتنسخ")
        } catch (e: ActionException) {
            BridgeResult.failure(e.code, e.message ?: e.code)
        } catch (e: Exception) {
            BridgeResult.failure("unexpected", e.message ?: "النسخ فشل")
        }
    }

    fun shareBackup(fileName: String?, json: String?): BridgeResult {
        val body = json?.takeIf { it.isNotBlank() }
            ?: return BridgeResult.failure("empty_backup", "مفيش بيانات للنسخة الاحتياطية")
        if (body.length > MAX_BACKUP_CHARS) {
            return BridgeResult.failure("too_large", "حجم النسخة الاحتياطية كبير جداً")
        }
        return try {
            actions.shareFile(FileNames.backupName(fileName), "application/json", body)
            BridgeResult.success("shared", "اختار مكان الحفظ")
        } catch (e: ActionException) {
            BridgeResult.failure(e.code, e.message ?: e.code)
        } catch (e: Exception) {
            BridgeResult.failure("unexpected", e.message ?: "فشل إنشاء النسخة")
        }
    }
}
