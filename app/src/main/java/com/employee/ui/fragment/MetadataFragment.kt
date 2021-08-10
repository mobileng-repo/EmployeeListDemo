package com.employee.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.employee.R
import com.employee.adapter.UserPostListAdapter
import com.employee.databinding.FragmentMetadataBinding
import com.employee.model.userPostModel.DataItem
import com.employee.model.userPostModel.UserPostResponse
import com.employee.retrofit.ApiInterface
import com.employee.retrofit.RetrofitService
import com.employee.utils.AppUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.util.*

class MetadataFragment : Fragment() {
    lateinit var binding: FragmentMetadataBinding
    private var apiInterface: ApiInterface? = null
    var userPostList: ArrayList<DataItem>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMetadataBinding.inflate(inflater, container, false)
        apiInterface = RetrofitService.createService(ApiInterface::class.java)
        return binding.root
    }

    /*Display Users Receiving Data*/
    fun displayReceivedData(
        name: String?,
        id: Int,
        gender: String?,
        email: String?,
        status: String?,
        createdAt: String?,
        updatedAt: String?
    ) {
        binding.apply {
            llUserInfo.visibility = View.VISIBLE
            tvNoUserSelected.visibility = View.GONE
            tvName.text = name
            tvGender.text = gender
            tvEmail.text = email
            tvStatus.text = status
            tvCreatedDate.text = createdAt
            tvUpdatedDate.text = updatedAt
        }

        //Todo:: Get All PostList from this API Call....
        getPostListAPICall(id)
    }

    //Todo:: Get All PostList from this API Call....
    private fun getPostListAPICall(id: Int) {
        if (activity?.let { AppUtils.isConnectedToInternet() }!!) {
            val call: Call<UserPostResponse> = apiInterface!!.getAllPostListAPICall(id)
            call.enqueue(object : Callback<UserPostResponse?> {
                override fun onResponse(
                    call: Call<UserPostResponse?>,
                    response: Response<UserPostResponse?>
                ) {
                    val userPostResponse: UserPostResponse
                    if (response.isSuccessful) {
                        userPostResponse = response.body()!!
                        when (userPostResponse.code) {
                            200 -> {
                                userPostList = userPostResponse.data as ArrayList<DataItem>
                                if (userPostList!!.size > 0) {
                                    val userPostListAdapter =
                                        UserPostListAdapter(context, userPostList!!)
                                    val mLayoutManager1: RecyclerView.LayoutManager =
                                        LinearLayoutManager(activity)

                                    binding.apply {
                                        rvPostList.layoutManager = mLayoutManager1
                                        rvPostList.adapter = userPostListAdapter
                                        tvNoDataFound.visibility = View.GONE
                                        rvPostList.visibility = View.VISIBLE
                                    }
                                } else {
                                    binding.apply {
                                        tvNoDataFound.visibility = View.VISIBLE
                                        rvPostList.visibility = View.GONE
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(activity, response.message(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserPostResponse?>, t: Throwable) {
                    if (t is SocketTimeoutException) {
                        Toast.makeText(
                            activity,
                            getString(R.string.connection_timeout),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        t.printStackTrace()
                        Toast.makeText(
                            activity,
                            getString(R.string.something_went_wrong),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
        } else {
            Toast.makeText(activity, getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
        }
    }
}