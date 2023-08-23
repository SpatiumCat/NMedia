package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.repository.AuthRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

class RegistrationViewModel: ViewModel() {

    private val repository = AuthRepositoryImpl()

    private var _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState> get() = _dataState

    private val _registrated = SingleLiveEvent<Unit>()
    val registrated: LiveData<Unit> get() = _registrated

    fun registerUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                _dataState.value = FeedModelState(loading = true)
                repository.register(login, password, name)
                _registrated.value = Unit
                _dataState.value = FeedModelState()
            } catch (e: Exception){
                _dataState.value = FeedModelState(error = true)
            }

        }
    }


}
