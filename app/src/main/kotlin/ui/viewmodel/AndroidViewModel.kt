package top.ltfan.notdeveloper.ui.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel

open class AndroidViewModel<T: Application>(val application: T) : ViewModel()
