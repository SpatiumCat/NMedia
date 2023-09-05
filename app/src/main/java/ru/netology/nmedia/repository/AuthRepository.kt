package ru.netology.nmedia.repository

import ru.netology.nmedia.model.PhotoModel

interface AuthRepository {

    suspend fun signIn(login: String, password: String)
    suspend fun register(login: String, password: String, name: String)
    suspend fun registerWithPhoto(login: String, password: String, name: String, avatar: PhotoModel)
}
