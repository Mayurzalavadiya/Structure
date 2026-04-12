package com.starter.app.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            block(this)
        }
    }
}
