package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.Post

class PostRepositoryInMemoryImp: PostRepository {

    private var post = Post (
        id = 1,
        author = "Нетология. Университет интернет профессий",
        content = "Привет. Это новая Нетология. Когда-то Нетология начиналась с интенсивов по онлайн маркетнигу. Затем появились курсы по дизайну и разработке, аналитике и управлению. Мы растем сами и помогаем расти студентам от новичков до уверенных профессионалов. Но самое важное остается с нами, мы верим, что в каждом уже есть сила,которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия - помочь встать на путь роста и начать цепочку перемен. https://netology.ru/",
        published = "28 января в 21:10",
        likedByMe = false
            )

    val data = MutableLiveData(post)

    override fun get(): LiveData<Post> = data

    override fun like() {
        post = post.copy(likedByMe = !post.likedByMe, likes = post.likes +  if (post.likedByMe) -1 else +1 )
        data.value = post
    }

    override fun share() {
        post = post.copy(shares = post.shares + 10)
        data.value = post
    }
}
