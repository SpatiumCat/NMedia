package ru.netology.nmedia.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.AuthApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.model.PhotoModel
import java.io.IOException
import java.lang.Exception

class AuthRepositoryImpl : AuthRepository {

    override suspend fun signIn(login: String, password: String) {
        try {
            val response = AuthApi.retrofitService.updateUser(login, password)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setToken(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun register(login: String, password: String, name: String) {
        try {
            val response = AuthApi.retrofitService.registerUser(login, password, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setToken(body)
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
            val response = AuthApi.retrofitService.registerWithPhoto(
                login.toRequestBody("text/plain".toMediaType()),
                password.toRequestBody("text/plain".toMediaType()),
                name.toRequestBody("text/plain".toMediaType()),
                MultipartBody.Part.createFormData("file", "file", avatar.file.asRequestBody())
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setToken(body)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}
