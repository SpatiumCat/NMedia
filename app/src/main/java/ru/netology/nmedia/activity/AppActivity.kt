package ru.netology.nmedia.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.onNavDestinationSelected
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.databinding.ActivityAppBinding
import ru.netology.nmedia.databinding.DialogSigninBinding
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.SignInViewModel
import javax.inject.Inject


@AndroidEntryPoint
class AppActivity : AppCompatActivity() {

    @Inject
    lateinit var appAuth: AppAuth
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability
    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    private lateinit var dialogBuilder: AlertDialog.Builder


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityAppBinding.inflate(layoutInflater)
        val dialogSigninBinding = DialogSigninBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) return@let

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) {
                Snackbar.make(
                    binding.root,
                    R.string.error_empty_content,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(android.R.string.ok) {
                        finish()
                    }
                    .show()
                return@let
            }
            findNavController(R.id.navHostFragment).navigate(
                R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    textArg = text
                }
            )
        }

        checkGoogleApiAvailability()

        val viewModel: AuthViewModel by viewModels<AuthViewModel>()
        val signInViewModel by viewModels<SignInViewModel>()

        dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogSigninBinding.root)
            .setCancelable(true)
            .setPositiveButton(R.string.dialog_signin) { dialog, id ->
                val login = dialogSigninBinding.username.text.toString()
                val password = dialogSigninBinding.password.text.toString()
                signInViewModel.signIn(login, password)

            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, id ->
                dialog.cancel()
            }

        var oldMenuProvider: MenuProvider? = null

        viewModel.data.observe(this) {
            oldMenuProvider?.let(::removeMenuProvider)

            addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_auth, menu)
                    val authorized = viewModel.isAuthorized
                    if (authorized) {
                        menu.setGroupVisible(R.id.authorized, true)
                        menu.setGroupVisible(R.id.unauthorized, false)
                    } else {
                        menu.setGroupVisible(R.id.authorized, false)
                        menu.setGroupVisible(R.id.unauthorized, true)
                    }
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                    when (menuItem.itemId) {
                        R.id.auth -> {
                            menuItem.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                            true
                        }

                        R.id.register -> {
                            findNavController(R.id.navHostFragment).navigate(R.id.toRegistrationFragment)
                            true
                        }

                        R.id.logout -> {
                            val destination =
                                findNavController(R.id.navHostFragment).currentDestination
                            destination?.let {
                                if (it.id == R.id.newPostFragment) {
                                    AlertDialog.Builder(this@AppActivity)
                                        .setTitle(R.string.dialog_are_you_sure)
                                        .setCancelable(false)
                                        .setPositiveButton(R.string.dialog_ok) { dialog, id ->
                                            appAuth.clearAuth()
                                            findNavController(R.id.navHostFragment).navigateUp()
                                        }
                                        .setNegativeButton(R.string.dialog_cancel) { dialog, id ->
                                            dialog.cancel()
                                        }
                                        .show()
                                } else {
                                    appAuth.clearAuth()
                                }
                            }
                            true
                        }

                        else -> {
                            false
                        }
                    }
            }.apply {
                oldMenuProvider = this
            }, this)
        }

    }


    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, R.string.google_api_unavailable, Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }

}
