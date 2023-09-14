package ru.netology.nmedia.activity


import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.util.StringArg
import ru.netology.nmedia.viewmodel.PostViewModel

@AndroidEntryPoint
class NewPostFragment : Fragment() {

    val viewModel: PostViewModel by viewModels(ownerProducer = ::requireParentFragment)

    private val photoPickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    R.string.photo_pick_error,
                    Toast.LENGTH_SHORT
                ).show()

                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    viewModel.setPhoto(PhotoModel(uri, uri.toFile()))
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentNewPostBinding.inflate(inflater, container, false)
        arguments?.textArg ?: binding.contentPanel.setText(viewModel.draft)
        arguments?.textArg?.let(binding.contentPanel::setText)
        binding.contentPanel.requestFocus()


        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.save -> {
                        val text = binding.contentPanel.text.toString()
                        if (text.isNotBlank()) {
                            viewModel.changeContent(text)
                            viewModel.save()
                            AndroidUtils.hideKeyboard(requireView())
                        }
                        true
                    }

                    else -> false
                }
        }, viewLifecycleOwner)

        binding.clearButton.setOnClickListener {
            viewModel.clearPhoto()
        }

        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent(photoPickerContract::launch)
        }

        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .cameraOnly()
                .crop()
                .createIntent { photoPickerContract.launch(it) }
        }

        viewModel.photo.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.previewContainer.isGone = true
                return@observe
            }

            binding.previewContainer.isVisible = true
            binding.preview.setImageURI(photo.uri)
        }

        viewModel.postCreated.observe(viewLifecycleOwner) {
            //viewModel.loadPosts()
            findNavController().navigateUp()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val text = binding.contentPanel.text.toString()
            if (text.isNotBlank()) {
                viewModel.saveDraft(text)
            }
            findNavController().navigateUp()
        }
        return binding.root
    }

    companion object {
        var Bundle.textArg: String? by StringArg
    }

}

