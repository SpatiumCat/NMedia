package ru.netology.nmedia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.*

class MainActivity : AppCompatActivity() {

    val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val viewModel by viewModels<PostViewModel>()
    val adapter = PostAdapter(object : OnInteractionListener {

        override fun onLike(post: Post) {
            viewModel.likeById(post.id)
        }

        override fun onShare(post: Post) {
            viewModel.shareById(post.id)
        }

        override fun onRemove(post: Post) {
            viewModel.removeById(post.id)
        }

        override fun onEdit(post: Post) {
            viewModel.edit(post)
            binding.editGroup.visibility = View.VISIBLE
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editGroup.visibility = if (viewModel.edited.value?.id == 0L) View.GONE else View.VISIBLE
        binding.list.adapter = adapter
        viewModel.data.observe(this) { posts ->
            adapter.submitList(posts)
        }


        viewModel.edited.observe(this) { post ->
            if (post.id == 0L) {
                return@observe
            }
            binding.contentPanel.apply {
                requestFocus()
                setText(post.content)
            }
        }
        binding.editCancelButton.setOnClickListener {
            viewModel.save()
            binding.contentPanel.apply {
                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(this)
            }
            binding.editGroup.visibility = View.GONE
        }

        binding.save.setOnClickListener {
            binding.contentPanel.apply {
                if (text.isNullOrBlank()) {
                    Toast.makeText(
                        this@MainActivity,
                        R.string.toast_text,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                viewModel.changeContent(text.toString())
                viewModel.save()

                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(this)
                binding.editGroup.visibility = View.GONE
            }
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


