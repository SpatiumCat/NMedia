package ru.netology.nmedia.repository

import android.content.Context
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.AuthApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AuthRepository {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AuthRepositoryImplEntryPoint {
        fun getAuthApiService(): AuthApiService
        fun getAppAuth(): AppAuth
    }
    val entryPoint = EntryPointAccessors.fromApplication(
        context,
        AuthRepositoryImplEntryPoint::class.java
    )

    override suspend fun signIn(login: String, password: String) {
        try {
            val response = entryPoint.getAuthApiService().updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            entryPoint.getAppAuth().setToken(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun register(login: String, password: String, name: String) {
        try {
            val response = entryPoint.getAuthApiService().registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            entryPoint.getAppAuth().setToken(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun registerWithPhoto(
        login: String,
        password: String,
        name: String,
        avatar: PhotoModel
    ) {
        try {
            val response = entryPoint.getAuthApiService().registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData("file", "file", avatar.file.asRequestBody())
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            entryPoint.getAppAuth().setToken(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
