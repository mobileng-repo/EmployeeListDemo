package com.employee.retrofit

import com.google.gson.JsonObject
import com.employee.model.userListModel.GetAllUserResponse
import com.employee.model.userPostModel.UserPostResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {
    /*1) Get All User Data API*/
    @GET(RestConstant.GET_ALL_USERS)
    fun getAllUserListAPICall(): Call<GetAllUserResponse?>

    /*2) Create User API*/
    @Headers("Accept: application/json", "Content-Type: application/json")
    @POST(RestConstant.GET_ALL_USERS)
    fun createUserAPICall(@Body body: JsonObject?, @Header("Authorization") authHeader: String?): Call<JsonObject>

    /*3) Delete User API*/
    @DELETE(RestConstant.DELETE_USER)
    fun deleteUserAPICall(@Path("id") itemId: Int?, @Header("Authorization") authHeader: String?): Call<JsonObject>

    /*4) get Post List API*/
    @GET(RestConstant.USER_POST)
    fun getAllPostListAPICall(@Path("id") itemId: Int?): Call<UserPostResponse>
}