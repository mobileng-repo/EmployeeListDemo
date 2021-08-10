package com.employee.utils

import android.annotation.SuppressLint
import android.app.Application

class EmployeeDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        employeeDemoApp = this
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @JvmStatic
        var employeeDemoApp: EmployeeDemoApp? = null
            private set
    }
}