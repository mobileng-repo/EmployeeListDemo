package com.employee.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.TextView
import java.util.regex.Pattern

object AppUtils {
    fun isEmailValid(email: String): Boolean {
        val expression = "^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(email)
        return matcher.matches()
    }

    @JvmStatic
    fun getText(textView: TextView): String {
        return textView.text.toString().trim { it <= ' ' }
    }

    @JvmStatic
    fun isConnectedToInternet(): Boolean {
        val cm = EmployeeDemoApp.employeeDemoApp!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo: NetworkInfo? = cm.activeNetworkInfo
        return netInfo != null && netInfo.isConnected && netInfo.isAvailable
    }
}