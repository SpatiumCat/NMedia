package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import ru.netology.nmedia.Ad
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.DateSeparator
import ru.netology.nmedia.FeedItem
import ru.netology.nmedia.Post
import ru.netology.nmedia.R
import ru.netology.nmedia.countMapping
import ru.netology.nmedia.databinding.CardAdBinding
import ru.netology.nmedia.databinding.CardPostBinding
import ru.netology.nmedia.databinding.DateSeparatorItemBinding
import java.text.SimpleDateFormat
import java.util.*


interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onRemove(post: Post)
    fun onEdit(post: Post)
    fun onPlay(post: Post)
    fun onViewPost(post: Post)
    fun onRetrySaving(post: Post)
}


class PostAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.card_ad
            is Post -> R.layout.card_post
            is DateSeparator -> R.layout.date_separator_item
            null -> error("unknown item type")

        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.card_post -> {
                val binding =
                    CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PostViewHolder(binding, onInteractionListener)
            }

            R.layout.card_ad -> {
                val binding =
                    CardAdBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }
            R.layout.date_separator_item -> {
                val binding =
                    DateSeparatorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DateSeparatorViewHolder(binding)
            }

            else -> error("unknown view type: $viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is DateSeparator -> (holder as? DateSeparatorViewHolder)?.bind(item)
            null -> error("unknown item type")

        }
    }
}

class DateSeparatorViewHolder(
    private val binding: DateSeparatorItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(separator: DateSeparator) {
        binding.textSeparator.text = separator.date
    }
}

class AdViewHolder(
    private val binding: CardAdBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(ad: Ad) {
        Glide.with(binding.image)
            .load("$BASE_URL/media/${ad.image}")
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(binding.image)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    //    private val BASE_URL = "http://192.168.0.212:9999"
    fun bind(post: Post) {
        binding.apply {
            author.text = post.author
            textPublished.text = SimpleDateFormat(
                "dd.MM.yyyy",
                Locale.getDefault()
            ).format(Date(post.published * 1000L))
            content.text = post.content
            like.isChecked = post.likedByMe
            like.text = post.likes.toString()
            videoGroup.visibility = if (post.video.isNullOrBlank()) View.GONE else View.VISIBLE
            with(imageSaved) {
                isActivated = post.isSaved
            }
            errorButton.visibility = if (post.isSaved) View.GONE else View.VISIBLE

            menuButton.isVisible = post.ownedByMe
            imageAttachmentView.visibility = if (post.attachment == null) View.GONE else {
                Glide.with(binding.imageAttachmentView)
                    .load("$BASE_URL/media/${post.attachment.url}")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.imageAttachmentView)
                post.attachment.description?.let {
                    imageAttachmentView.contentDescription = it
                }
                View.VISIBLE
            }

            if (post.authorAvatar.isNotBlank()) {
                Glide.with(binding.avatarImageView)
                    .load("$BASE_URL/avatars/${post.authorAvatar}")
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10000)
                    .transform(CircleCrop())
                    .into(binding.avatarImageView)
            } else {
                Glide.with(binding.avatarImageView).clear(binding.avatarImageView)
                binding.avatarImageView.setImageResource(R.drawable.netology)
            }


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

            menuButton.setOnClickListener {
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
                            }

                            else -> false
                        }
                    }
                }.show()
            }

            binding.root.setOnClickListener {
                onInteractionListener.onViewPost(post)
            }

            binding.errorButton.setOnClickListener {
                onInteractionListener.onRetrySaving(post)
            }
        }
    }
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {

    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }
}
