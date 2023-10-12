package ru.netology.nmedia

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.adapter.PostLoadingStateAdapter
import ru.netology.nmedia.databinding.DialogSigninBinding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.viewmodel.SignInViewModel
import java.util.*

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var dialogBuilder: AlertDialog.Builder
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentFeedBinding.inflate(layoutInflater, container, false)
        val viewModel: PostViewModel by activityViewModels()
        val authViewModel: AuthViewModel by activityViewModels()
        val signInViewModel: SignInViewModel by activityViewModels()
        val dialogSigninBinding = DialogSigninBinding.inflate(layoutInflater)

        dialogBuilder = AlertDialog.Builder(requireActivity())
        dialogBuilder.setView(dialogSigninBinding.root)
            .setCancelable(true)
            .setPositiveButton(R.string.dialog_signin) { _, _ ->
                val login = dialogSigninBinding.username.text.toString()
                val password = dialogSigninBinding.password.text.toString()
                signInViewModel.signIn(login, password)

            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                dialog.cancel()
            }


        val adapter = PostAdapter(object : OnInteractionListener {

            override fun onLike(post: Post) {
                if (!authViewModel.isAuthorized) {
                    val parent: ViewGroup? = dialogSigninBinding.root.parent as? ViewGroup
                    parent?.removeView(dialogSigninBinding.root)
                    dialogBuilder.show()
                    return
                }
                viewModel.likeById(post.id, post.likedByMe)
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

            override fun onRetrySaving(post: Post) {
                viewModel.retrySaving(post)
            }
        })


        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = PostLoadingStateAdapter { adapter.retry() },
            footer = PostLoadingStateAdapter { adapter.retry() },
        )


        lifecycleScope.launch {
            viewModel.data.collectLatest {
                adapter.submitData(it)
            }
        }


        binding.add.setOnClickListener {
            if (!authViewModel.isAuthorized) {
                val parent: ViewGroup? = dialogSigninBinding.root.parent as? ViewGroup
                parent?.removeView(dialogSigninBinding.root)
                dialogBuilder.show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest {
                binding.swiperefresh.isRefreshing =
                    it.refresh is LoadState.Loading
            }
        }
        authViewModel.data.observe(viewLifecycleOwner) {
            adapter.refresh()
        }

        binding.swiperefresh.setOnRefreshListener {
            adapter.refresh()
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


