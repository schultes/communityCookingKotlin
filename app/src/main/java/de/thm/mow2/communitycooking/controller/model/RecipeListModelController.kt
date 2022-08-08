package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.model.service.DatabaseService

interface IRecipeListViewController {
    fun setRecipes(recipes: List<Recipe>)
    fun setFilteredRecipes(recipes: List<Recipe>)
    fun showErrorMessage(message: String)
}

class RecipeListModelController(private val viewController: IRecipeListViewController) {
    fun downloadData() {
        DatabaseService.getAllRecipes { result, error ->
            result?.let { recipes ->
                viewController.setRecipes(recipes)
            }
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }

    fun filterRecipes(
        recipes: List<Recipe>,
        needle: String,
        isVegetarian: Boolean,
        isVegan: Boolean
    ) {
        if (recipes.isNotEmpty()) {
            var titleQuery = ""
            var usernameQuery = ""
            if (needle.startsWith('@')) {
                usernameQuery = needle
            } else {
                titleQuery = needle
            }

            viewController.setFilteredRecipes(recipes.filter { recipe ->
                if (usernameQuery.isNotEmpty()) recipe.owner.username.lowercase()
                    .contains(needle.lowercase()) else true
            }.filter { recipe ->
                if (titleQuery.isNotEmpty()) recipe.title.lowercase()
                    .contains(needle.lowercase()) else true
            }.filter { recipe ->
                when (recipe.type) {
                    RecipeType.VEGAN -> isVegan || (!isVegan && !isVegetarian)
                    RecipeType.VEGETARIAN -> isVegetarian || (!isVegan && !isVegetarian)
                    else -> (!isVegan && !isVegetarian)
                }
            })
        }
    }
}