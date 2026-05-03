package com.example.app.exception

class ApplicationException(override val message: String) : Throwable() {
    var type: Type? = null


    enum class Type {
        NO_INTERNET, NO_DATA, VALIDATION
    }
}
