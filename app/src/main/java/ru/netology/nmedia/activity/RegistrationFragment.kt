package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.RegistrationViewModel

class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!
    private val registrationViewModel: RegistrationViewModel by viewModels(ownerProducer = ::requireParentFragment)

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


        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
