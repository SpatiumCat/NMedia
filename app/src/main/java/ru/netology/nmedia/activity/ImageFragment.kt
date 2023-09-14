package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.databinding.FragmentImageBinding
import ru.netology.nmedia.viewmodel.PostViewModel
@AndroidEntryPoint
class ImageFragment : Fragment() {

//    private var _binding: FragmentImageBinding? = null
//    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentImageBinding.inflate(inflater, container, false)
        val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

        arguments?.textArg?.let {
            Glide.with(binding.image)
                .load("${BuildConfig.BASE_URL}/media/${it}")
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.image)
        }
        return binding.root
    }

//    override fun onDestroyView() {
//        _binding = null
//        super.onDestroyView()
//    }
}
