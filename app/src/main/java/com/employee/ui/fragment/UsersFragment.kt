package com.employee.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.employee.R
import com.employee.adapter.AllUserListAdapter
import com.employee.databinding.FragmentUsersBinding
import com.employee.model.userListModel.DataItem
import com.employee.model.userListModel.GetAllUserResponse
import com.employee.retrofit.ApiInterface
import com.employee.retrofit.RestConstant
import com.employee.retrofit.RetrofitService
import com.employee.room.AppDatabase
import com.employee.room.ListRoom
import com.employee.ui.dialog.CreateUserDialog
import com.employee.utils.AppUtils
import com.employee.viewModel.UserViewModel
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException

class UsersFragment : Fragment() {
    private lateinit var binding: FragmentUsersBinding
    private var allUserList: ArrayList<DataItem> = ArrayList()
    private var allUserListAdapter: AllUserListAdapter? = null
    private var userViewModel: UserViewModel? = null
    private var createUserDialog: CreateUserDialog? = null
    private lateinit var selectedGender: String
    private lateinit var selectedStatus: String

    //For Room Database
    private var allDataList: List<ListRoom>? = null
    private lateinit var listData: ListRoom

    /*For Interface*/
    private var apiInterface: ApiInterface? = null

    /*For Sending Data to another Fragment or Activity*/
    var SM: SendUserData? = null

    @SuppressLint("UseRequireInsteadOfGet", "FragmentLiveDataObserve")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUsersBinding.inflate(inflater, container, false)

        //Initialization
        createUserDialog = CreateUserDialog(context)
        apiInterface = RetrofitService.createService(ApiInterface::class.java)
        listData = ListRoom()
        allDataList = ArrayList()

        binding.rgOrder.setOnCheckedChangeListener { _, i ->
            when (i) {
                R.id.rb_normal -> {
                    /*Get Normal Data*/
                    getList()
                }
                R.id.rb_asc -> {
                    /*Get ASC Ordered data by Name*/
                    getASCList()
                }
                R.id.rb_active -> {
                    /*Get Active User Data*/
                    getActiveUserList()
                }
            }
        }

        /*Check Internet Connection*/
        if (AppUtils.isConnectedToInternet()) {
            userViewModel = ViewModelProviders.of(this).get(UserViewModel::class.java)
            userViewModel!!.init()
            userViewModel!!.allUserRepository()!!
                .observe(this, { userListResponse: GetAllUserResponse? ->
                    val dataItem = userListResponse!!.data
                    allUserList.clear()
                    allUserList.addAll(dataItem!!)
                    for (i in 0 until allUserList.size) {
                        checkEmailDuplication(
                            allUserList[i].name!!,
                            allUserList[i].email!!,
                            allUserList[i].gender!!,
                            allUserList[i].status!!,
                            allUserList[i].createdAt,
                            allUserList[i].updatedAt
                        )
                    }
                })
        } else {
            /*Get Local Storage data when Internet Connection is not available*/
            getList()
        }

