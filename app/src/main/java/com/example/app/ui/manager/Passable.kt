package com.example.app.ui.manager

interface Passable<in T> {
    fun passData(t: T)
}
