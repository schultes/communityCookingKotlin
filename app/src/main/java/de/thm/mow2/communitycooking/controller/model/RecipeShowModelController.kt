package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication

interface IRecipeShowViewController {
    fun setData(userId: String, recipe: Recipe)
    fun showErrorMessage(message: String)
}

class RecipeShowModelController(private val viewController: IRecipeShowViewController) {
    fun downloadData(recipeId: String) {
        TPFirebaseAuthentication.getUser()?.uid?.let { userId ->
            DatabaseService.getRecipeById(recipeId) { result, error ->
                result?.let { recipe ->
                    viewController.setData(userId, recipe)
                }

                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        }
    }
}