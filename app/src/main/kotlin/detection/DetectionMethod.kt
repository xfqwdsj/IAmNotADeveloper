package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes

/**
 * A single way of detecting that developer options are enabled.
 *
 * The hierarchy is sealed, so the compiler requires a
 * [top.ltfan.notdeveloper.xposed.hook.Hook] for every detection method.
 */
sealed class DetectionMethod(
    val preferenceKey: String,
    @param:StringRes val nameId: Int
) {
    abstract fun test(context: Context): Boolean
}
