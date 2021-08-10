package com.employee.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioGroup
import androidx.appcompat.widget.*
import com.employee.R

class CreateUserDialog(context: Context?) : Dialog(context!!) {
    var etName: AppCompatEditText? = null
    var etEmailId: AppCompatEditText? = null
    var rbMale: AppCompatRadioButton? = null
    var rbFemale: AppCompatRadioButton? = null
    var rbActive: AppCompatRadioButton? = null
    var rbInactive: AppCompatRadioButton? = null
    var rgStatus: RadioGroup? = null
    var rgGender: RadioGroup? = null
    var btnCreateUser: AppCompatButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_create_user)

        val v = (window)?.decorView
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        window!!.setGravity(Gravity.CENTER)
        v?.setBackgroundResource(R.color.transparent)
        setCanceledOnTouchOutside(false)
        setCancelable(true)

        v?.setOnClickListener {
            this.dismiss()
        }

        etName = findViewById(R.id.et_name)
        etEmailId = findViewById(R.id.et_emailID)
        rbMale = findViewById(R.id.rb_male)
        rbFemale = findViewById(R.id.rb_female)
        rbActive = findViewById(R.id.rb_active)
        rbInactive = findViewById(R.id.rb_inactive)
        rgGender = findViewById(R.id.rg_gender)
        rgStatus = findViewById(R.id.rg_status)
        btnCreateUser = findViewById(R.id.btn_createUser)
    }
}