        /*Create User Dialog Button*/
        binding.btnAddUser.setOnClickListener {
            createUserDialog?.show()
            createUserDialog?.etEmailId!!.setText("")
            createUserDialog?.etName!!.setText("")

            /*Create User Button OnClick Method to insert a new user*/
            createUserDialog?.btnCreateUser!!.setOnClickListener {
                if (createUserValidation()) {
                    /*Check Gender is selected Or Not*/
                    if (createUserDialog!!.rgGender!!.checkedRadioButtonId == -1) {
                        Toast.makeText(
                            activity,
                            getString(R.string.select_gender),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedGender = if (createUserDialog!!.rbMale!!.isChecked) {
                            "Male"
                        } else {
                            "Female"
                        }
                    }

                    /*Check Status is selected Or Not*/
                    if (createUserDialog!!.rgStatus!!.checkedRadioButtonId == -1) {
                        Toast.makeText(
                            activity,
                            getString(R.string.select_status),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        selectedStatus = if (createUserDialog!!.rbActive!!.isChecked) {
                            "Active"
                        } else {
                            "InActive"
                        }
                    }

                    if (createUserDialog!!.rbMale!!.isChecked || createUserDialog!!.rbFemale!!.isChecked &&
                        createUserDialog!!.rbActive!!.isChecked || createUserDialog!!.rbInactive!!.isChecked
                    ) {
                        /*Create user API Call*/
                        createUserApiCall()
                    }
                }
            }
        }
        return binding.root
    }

    /*Validation for Insert New User Data*/
    private fun createUserValidation(): Boolean {
        return when {
            TextUtils.isEmpty(AppUtils.getText(createUserDialog!!.etName!!)) -> {
                Toast.makeText(activity, getString(R.string.please_enter_name), Toast.LENGTH_SHORT)
                    .show()
                false
            }
            TextUtils.isEmpty(AppUtils.getText(createUserDialog!!.etEmailId!!)) -> {
                Toast.makeText(activity, getString(R.string.please_enter_email), Toast.LENGTH_SHORT)
                    .show()
                false
            }
            !AppUtils.isEmailValid(AppUtils.getText(createUserDialog!!.etEmailId!!)) -> {
                Toast.makeText(
                    activity,
                    getString(R.string.please_enter_valid_email),
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
            else -> {
                true
            }
        }
    }

    /*Setup Recycler View here*/
    private fun setupRecyclerView(dataList: List<ListRoom>?) {
        if (dataList!!.isNotEmpty()) {
            allUserListAdapter = AllUserListAdapter(context, dataList, this)
            binding.rvAllUserList.layoutManager = LinearLayoutManager(context)
            binding.rvAllUserList.adapter = allUserListAdapter
            binding.rvAllUserList.itemAnimator = DefaultItemAnimator()
            binding.rvAllUserList.isNestedScrollingEnabled = true
            allUserListAdapter!!.notifyDataSetChanged()
            binding.rvAllUserList.visibility = View.VISIBLE
            binding.tvNoDataFound.visibility = View.GONE
        } else {
            binding.rvAllUserList.visibility = View.GONE
            binding.tvNoDataFound.visibility = View.VISIBLE
        }
    }

    //Insert Data in Database
    fun insertDataList(
        name: String,
        email: String,
        gender: String,
        status: String,
        created_at: String,
        updated_at: String
    ) {
        @SuppressLint("StaticFieldLeak")
        class InsertData : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                listData.name = name
                listData.email = email
                listData.status = status
                listData.gender = gender
                listData.createdAt = created_at
                listData.updatedAt = updated_at

                //adding to database
                AppDatabase.getInstance(context!!)
                    .listDao()
                    ?.insert(listData)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                /*Get All Local Storage data*/
                getList()
            }
        }

        val st = InsertData()
        st.execute()
    }

    /*Get All Stored Data From the Room database*/
    fun getList() {
        @SuppressLint("StaticFieldLeak")
        class GetList : AsyncTask<Void?, Void?, List<ListRoom>?>() {
            override fun doInBackground(vararg p0: Void?): List<ListRoom>? {
                allDataList = AppDatabase
                    .getInstance(context!!)
                    .listDao()
                    ?.all as List<ListRoom>?
                return allDataList
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(datalist: List<ListRoom>?) {
                super.onPostExecute(datalist)
                setupRecyclerView(datalist)
            }
        }

        val gt = GetList()
        gt.execute()
    }

    /*Get ASC Data From the Room database*/
    private fun getASCList() {
        @SuppressLint("StaticFieldLeak")
        class GetList : AsyncTask<Void?, Void?, List<ListRoom>?>() {
            override fun doInBackground(vararg p0: Void?): List<ListRoom>? {
                allDataList = AppDatabase
                    .getInstance(context!!)
                    .listDao()
                    ?.getPersonsSortByAscName() as List<ListRoom>?
                return allDataList
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(datalist: List<ListRoom>?) {
                super.onPostExecute(datalist)
                setupRecyclerView(datalist)
            }
        }

        val gt = GetList()
        gt.execute()
    }

    /*Get Active User Data From the Room database*/
    private fun getActiveUserList() {
        @SuppressLint("StaticFieldLeak")
        class GetList : AsyncTask<Void?, Void?, List<ListRoom>?>() {
            override fun doInBackground(vararg p0: Void?): List<ListRoom>? {
                allDataList = AppDatabase
                    .getInstance(context!!)
                    .listDao()
                    ?.getSortByStatus("active") as List<ListRoom>?
                return allDataList
            }

            @SuppressLint("SetTextI18n")
            override fun onPostExecute(datalist: List<ListRoom>?) {
                super.onPostExecute(datalist)
                setupRecyclerView(datalist)
            }
        }

        val gt = GetList()
        gt.execute()
    }

    /*Delete User Data From the Room Database*/
    fun deleteListData(email: String, id: Int) {
        @SuppressLint("StaticFieldLeak")
        class DeleteTask : AsyncTask<Void?, Void?, Void?>() {
            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
                getList()
            }

            override fun doInBackground(vararg p0: Void?): Void? {
                try {
                    AppDatabase.getInstance(context!!)
                        .listDao()
                        ?.deleteId(email)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }
        }

        val dt = DeleteTask()
        dt.execute()
    }

    /*Check data duplication using Email ID*/
    fun checkEmailDuplication(
        name: String,
        email: String,
        gender: String,
        status: String,
        created_at: String,
        updated_at: String
    ) {
        @SuppressLint("StaticFieldLeak")
        class CheckValidation : AsyncTask<Void?, Void?, ListRoom?>() {
            override fun onPostExecute(data: ListRoom?) {
                super.onPostExecute(data)
                if (data != null) {
                    if (!data.email.equals(email)) {
                        insertDataList(name, email, gender, status, created_at, updated_at)
                    } else {
                        getList()
                    }
                } else {
                    insertDataList(name, email, gender, status, created_at, updated_at)
                }
            }

            override fun doInBackground(vararg p0: Void?): ListRoom? {
                return AppDatabase.getInstance(context!!)
                    .listDao()
                    ?.emailValidation(email)
            }
        }

        val st = CheckValidation()
        st.execute()
    }

    /**
     * Create User Data API Call
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun createUserApiCall() {
        try {
            if (AppUtils.isConnectedToInternet()) {
                val params = JSONObject()
                try {
                    params.put("name", AppUtils.getText(createUserDialog!!.etName!!))
                    params.put("email", AppUtils.getText(createUserDialog!!.etEmailId!!))
                    params.put("gender", selectedGender)
                    params.put("status", selectedStatus)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                val param = JsonParser.parseString(params.toString()) as JsonObject
                val call: Call<JsonObject> = apiInterface!!.createUserAPICall(param, RestConstant.TOKEN)
                call.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {
                        if (response.isSuccessful) {
                            val json = JSONObject(response.body().toString())
                            val code = json.getString("code")
                            if (code.equals("422")) {
                                Toast.makeText(activity, "Email Already Added", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                val jsonData = json.getJSONObject("data")
                                val name = jsonData.getString("name")
                                val email = jsonData.getString("email")
                                val gender = jsonData.getString("gender")
                                val status = jsonData.getString("status")
                                val createdAt = "2021-05-28T03:50:04.006+05:30"
                                val updatedAt = "2021-05-28T03:50:04.006+05:30"

                                /*Inserted data add in room database*/
                                checkEmailDuplication(
                                    name,
                                    email,
                                    gender,
                                    status,
                                    createdAt,
                                    updatedAt
                                )
                                createUserDialog!!.dismiss()
                                Toast.makeText(activity, "Successfully added", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        onFailureCall(activity, t)
                    }
                })
            } else {
                /*Inserted data in room database when Internet is not available*/
                checkEmailDuplication(
                    AppUtils.getText(createUserDialog!!.etName!!),
                    AppUtils.getText(createUserDialog!!.etEmailId!!),
                    selectedGender,
                    selectedStatus,
                    "2021-05-28T03:50:04.006+05:30",
                    "2021-05-28T03:50:04.006+05:30"
                )
                createUserDialog!!.dismiss()
                Toast.makeText(activity, "Successfully added", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onFailureCall(ctx: Context?, t: Throwable) {
        Toast.makeText(ctx, t.message, Toast.LENGTH_SHORT).show()
        try {
            if (t is SocketTimeoutException) {
                Toast.makeText(ctx, getString(R.string.connection_timeout), Toast.LENGTH_SHORT)
                    .show()
            } else {
                t.printStackTrace()
                Toast.makeText(ctx, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    /*Delete record Alert Dialog*/
    fun deleteRecord(email: String, id: Int) {
        AlertDialog.Builder(activity)
            .setMessage(getString(R.string.are_you_sure_delete))
            .setPositiveButton(getString(R.string.yes)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                deleteUserApiCall(email, id)
            }
            .setNegativeButton(getString(R.string.no)) { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
    }

    /**
     * DELETE USER API CALL
     */
    @SuppressLint("UseRequireInsteadOfGet")
    private fun deleteUserApiCall(email: String, id: Int) {
        try {
            if (AppUtils.isConnectedToInternet()) {
                val call: Call<JsonObject> =
                    apiInterface!!.deleteUserAPICall(id, RestConstant.TOKEN)
                call.enqueue(object : Callback<JsonObject?> {
                    override fun onResponse(
                        call: Call<JsonObject?>,
                        response: Response<JsonObject?>
                    ) {
                        if (response.isSuccessful) {
                            val json = JSONObject(response.body().toString())
                            val code = json.getString("code")
                            if (code.equals("204")) {
                                Toast.makeText(
                                    activity,
                                    "Delete User Successfully",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                /*Deleted data in room database when Internet is not available*/
                                deleteListData(email, id)
                            } else {
                                /*Deleted data from only room database*/
                                deleteListData(email, id)
                            }
                        }
                    }

                    override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                        onFailureCall(activity, t)
                    }
                })
            } else {
                /*Deleted data in room database when Internet is not available*/
                deleteListData(email, id)
                Toast.makeText(activity, "Deleted Successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*Click On Any Users*/
    @SuppressLint("UseRequireInsteadOfGet")
    fun selectUser(
        id: Int,
        name: String?,
        gender: String?,
        email: String?,
        status: String?,
        createdAt: String?,
        updatedAt: String?
    ) {
        /*Send Data to another Fragment*/
        SM!!.sendData(id, name, gender, email, status, createdAt, updatedAt)
        /*Change Viewpager Fragment on click*/
        val i1 = Intent("USER_CLICK")
        LocalBroadcastManager.getInstance(activity!!).sendBroadcast(i1)
    }

    /*Interface For Sending Data to Another Screen*/
    interface SendUserData {
        fun sendData(
            id: Int,
            name: String?,
            gender: String?,
            email: String?,
            status: String?,
            createdAt: String?,
            updatedAt: String?
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            SM = activity as SendUserData?
        } catch (e: ClassCastException) {
            throw ClassCastException("Error in retrieving data. Please try again")
        }
    }
}