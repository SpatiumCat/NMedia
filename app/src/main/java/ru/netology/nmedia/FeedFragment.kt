package ru.netology.nmedia

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.viewmodel.PostViewModel
import java.util.*

class FeedFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        val adapter = PostAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                viewModel.shareById(post.id)
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.chooser_share_post))
                startActivity(shareIntent)
            }

            @SuppressLint("SuspiciousIndentation")
            override fun onPlay(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }

            override fun onViewPost(post: Post) {
                findNavController().navigate(
                    R.id.action_feedFragment_to_postFragment,
                    Bundle().apply {
                        textArg = post.id.toString()
                    }
                )
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }
        })


        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            val newPost = adapter.currentList.size < state.posts.size
            adapter.submitList(state.posts) {
                if (newPost) binding.list.smoothScrollToPosition(0)
            }
            binding.emptyText.isVisible = state.empty

        }
        viewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state.loading
            binding.swiperefresh.isRefreshing = state.refreshing
            if (state.error) {
                Snackbar.make(
                    binding.root, R.string.error_loading, Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.retry_loading) {viewModel.loadPosts()}
                    .show()
            }


        }

//        binding.retryButton.setOnClickListener {
//            viewModel.loadPosts()
//        }

        binding.add.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        binding.swiperefresh.setOnRefreshListener {
            viewModel.refresh()
           // binding.swiperefresh.isRefreshing = false
        }
        return binding.root
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


