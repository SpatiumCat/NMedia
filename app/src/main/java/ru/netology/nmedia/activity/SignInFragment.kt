package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel
@AndroidEntryPoint
class SignInFragment : Fragment() {

//    private var _binding: FragmentSignInBinding? = null
//
//    private val binding get() = _binding!!

    private val viewModel: SignInViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentSignInBinding.inflate(inflater, container, false)

        viewModel.authorized.observe(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        viewModel.dataState.observe(viewLifecycleOwner) {
            binding.signInButton.isVisible = !it.loading
            binding.progressBarSignIn.isVisible = it.loading
            if (it.error) {
                Snackbar.make(
                    binding.root, R.string.error_loading, Snackbar.LENGTH_LONG
                )
                    .show()
            }
        }

        binding.signInButton.setOnClickListener {
            val textLogin = binding.loginSignIn.text.toString()
            val textPassword = binding.passwordSignIn.text.toString()
            if (textLogin.isNotBlank() && textPassword.isNotBlank()) {
                viewModel.signIn(textLogin, textPassword)
            }
        }
        return binding.root
    }

//    override fun onDestroyView() {
//        _binding = null
//        super.onDestroyView()
//    }
}
