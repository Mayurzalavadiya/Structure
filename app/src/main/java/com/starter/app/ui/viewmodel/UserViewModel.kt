package com.starter.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.starter.app.data.dao.UserDao
import com.starter.app.data.pojo.dataclass.Event
import com.starter.app.data.pojo.dataclass.UserEntity
import com.starter.app.data.pojo.response.UsersResponse
import com.starter.app.data.repository.UserRepository
import com.starter.app.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userDao: UserDao,
    private val userRepository: UserRepository
) : BaseViewModel() {

    private var _loadingState = MutableStateFlow(false)
    val loadingState = _loadingState.asStateFlow()

    val userLiveData = MutableLiveData<UsersResponse>()

    fun getUser(limit: Int, skip: Int) = launch {
        _loadingState.value = true
        try {
            val result = userRepository.getUser(limit, skip)
            userLiveData.value = result
            saveUsersToDb(result.users?.filterNotNull().orEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _loadingState.value = false
        }
    }

    private suspend fun saveUsersToDb(users: List<UsersResponse.User>) {
        val userEntities = users.map {
            UserEntity(
                id = it.id ?: 0,
                firstName = it.firstName,
                lastName = it.lastName,
                email = it.email,
                image = it.image
            )
        }
        userDao.insertUsers(userEntities) // 🚨 This is blindly inserting
    }

    private val _cachedUsers = MutableLiveData<List<UsersResponse.User>>()
    val cachedUsers: LiveData<List<UsersResponse.User>> get() = _cachedUsers

    fun getCachedUsers() {
        launch {
            val localUsers = userDao.getAllUsers()
            val mappedUsers = localUsers.map {
                UsersResponse.User(
                    id = it.id,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                    image = it.image
                )
            }
            _cachedUsers.postValue(mappedUsers)
        }
    }

}
