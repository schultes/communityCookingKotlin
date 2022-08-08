package de.thm.mow2.communitycooking.controller.view

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.controller.model.IRecipeListViewController
import de.thm.mow2.communitycooking.controller.model.RecipeListModelController
import de.thm.mow2.communitycooking.databinding.ActivityRecipeListBinding
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.view.activity.RecipeEditActivity
import de.thm.mow2.communitycooking.view.activity.RecipeListActivity
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.adapter.RecipeListAdapter

class RecipeListViewController(
    private val activity: RecipeListActivity,
    private val binding: ActivityRecipeListBinding
) : IRecipeListViewController {

    private var modelController: RecipeListModelController = RecipeListModelController(this)
    private var recipes: List<Recipe>

    init {
        binding.apply {
            buttonBack.setOnClickListener(::onBackClicked)
            chipVegetarian.setOnClickListener(::onFilterChanged)
            chipVegan.setOnClickListener(::onFilterChanged)
            editTextSearch.doOnTextChanged(::onQueryTextChanged)
            recyclerViewRecipes.adapter = RecipeListAdapter(::onItemSelected, ::onItemLongPressed)
            recyclerViewRecipes.layoutManager = LinearLayoutManager(activity)
            fabAdd.setOnClickListener(::onAddClicked)
        }

        if (activity.intent.getBooleanExtra(EXTRA_PARAMETER_IS_SELECT_RECIPE, false))
            showErrorMessage("Tippe auf das Event, das du dem Rezept Hinzufügen möchtest!")
        recipes = emptyList()

        modelController.downloadData()
    }

    private fun onQueryTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        modelController.filterRecipes(
            recipes,
            text.toString(),
            binding.chipVegetarian.isChecked,
            binding.chipVegan.isChecked
        )
    }

    private fun onFilterChanged(view: View) {
        binding.apply {
            modelController.filterRecipes(
                recipes,
                editTextSearch.text.toString(),
                chipVegetarian.isChecked,
                chipVegan.isChecked
            )
        }
    }

    private fun onItemSelected(recipe: Recipe) {
        if (activity.intent.getBooleanExtra(EXTRA_PARAMETER_IS_SELECT_RECIPE, false)) {
            activity.intent.putExtra(EXTRA_PARAMETER_RECIPE_ID, recipe.documentId)
            activity.setResult(Activity.RESULT_OK, activity.intent)
            activity.finish()
        } else {
            val intent = Intent(activity, RecipeShowActivity::class.java)
            intent.putExtra(RecipeShowViewController.EXTRA_PARAMETER_RID, recipe.documentId)
            activity.startActivity(intent)
        }
    }

    private fun onItemLongPressed(recipe: Recipe) {
        val intent = Intent(activity, RecipeShowActivity::class.java)
        intent.putExtra(RecipeShowViewController.EXTRA_PARAMETER_RID, recipe.documentId)
        activity.startActivity(intent)
    }

    private fun onBackClicked(view: View) {
        activity.finish()
    }

    private fun onAddClicked(view: View) {
        val intent = Intent(activity, RecipeEditActivity::class.java)
        activity.startActivity(intent)
    }

    override fun setRecipes(recipes: List<Recipe>) {
        this.recipes = recipes
        (binding.recyclerViewRecipes.adapter as RecipeListAdapter).recipes.replaceAll(recipes)
        binding.progressBar.visibility = View.GONE
    }

    override fun setFilteredRecipes(recipes: List<Recipe>) {
        (binding.recyclerViewRecipes.adapter as RecipeListAdapter).recipes.replaceAll(recipes)
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    companion object {
        const val EXTRA_PARAMETER_IS_SELECT_RECIPE = "is_select_recipe"
        const val EXTRA_PARAMETER_RECIPE_ID = "recipe_id"
    }
}