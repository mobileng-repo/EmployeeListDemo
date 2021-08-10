package com.employee.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity
class ListRoom : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var name: String? = null
    var email: String? = null
    var gender: String? = null
    var status: String? = null
    @SerializedName("created_at")
    var createdAt: String? = null
    @SerializedName("updated_at")
    var updatedAt: String? = null
}