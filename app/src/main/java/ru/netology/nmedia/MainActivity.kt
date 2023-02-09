package ru.netology.nmedia

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val viewModel by viewModels<PostViewModel>()
        val adapter = PostAdapter (
            { post ->
            viewModel.likeById(post.id) },
            { post ->
            viewModel.shareById(post.id) }
        )

        binding.list.adapter = adapter
        viewModel.data.observe (this) { posts ->
            adapter.submitList(posts)
        }
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
