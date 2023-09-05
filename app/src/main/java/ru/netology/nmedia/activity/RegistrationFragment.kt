package ru.netology.nmedia.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val registrationViewModel: RegistrationViewModel by viewModels(ownerProducer = ::requireParentFragment)

    private val avatarPickerContract =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(
                    requireContext(),
                    R.string.photo_pick_error,
                    Toast.LENGTH_LONG
                ).show()

                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    registrationViewModel.setAvatar(PhotoModel(uri, uri.toFile()))
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)

        registrationViewModel.dataState.observe(viewLifecycleOwner) { state ->
            binding.progressBarRegister.isVisible = state.loading
            binding.registerButton.isVisible = !state.loading
            if (state.error) {
                Snackbar.make(
                    binding.root, R.string.error_loading, Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }

        registrationViewModel.registrated.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        registrationViewModel.avatar.observe(viewLifecycleOwner) { avatar ->
            if (avatar == null) {return@observe}
            binding.avatarUser.setImageURI(avatar.uri)
        }

        binding.registerButton.setOnClickListener {
            val login = binding.loginRegister.text.toString()
            val pass = binding.passwordRegister.text.toString()
            val confPass = binding.passwordConfirmedRegister.text.toString()
            val name = binding.nameRegister.text.toString()
            if (pass != confPass) {
                Snackbar.make(
                    binding.root, R.string.password_not_conf, Snackbar.LENGTH_LONG
                )
                    .show()
                return@setOnClickListener
            }
            if (login.isNotBlank() && pass.isNotBlank() && name.isNotBlank()) {
                registrationViewModel.registerUser(login, pass, name)
            }
        }

        binding.avatarUser.setOnClickListener {
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .createIntent {
                    avatarPickerContract.launch(it)
                }
        }



        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
