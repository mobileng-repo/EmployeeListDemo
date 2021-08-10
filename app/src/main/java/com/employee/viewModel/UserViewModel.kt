package com.employee.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.employee.model.userListModel.GetAllUserResponse
import com.employee.repository.UserListRepository
import com.employee.repository.UserListRepository.Companion.instance

class UserViewModel : ViewModel() {
    private var mutableLiveData: MutableLiveData<GetAllUserResponse?>? = null
    private var userListRepository: UserListRepository? = null

    fun init() {
        if (mutableLiveData != null) {
            return
        }
        userListRepository = instance
        mutableLiveData = userListRepository?.allUserList()
    }

    fun allUserRepository(): MutableLiveData<GetAllUserResponse?>?{
        return mutableLiveData
    }
}