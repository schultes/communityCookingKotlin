package de.thm.mow2.communitycooking.controller.view

import android.content.Intent
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.controller.model.ILoginViewController
import de.thm.mow2.communitycooking.controller.model.LoginModelController
import de.thm.mow2.communitycooking.databinding.ActivityLoginBinding
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.mow2.communitycooking.view.activity.LoginActivity
import de.thm.mow2.communitycooking.view.activity.MainActivity
import de.thm.mow2.communitycooking.view.activity.ProfileEditActivity
import de.thm.mow2.communitycooking.view.activity.RegisterActivity
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication


class LoginViewController(
    private val activity: LoginActivity,
    private val binding: ActivityLoginBinding
) : ILoginViewController {

    private val modelController: LoginModelController = LoginModelController(this)

    init {
        binding.apply {
            editTextEmail.doOnTextChanged { text, _, _, _ ->
                inputLayoutEmail.error =
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text)
                            .matches()
                    ) "Emailadresse ist nicht korrekt eingegeben!" else null
                if (text.isNullOrBlank()) "Keine Emailadresse eingeben!" else null
            }
            editTextPassword.doOnTextChanged { text, _, _, _ ->
                inputLayoutPassword.error =
                    if (text.isNullOrBlank()) "Kein Passwort eingeben!" else null
            }

            buttonLogin.setOnClickListener(::onLoginClicked)
            buttonRedirectToRegister.setOnClickListener(::onRedirectToRegisterClicked)
        }

        if (TPFirebaseAuthentication.isSignedIn()) {
            DatabaseService.getMyself { user, error ->
                user?.also { user ->
                    redirectToMainActivity()
                } ?: run {
                    redirectToProfileEditActivity()
                }

                error?.let { message ->
                    showErrorMessage("Unglütige Eingabe. Das Passwort stimmt mit der Email-Adresse nicht überein!")
                }
            }
        }
    }

    private fun onLoginClicked(view: View) {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            modelController.onLoginClicked(email, password)
        } else {
            showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
        }
    }

    private fun onRedirectToRegisterClicked(view: View) {
        activity.startActivity(Intent(activity, RegisterActivity::class.java))
        activity.finish()
    }


    override fun redirectToMainActivity() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    override fun redirectToProfileEditActivity() {
        activity.startActivity(Intent(activity, ProfileEditActivity::class.java))
        activity.finish()
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }
}