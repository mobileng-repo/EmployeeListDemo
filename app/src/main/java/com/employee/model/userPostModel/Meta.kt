package com.employee.model.userPostModel

import com.google.gson.annotations.SerializedName

data class Meta(

	@field:SerializedName("pagination")
	val pagination: Pagination? = null
)