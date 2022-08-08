package de.thm.mow2.communitycooking.controller.view

import IRecipeEditViewController
import RecipeEditModelController
import android.app.Activity
import android.content.Intent
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.ActivityRecipeEditBinding
import de.thm.mow2.communitycooking.databinding.AlertShowIngredientsBinding
import de.thm.mow2.communitycooking.databinding.AlertShowStepsBinding
import de.thm.mow2.communitycooking.model.IngredientUnit
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.view.activity.RecipeEditActivity
import de.thm.mow2.communitycooking.view.adapter.RecipeEditIngredientListAdapter
import de.thm.mow2.communitycooking.view.adapter.RecipeEditStepListAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.time.Instant
import java.util.*


class RecipeEditViewController(
    private val activity: RecipeEditActivity,
    private val binding: ActivityRecipeEditBinding
) : IRecipeEditViewController {

    private val recipeId: String? = activity.intent.getStringExtra("rid")
    private val modelController = RecipeEditModelController(this)

    private var currentImageId: String = ""
    private var isImageChanged = false

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    init {
        binding.apply {
            cardViewImageContainer.setOnClickListener(::onImageChangeClicked)
            buttonBack.setOnClickListener(::onBackClicked)
            fabSave.setOnClickListener(::onSaveClicked)
            buttonAddIngredient.setOnClickListener(::onAddIngredientClicked)
            buttonAddStep.setOnClickListener(::onAddStepClicked)

            editTextPortionSize.doOnTextChanged { text, _, _, _ ->
                inputLayoutPortionSize.error = when {
                    text.isNullOrEmpty() -> {
                        "Dies ist ein notwendiges Feld!"
                    }
                    text.toString().toInt() <= 0 -> {
                        "Die Portionsgröße muss mindestens 1 sein!"
                    }
                    text.toString().toInt() > 9999 -> {
                        "Die Portionsgröße ist auf 9999 beschränkt!"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextDuration.doOnTextChanged { text, _, _, _ ->
                inputLayoutDuration.error = when {
                    text.isNullOrEmpty() -> {
                        "Dies ist ein notwendiges Feld!"
                    }
                    text.toString().toInt() <= 0 -> {
                        "Die Dauer in Minuten muss mindestens 1 sein!"
                    }
                    text.toString().toInt() > 9999 -> {
                        "Die Dauer ist auf 9999 beschränkt!"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextRecipeTitle.doOnTextChanged { text, _, _, _ ->
                inputLayoutTitle.error = when {
                    text.isNullOrEmpty() -> {
                        "Kein Titel eingegeben!"
                    }
                    else -> {
                        null
                    }
                }
            }

            editTextDescription.doOnTextChanged { text, _, _, _ ->
                inputLayoutDescription.error = when {
                    text.isNullOrEmpty() -> {
                        "Keine Beschreibung eingegeben!"
                    }
                    else -> {
                        null
                    }
                }
            }

            spinnerPreference.adapter =
                ArrayAdapter.createFromResource(
                    activity,
                    R.array.recipeTypes,
                    android.R.layout.simple_spinner_dropdown_item
                )

            recyclerViewIngredientsList.adapter = RecipeEditIngredientListAdapter(activity)
            recyclerViewIngredientsList.layoutManager = LinearLayoutManager(activity)

            recyclerViewStepsList.adapter = RecipeEditStepListAdapter()
            recyclerViewStepsList.layoutManager = LinearLayoutManager(activity)
        }

        downloadData(recipeId)
        registerForActivityResult()
    }

    fun downloadData(recipeId: String?) {
        if (recipeId != null) {
            modelController.downloadData(recipeId)
        }
    }

    override fun setData(recipe: Recipe) {
        binding.apply {
            editTextRecipeTitle.setText(recipe.title)
            editTextDescription.setText(recipe.description)
            spinnerPreference.setSelection(RecipeType.values().indexOf(recipe.type))
            editTextPortionSize.setText(recipe.portionSize.toString())
            editTextDuration.setText(recipe.duration.toString())
            (recyclerViewIngredientsList.adapter as? RecipeEditIngredientListAdapter)?.updateContents(
                recipe.ingredients.toMutableList()
            )
            (recyclerViewStepsList.adapter as? RecipeEditStepListAdapter)?.updateContents(
                recipe.steps.toMutableList()
            )

            currentImageId = recipe.image

            FirebaseStorageService.downloadImageIntoImageView(
                recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
        }
    }

    private fun registerForActivityResult() {
        resultLauncher =
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val dataUri = result.data?.data

                    dataUri?.let { uri ->

                        try {
                            val bitmap =
                                if (Build.VERSION.SDK_INT < 28) MediaStore.Images.Media.getBitmap(
                                    activity.contentResolver,
                                    uri
                                ) else ImageDecoder.decodeBitmap(
                                    ImageDecoder.createSource(
                                        activity.contentResolver,
                                        uri
                                    )
                                )

                            binding.imageViewRecipeImage.setImageBitmap(
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

    fun onSaveClicked(view: View) {
        binding.apply {
            val timestamp = Instant.now().epochSecond.toDouble()
            val title = editTextRecipeTitle.text.toString().trim()
            val description = editTextDescription.text.toString().trim()
            val type = RecipeType.values()[spinnerPreference.selectedItemPosition]
            val steps = (recyclerViewStepsList.adapter as? RecipeEditStepListAdapter)?.steps!!
            val ingredients =
                (recyclerViewIngredientsList.adapter as? RecipeEditIngredientListAdapter)?.ingredients!!
            val portionSize = editTextPortionSize.text.toString()
            val duration = editTextDuration.text.toString()

            if (isImageChanged) {
                currentImageId =
                    if (currentImageId.isEmpty()) UUID.randomUUID().toString() else currentImageId

                val bitmap = (imageViewRecipeImage.drawable as BitmapDrawable).bitmap

                FirebaseStorageService.uploadImageIntoStorage(
                    currentImageId,
                    bitmap
                ) { isUploaded ->

                    if (isUploaded) {
                        if (title.isNotEmpty() && description.isNotEmpty() && steps.isNotEmpty() && ingredients.isNotEmpty() && portionSize.isNotEmpty() && duration.isNotEmpty()) {
                            modelController.onSaveClicked(
                                timestamp,
                                title,
                                description,
                                currentImageId,
                                type,
                                steps,
                                ingredients,
                                portionSize.trim().toInt(),
                                duration.trim().toInt(),
                                recipeId
                            )
                        } else {
                            showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
                        }
                    } else {
                        showErrorMessage("Dieses Bild konnte leider nicht hochgeladen werdenm! Versuchen Sie es noch einmal!")
                    }
                }
            } else {
                if (title.isNotEmpty() && description.isNotEmpty() && steps.isNotEmpty() && ingredients.isNotEmpty() && portionSize.isNotEmpty() && duration.isNotEmpty()) {
                    modelController.onSaveClicked(
                        timestamp,
                        title,
                        description,
                        currentImageId,
                        type,
                        steps,
                        ingredients,
                        portionSize.trim().toInt(),
                        duration.trim().toInt(),
                        recipeId
                    )
                } else {
                    showErrorMessage("Bitte füllen Sie alle benötigten Informationen aus!")
                }
            }
        }
    }

    private fun onAddIngredientClicked(view: View) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle("Zutat hinzufügen")
            AlertShowIngredientsBinding.inflate(LayoutInflater.from(context)).apply {
                setView(root)

                buttonDelete.visibility = View.GONE
                spinnerUnit.adapter = ArrayAdapter.createFromResource(
                    context,
                    R.array.ingredientUnitDropDown,
                    android.R.layout.simple_spinner_dropdown_item
                )
                editTextAmount.doOnTextChanged { text, _, _, _ ->
                    inputLayoutAmount.error =
                        if (text.isNullOrBlank()) "Keine Menge eingegeben!" else null
                }
                editTextIngredientName.doOnTextChanged { text, _, _, _ ->
                    inputLayoutIngredientName.error =
                        if (text.isNullOrBlank()) "Kein Name eingegeben!" else null
                }

                create().apply {
                    buttonConfirm.setOnClickListener {
                        if (editTextAmount.text!!.isNotEmpty() && editTextIngredientName.text!!.isNotEmpty()) {
                            (binding.recyclerViewIngredientsList.adapter as RecipeEditIngredientListAdapter).addItem(
                                editTextIngredientName.text.toString().trim(),
                                editTextAmount.text.toString().toDouble(),
                                IngredientUnit.values()[spinnerUnit.selectedItemPosition]
                            )
                            dismiss()
                        }
                        if (editTextIngredientName.text.isNullOrEmpty()) {
                            inputLayoutIngredientName.error = "Kein Name eingegeben!"
                        }
                        if (editTextAmount.text.isNullOrEmpty()) {
                            inputLayoutAmount.error = "Keine Menge eingegeben!"
                        }
                    }
                    buttonCancel.setOnClickListener { dismiss() }
                }.show()
            }
        }
    }

    private fun onAddStepClicked(view: View) {
        MaterialAlertDialogBuilder(activity).apply {
            setTitle("Arbeitsschritt hinzufügen")
            AlertShowStepsBinding.inflate(LayoutInflater.from(context)).apply {
                setView(root)

                buttonDelete.visibility = View.GONE
                editTextStepDescription.doOnTextChanged { text, _, _, _ ->
                    inputLayoutStepDescription.error =
                        if (text.isNullOrBlank()) "Keine Beschreibung eingegeben!" else null
                }

                create().apply {
                    buttonConfirm.setOnClickListener {
                        if (editTextStepDescription.text!!.isNotEmpty()) {
                            (binding.recyclerViewStepsList.adapter as RecipeEditStepListAdapter).addItem(
                                inputLayoutStepDescription.editText!!.text.trim().toString()
                            )
                            dismiss()
                        }
                        if (editTextStepDescription.text.isNullOrEmpty()) {
                            inputLayoutStepDescription.error = "Keine Beschreibung eingegeben!"
                        }
                    }
                    buttonCancel.setOnClickListener { dismiss() }
                }.show()
            }
        }
    }

    override fun exitActivity() {
        activity.finish()
    }

    private fun onBackClicked(view: View) {
        activity.finish()
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }
}