package de.thm.mow2.communitycooking.controller.view

import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.IProfileEditViewController
import de.thm.mow2.communitycooking.controller.model.ProfileEditModelController
import de.thm.mow2.communitycooking.databinding.ActivityProfileEditBinding
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.model.service.AuthenticationService
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.mow2.communitycooking.view.activity.LoginActivity
import de.thm.mow2.communitycooking.view.activity.MainActivity
import de.thm.mow2.communitycooking.view.activity.ProfileEditActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import java.util.*


class ProfileEditViewController(
    private val activity: ProfileEditActivity,
    private val binding: ActivityProfileEditBinding
) : IProfileEditViewController {

    private var currentImageId: String = ""
    private var isImageChanged = false

    private val modelController: ProfileEditModelController = ProfileEditModelController(this)
    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    init {
        binding.apply {
            cardViewImageContainer.setOnClickListener(::onImageChangeClicked)
            buttonChangePassword.setOnClickListener(::onPasswordChangeClicked)
            fabSave.setOnClickListener(::onSaveClicked)
            buttonBack.setOnClickListener(::onReturnClicked)

            spinnerPreference.adapter =
                ArrayAdapter.createFromResource(
                    activity,
                    R.array.recipeTypes,
                    android.R.layout.simple_spinner_dropdown_item
                )
            seekBarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, boolean: Boolean) {
                    textViewRadiusDisplay.text = "$progress km"
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {}
                override fun onStopTrackingTouch(p0: SeekBar?) {}
            })

            editTextFirstName.doOnTextChanged { text, _, _, _ ->
                inputLayoutFirstName.error = when {
                    text.isNullOrEmpty() -> {
                        "Kein Vorname eingegeben!"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextLastName.doOnTextChanged { text, _, _, _ ->
                inputLayoutLastName.error = when {
                    text.isNullOrEmpty() -> {
                        "Kein Nachname eingegeben!"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextDescription.doOnTextChanged { text, _, _, _ ->
                inputLayoutDescription.error = when {
                    text.isNullOrEmpty() -> {
                        "Dies ist ein notwendiges Feld"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextUsername.doOnTextChanged { text, _, _, _ ->
                inputLayoutUsername.error =
                    if (text.isNullOrBlank()) "Kein Benutzername eingegeben!" else null

            }

            editTextEmail.doOnTextChanged { text, _, _, _ ->
                inputLayoutEmail.error = when {
                    text.isNullOrEmpty() -> {
                        "Keine Emailadresse eingegeben!"
                    }
                    else -> {
                        null
                    }
                }
            }

            DatabaseService.getMyself { result, error ->
                result?.also { user ->
                    editTextUsername.setText(user.username)
                    editTextUsername.isEnabled = false
                    editTextFirstName.setText(user.forename)
                    editTextLastName.setText(user.lastname)
                    editTextDescription.setText(user.description)
                    spinnerPreference.setSelection(RecipeType.values().indexOf(user.preference))
                    seekBarRadius.progress = user.radiusInKilometer

                    currentImageId = user.image

                    FirebaseStorageService.downloadImageIntoImageView(
                        user.image,
                        imageViewProfileImage,
                        R.mipmap.default_user
                    )
                } ?: run {
                    TPFirebaseAuthentication.getUser()?.displayName?.let { displayName ->
                        editTextUsername.setText(displayName)
                    }
                    spinnerPreference.setSelection(RecipeType.values().indexOf(RecipeType.NONE))
                    seekBarRadius.progress = 15
                }

                error?.let { message ->
                    showErrorMessage(message)
                }
            }

            TPFirebaseAuthentication.getUser()?.email?.let { email ->
                editTextEmail.setText(email)
            }
        }

        registerForActivityResult()
    }

    override fun redirectToMainActivity() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    override fun exitActivity() {
        activity.finish()
    }

    private fun onReturnClicked(view: View) {
        activity.finish()
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    private fun registerForActivityResult() {
        resultLauncher = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val dataUri = result.data?.data

                dataUri?.let { uri ->

                    try {
                        val bitmap = if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
                            activity.contentResolver,
                            uri
                        ) else ImageDecoder.decodeBitmap(ImageDecoder.createSource(activity.contentResolver, uri))

                        binding.imageViewProfileImage.setImageBitmap(
                            ThumbnailUtils.extractThumbnail(
                                bitmap,
                                512,
                                512
                            )
                        )
                        isImageChanged = true
                    } catch (exception: Exception) {

                    }
                }
            }
        }
    }

    private fun onImageChangeClicked(view: View) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }

    private fun onSaveClicked(view: View) {
        binding.apply {
            val username = editTextUsername.text.toString()
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val preference = RecipeType.values()[spinnerPreference.selectedItemPosition]
            val radius = seekBarRadius.progress

            if (isImageChanged) {
                currentImageId =
                    if (currentImageId.isEmpty()) UUID.randomUUID().toString() else currentImageId

                val bitmap = (imageViewProfileImage.drawable as BitmapDrawable).bitmap

                FirebaseStorageService.uploadImageIntoStorage(
                    currentImageId,
                    bitmap
                ) { isUploaded ->

                    if (isUploaded) {
                        if (username.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && description.isNotEmpty()) {
                            modelController.onSaveClicked(
                                username, firstName, lastName, email, description, currentImageId,
                                preference, radius, editTextUsername.isEnabled
                            )
                        } else {
                            showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
                        }
                    } else {
                        showErrorMessage("Dieses Bild konnte leider nicht hochgeladen werdenm! Versuchen Sie es noch einmal!")
                    }
                }

            } else {
                if (username.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && description.isNotEmpty()) {
                    modelController.onSaveClicked(
                        username, firstName, lastName, email, description, currentImageId,
                        preference, radius, editTextUsername.isEnabled
                    )
                } else {
                    showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
                }
            }
        }
    }

    private fun onPasswordChangeClicked(view: View) {
        TPFirebaseAuthentication.sendPasswordResetEmail { error ->
            error?.also { message ->
                showErrorMessage(message)
            } ?: run {
                MaterialAlertDialogBuilder(activity)
                    .setMessage("Es wurde eine Email an \"${TPFirebaseAuthentication.getUser()!!.email}\" gesendet, über die Sie Ihr Passwort ändern können. Sie werden nun aus der App abgemeldet und zum Login weitergeleitet!")
                    .setTitle("Passwort ändern")
                    .setPositiveButton("OK") { _, _ ->
                        AuthenticationService.signOut()
                        activity.finishAffinity()
                        activity.startActivity(Intent(activity, LoginActivity::class.java))
                    }
                    .show()
            }
        }
    }
}