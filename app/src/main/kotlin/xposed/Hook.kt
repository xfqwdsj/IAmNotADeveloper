package top.ltfan.notdeveloper.xposed

import androidx.annotation.Keep
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import top.ltfan.notdeveloper.BuildConfig
import top.ltfan.notdeveloper.detection.DetectionCategory

@Keep
class Hook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName.startsWith("android") || lpparam.packageName.startsWith("com.android")) {
            return
        }

        Log.d("processing package ${lpparam.packageName}")

        if (lpparam.packageName == BuildConfig.APPLICATION_ID) {
            XposedHelpers.findAndHookMethod(
                "${BuildConfig.APPLICATION_ID}.xposed.ModuleStatusKt",
                lpparam.classLoader,
                "getStatusIsModuleActivated",
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.result = true
                    }
                },
            )
        }

        val prefs = XSharedPreferences(BuildConfig.APPLICATION_ID)

        // 应用所有检测方法的hook逻辑，现在使用统一的基类扩展方法
        DetectionCategory.allMethods.forEach { method ->
            try {
                method.hook(prefs, lpparam)
                Log.d("Applied hook for ${method::class.simpleName}")
            } catch (e: Exception) {
                Log.w("Failed to apply hook for ${method::class.simpleName}: ${e.message}")
            }
        }
    }
}
