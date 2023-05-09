package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.Post
import java.util.concurrent.TimeUnit

const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val client = OkHttpClient.Builder()
    .addInterceptor(logging)
    .connectTimeout(30, TimeUnit.SECONDS)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface PostApiService {

    @GET("posts")
    fun getAll(): Call<List<Post>>

    @GET("posts/{id}")
    fun getById(@Path("id")id: Long): Call<Post>

    @DELETE("posts/{id}")
    fun removeById(@Path("id")id: Long): Call<Unit>

    @POST("posts/{id}/likes")
    fun likeById(@Path("id")id: Long): Call<Post>

    @DELETE("posts/{id}/likes")
    fun deleteLikeById(@Path("id")id: Long): Call<Post>

    @POST("posts")
    fun save(@Body post: Post): Call<Post>

}

object PostApi {
    val retrofitService: PostApiService by lazy {
        retrofit.create()
    }
}
