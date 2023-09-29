package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.lastOrNull
import ru.netology.nmedia.Post
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostViewHolder
import ru.netology.nmedia.databinding.FragmentPostBinding
import ru.netology.nmedia.viewmodel.PostViewModel
@AndroidEntryPoint
class PostFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentPostBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by activityViewModels()
        val viewHolder = PostViewHolder(binding.post, object : OnInteractionListener {
            override fun onLike(post: Post) {
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

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
                findNavController().navigateUp()
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(
                    R.id.action_postFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = post.content
                    }
                )
            }

            override fun onPlay(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                startActivity(intent)
            }

            override fun onViewPost(post: Post) {}

            override fun onRetrySaving(post: Post) {
                viewModel.retrySaving(post)
            }
        })

//        val currentPost = arguments?.textArg?.let {
//            viewModel.data.value?.posts?.find { post -> post.id == it.toLong() }
//        }
//        if (currentPost != null) {
//            viewHolder.bind(currentPost)
//        }
//        viewModel.data.observe(viewLifecycleOwner) {
//            val updatedPost = it.posts.find { post -> post.id == currentPost?.id }
//            if (updatedPost != null) {
//                viewHolder.bind(updatedPost)
//            }
//        }

//        binding.post.imageAttachmentView.setOnClickListener {
//            findNavController().navigate(
//                R.id.action_postFragment_to_imageFragment,
//                Bundle().apply {
//                    currentPost?.let {
//                        if (it.attachment != null) {
//                            textArg = it.attachment.url
//                        }
//                    }
//                }
//            )
//        }

        return binding.root
    }
}

