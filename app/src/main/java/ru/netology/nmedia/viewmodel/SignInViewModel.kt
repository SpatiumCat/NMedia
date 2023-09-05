package ru.netology.nmedia.viewmodel

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.AuthRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

class SignInViewModel : ViewModel() {

    private val repository = AuthRepositoryImpl()

    private var _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState> get() = _dataState

    val dataAuth: LiveData<Token?> = AppAuth.getInstance().data.asLiveData()

    private val _authorized = SingleLiveEvent<Unit>()
    val authorized: LiveData<Unit> get() = _authorized


    fun signIn(login: String, password: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.signIn(login, password)
                _authorized.value = Unit
                _dataState.value = FeedModelState()
            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }

    fun signOut(){
    }

}
