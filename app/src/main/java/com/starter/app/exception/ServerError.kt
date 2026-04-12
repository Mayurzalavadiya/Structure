package com.starter.app.exception

import okio.IOException

class ServerError : IOException {

    private val errorBody: String

    constructor(message: String) : super(message) {
        errorBody = ""
    }

    constructor(message: String, errorBody: String) : super(message) {
        this.errorBody = errorBody
    }
}
