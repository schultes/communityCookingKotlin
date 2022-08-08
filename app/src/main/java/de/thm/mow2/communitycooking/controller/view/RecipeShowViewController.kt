package de.thm.mow2.communitycooking.controller.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.IRecipeShowViewController
import de.thm.mow2.communitycooking.controller.model.RecipeShowModelController
import de.thm.mow2.communitycooking.databinding.ActivityRecipeShowBinding
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.mow2.communitycooking.view.activity.RecipeEditActivity
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.adapter.RecipeIngredientsListAdapter
import de.thm.mow2.communitycooking.view.adapter.StepsListAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class RecipeShowViewController(
    private val activity: RecipeShowActivity,
    private val binding: ActivityRecipeShowBinding
) : IRecipeShowViewController {

    private val recipeId: String = activity.intent.getStringExtra("rid").toString()
    private val modelController = RecipeShowModelController(this)
    private var recipe: Recipe? = null

    init {
        binding.apply {
            buttonBack.setOnClickListener(::onBackClicked)
            buttonOwner.setOnClickListener(::onShowUserClicked)
            fabEdit.setOnClickListener(::onEditClicked)

            recyclerViewIngredientsList.adapter = RecipeIngredientsListAdapter(activity)
            recyclerViewIngredientsList.layoutManager = LinearLayoutManager(activity)

            recyclerViewStepsList.adapter = StepsListAdapter()
            recyclerViewStepsList.layoutManager = LinearLayoutManager(activity)
        }

        loadRecipe()
    }

    fun loadRecipe() {
        modelController.downloadData(recipeId)
    }

    override fun setData(userId: String, recipe: Recipe) {
        this.recipe = recipe
        binding.apply {
            textViewRecipeTitle.text = recipe.title
            textViewDate.text =
                LocalDateTime.ofEpochSecond(recipe.timestamp.toLong(), 0, ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("dd. MMMM yyyy"))
            editTextDuration.text = recipe.duration.toString() + " Minuten"
            textViewPortionSize.text = " " + recipe.portionSize.toString() + " "
            buttonOwner.text = recipe.owner.fullname
            textViewDescription.text = recipe.description
            textViewPreference.text =
                activity.resources.getStringArray(R.array.recipeTypes)[RecipeType.values()
                    .indexOf(recipe.type)]
            (recyclerViewIngredientsList.adapter as? RecipeIngredientsListAdapter)?.updateContents(
                recipe.ingredients,
                recipe.portionSize
            )
            (recyclerViewStepsList.adapter as? StepsListAdapter)?.updateContents(
                recipe.steps
            )

            FirebaseStorageService.downloadImageIntoImageView(
                recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
            if (userId == recipe.owner.documentId) {
                fabEdit.visibility = View.VISIBLE
            }
        }
    }

    private fun onShowUserClicked(view: View) {
        recipe?.let { recipe ->
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(ProfileShowViewController.EXTRA_PARAMETER_UID, recipe.owner.documentId)
            view.context.startActivity(intent)
        }
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    private fun onBackClicked(view: View) {
        activity.finish()
    }

    private fun onEditClicked(view: View) {
        val intent = Intent(activity, RecipeEditActivity::class.java)
        intent.putExtra("rid", recipeId)
        activity.startActivity(intent)
    }

    companion object {
        const val EXTRA_PARAMETER_RID = "rid"
    }
}