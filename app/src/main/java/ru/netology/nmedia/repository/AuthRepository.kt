package ru.netology.nmedia.repository

interface AuthRepository {

    suspend fun signIn(login: String, password: String)
    suspend fun register(login: String, password: String, name: String)
}
