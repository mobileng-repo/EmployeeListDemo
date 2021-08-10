package com.employee.model.userListModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetAllUserResponse(
	@field:SerializedName("data")
	val data: List<DataItem>? = null
): Parcelable