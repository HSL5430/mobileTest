package cn.accenture.mobiletest

import android.app.Application

class App : Application() {

    override fun onCreate() {
        app = this
        super.onCreate()
    }

    companion object {
        private lateinit var app: App

        fun getInstance() = app
    }
}
