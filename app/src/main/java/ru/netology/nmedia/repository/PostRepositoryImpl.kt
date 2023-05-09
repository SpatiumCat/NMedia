package ru.netology.nmedia.repository


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.Post
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.DraftDao
import ru.netology.nmedia.entity.DraftEntity


//const val BASE_URL = "http://192.168.0.212:9999"

class PostRepositoryImpl(private val draftDao: DraftDao) : PostRepository {


    override fun getAllAsync(callback: PostRepository.GetAllCallback<List<Post>>) {
      PostApi.retrofitService.getAll().enqueue(object: Callback<List<Post>>{
          override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
              if(!response.isSuccessful) {
                  callback.onError(RuntimeException(response.message()))
                  return
              }
              callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
          }

          override fun onFailure(call: Call<List<Post>>, t: Throwable) {
              callback.onError(RuntimeException(t))
          }

      })
    }


    override fun likeByIdAsync(id: Long, callback: PostRepository.GetAllCallback<Post>) {
       PostApi.retrofitService.likeById(id).enqueue(object: Callback<Post> {
           override fun onResponse(call: Call<Post>, response: Response<Post>) {
               if(!response.isSuccessful) {
                   callback.onError(RuntimeException(response.message()))
                   return
               }
               callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
           }

           override fun onFailure(call: Call<Post>, t: Throwable) {
               callback.onError(RuntimeException(t))
           }

       })
    }

    override fun deleteLikeByIdAsync(id: Long, callback: PostRepository.GetAllCallback<Post>) {
        PostApi.retrofitService.deleteLikeById(id).enqueue(object: Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if(!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }

        })
    }

    override fun shareById(id: Long) {
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.GetAllCallback<Unit>) {
        PostApi.retrofitService.removeById(id).enqueue(object: Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(Unit)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }

        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.GetAllCallback<Post>) {
        PostApi.retrofitService.save(post).enqueue(object: Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if(!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(RuntimeException(t))
            }

        })
    }

    override fun insertDraft(content: String) {
        draftDao.insertDraft(DraftEntity.fromDtoDraft(content))
    }

    override fun deleteDraft() {
        draftDao.deleteDraft()
    }

    override fun getDraft(): String? {
        return draftDao.getDraft()
    }
}
