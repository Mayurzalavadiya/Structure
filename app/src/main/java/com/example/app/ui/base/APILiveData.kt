package com.example.app.ui.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.app.data.pojo.DataWrapper
import com.example.app.data.pojo.ResponseBody

class APILiveData<T> : MutableLiveData<DataWrapper<T>>() {

    /**
     *  @param owner : Life Cycle Owner
     *  @param onChange : live data
     *  @param onError : Server and App error -> return true to handle error by base else return false to handle error by your self
     *
     */
    /*fun observe(owner: BaseDialog, onChange: (ResponseBody<T>) -> Unit, onError: (Throwable) -> Boolean = { true }) {
        super.observe(owner, Observer<DataWrapper<T>> {
            if (it?.throwable != null) {
                if (onError(it.throwable)) owner.onError(it.throwable)
            } else if (it?.responseBody != null) {
                onChange(it.responseBody)
            }
        })
    }*/

    /**
     *  @param owner : Life Cycle Owner
     *  @param onChange : live data
     *  @param onError : Server and App error -> return true to handle error by base else return false to handle error by your self
     *
     */
    fun observe(owner: BaseFragment<*>, onChange: (ResponseBody<T>) -> Unit, onError: (Throwable) -> Boolean = { true }) {
        super.observe(owner) {
            if (it?.throwable != null) {
                if (onError(it.throwable)) owner.onError(it.throwable)
            } else if (it?.responseBody != null) {
                onChange(it.responseBody)
            }
        }
    }

    /**
     *  @param owner : Life Cycle Owner
     *  @param onChange : live data
     *  @param onError : Server and App error -> return true to handle error by base else return false to handle error by your self
     *
     */
    fun observe(owner: BaseActivity, onChange: (ResponseBody<T>) -> Unit, onError: (Throwable) -> Boolean = { true }) {
        super.observe(owner, Observer<DataWrapper<T>> {
            if (it.throwable != null) {
                if (onError(it.throwable)) owner.onError(it.throwable)
            } else if (it.responseBody != null) {
                onChange(it.responseBody)
            }
        })
    }
}

/*
fun APILiveData<*>.isSuccess() = this.value?.responseBody?.responseCode == StatusCode.CODE_SUCCESS

fun APILiveData<*>.isNoData() = this.value?.responseBody?.responseCode == StatusCode.CODE_NO_DATA*/
