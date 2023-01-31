package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val post = Post (
            id = 1,
            author = "Нетология. Университет интернет профессий",
            content = "Привет. Это новая Нетология. Когда-то Нетология начиналась с интенсивов по онлайн маркетнигу. Затем появились курсы по дизайну и разработке, аналитике и управлению. Мы растем сами и помогаем расти студентам от новичков до уверенных профессионалов. Но самое важное остается с нами, мы верим, что в каждом уже есть сила,которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия - помочь встать на путь роста и начать цепочку перемен. https://netology.ru/",
            published = "28 января в 21:10",
            likedByMe = false
                )
        binding.apply {
            author.text = post.author
            textPublished.text = post.published
            content.text = post.content
            if (post.likedByMe) {
                like.setImageResource(R.drawable.ic_liked_24)
            }
            likesCount.text = countsMapping(post.likes)
            shareCount.text = countsMapping(post.shares)
            viewsCount.text = countsMapping(post.views)

            like.setOnClickListener {
                post.likedByMe = !post.likedByMe
                if (post.likedByMe) {
                    like.setImageResource(R.drawable.ic_liked_24)
                    post.likes++
                    likesCount.text = countsMapping(post.likes)
                } else {
                    like.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    post.likes--
                    likesCount.text = countsMapping(post.likes)
                }
            }

            share.setOnClickListener {
                post.shares += 10
                shareCount.text = countsMapping(post.shares)
            }
        }
    }
}

fun countsMapping (count: Int): String {
    return when(count) {
        in 0..1099 -> count.toString()
        in 1100..9999 -> String.format(Locale.US,"%.1fk", (count/100)/10.0)
        in 10000..999999 -> "${count/1000}k"
        in 1_000_000..9_999_999 -> String.format(Locale.US,"%.1fM", (count/100_000)/10.0)
        in 10_000_000..Long.MAX_VALUE -> "${count/1_000_000}M"
        else -> ""
    }
}
