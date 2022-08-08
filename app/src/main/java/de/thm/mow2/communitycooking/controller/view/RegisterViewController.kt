package de.thm.mow2.communitycooking.controller.view

import android.content.Intent
import android.view.View
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.controller.model.IRegisterViewController
import de.thm.mow2.communitycooking.controller.model.RegisterModelController
import de.thm.mow2.communitycooking.databinding.ActivityRegisterBinding
import de.thm.mow2.communitycooking.view.activity.LoginActivity
import de.thm.mow2.communitycooking.view.activity.ProfileEditActivity
import de.thm.mow2.communitycooking.view.activity.RegisterActivity

class RegisterViewController(
    private val activity: RegisterActivity,
    private val binding: ActivityRegisterBinding
) : IRegisterViewController {

    private val modelController: RegisterModelController = RegisterModelController(this)

    init {
        binding.apply {
            editTextUsername.doOnTextChanged { text, _, _, _ ->
                inputLayoutUsername.error =
                    if (text.isNullOrBlank()) "Kein Benutzername eingegeben!" else null
            }

            editTextEmail.doOnTextChanged { text, _, _, _ ->
                inputLayoutEmail.error =
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(text)
                            .matches()
                    ) "Emailadresse ist nicht korrekt eingegeben!" else null
                if (text.isNullOrBlank()) "Keine Emailadresse eingegeben!" else null
            }

            editTextPassword.doOnTextChanged { text, _, _, _ ->
                inputLayoutPassword.error =
                    if (text!!.length < 6) "Das Passwort muss mindestens 6 Zeichen lang sein!" else null
                if (text.isNullOrBlank()) "Kein Passwort eingegeben!" else null
            }

            editTextConfirmPassword.doOnTextChanged { text, _, _, _ ->
                inputLayoutConfirmPassword.error =
                    if (text.toString() != inputLayoutPassword.editText!!.text.toString()) "Passwörter stimmen nicht überein!" else null
                if (text.isNullOrBlank()) "Kein Passwort eingegeben!" else null
            }

            buttonRegister.setOnClickListener(::onLoginClicked)
            buttonRedirectToLogin.setOnClickListener(::onRedirectToLoginClicked)
        }
    }

    private fun onLoginClicked(view: View) {
        binding.apply {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()
            val username = editTextUsername.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                if (password == confirmPassword) {
                    modelController.onRegisterClicked(email, password, username)
                }
            } else {
                showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
            }
        }
    }

    private fun onRedirectToLoginClicked(view: View) {
        activity.startActivity(Intent(activity, LoginActivity::class.java))
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