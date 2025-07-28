package top.ltfan.notdeveloper.detection

import android.content.Context
import androidx.annotation.StringRes

abstract class DetectionMethod(
    val preferenceKey: String,
    @param:StringRes val nameId: Int
) {
    abstract fun test(context: Context): Boolean
}
