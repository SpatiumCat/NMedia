package ru.netology.nmedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val viewModel by viewModels<PostViewModel>()

        viewModel.data.observe(this) { post ->
            binding.apply {
                author.text = post.author
                textPublished.text = post.published
                content.text = post.content
                like.setImageResource(
                    if (post.likedByMe) R.drawable.ic_liked_24 else R.drawable.ic_baseline_favorite_border_24
                )

                likesCount.text = countMapping(post.likes)
                shareCount.text = countMapping(post.shares)
                viewsCount.text = countMapping(post.views)

            }
        }


        binding.like.setOnClickListener {
            viewModel.like()
            Log.d("MainActivity", "like")
        }

        binding.share.setOnClickListener {
            viewModel.share()
            Log.d("MainActivity", "share")
        }

        binding.root.setOnClickListener { Log.d("MainActivity", "root") }
        binding.avatarImageView.setOnClickListener { Log.d("MainActivity", "avatar") }
    }
}


fun countMapping(count: Int): String {
    return when (count) {
        in 0..1099 -> count.toString()
        in 1100..9999 -> String.format(Locale.US, "%.1fk", (count / 100) / 10.0)
        in 10000..999999 -> "${count / 1000}k"
        in 1_000_000..9_999_999 -> String.format(Locale.US, "%.1fM", (count / 100_000) / 10.0)
        in 10_000_000..Long.MAX_VALUE -> "${count / 1_000_000}M"
        else -> ""
    }
}
