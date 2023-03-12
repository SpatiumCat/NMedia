package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.Post
import ru.netology.nmedia.R
import ru.netology.nmedia.countMapping
import ru.netology.nmedia.databinding.CardPostBinding

typealias OnLikeListener = (post: Post) -> Unit

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPlay(post: Post)
}


class PostAdapter (
    private val onInteractionListener: OnInteractionListener
    ): ListAdapter<Post, PostViewHolder>(PostDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

}

class PostViewHolder (
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind (post: Post) {
            binding.apply {
                author.text = post.author
                textPublished.text = post.published
                content.text = post.content
                like.isChecked = post.likedByMe
                like.text = post.likes.toString()
                videoGroup.visibility = if (post.video.isBlank()) View.GONE else View.VISIBLE

                like.text = countMapping(post.likes)
                share.text = countMapping(post.shares)
                viewsCount.text = countMapping(post.views)

                like.setOnClickListener {
                    onInteractionListener.onLike(post)
                }
                share.setOnClickListener {
                    onInteractionListener.onShare(post)
                }

                imageVideoButton.setOnClickListener {
                    onInteractionListener.onPlay(post)
                }

                playButton.setOnClickListener {
                    onInteractionListener.onPlay(post)
                }

                menuButton.setOnClickListener{
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.popup_menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(post)
                                    true
                                }
                                R.id.edit -> {
                                    onInteractionListener.onEdit(post)
                                    true
                                } else -> false
                            }
                        }
                    }.show()
                }
            }
        }
    }

class PostDiffCallback: DiffUtil.ItemCallback<Post>() {

    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}
