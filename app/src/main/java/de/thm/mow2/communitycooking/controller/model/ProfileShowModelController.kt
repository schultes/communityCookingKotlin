package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.DatabaseService


interface IProfileShowViewController {
    fun setData(user: User, recipes: List<Recipe>)
    fun showErrorMessage(message: String)
}

class ProfileShowModelController(private val viewController: IProfileShowViewController) {
    fun downloadData(uid: String) {
        DatabaseService.getUserById(uid) { result, error ->
            result?.let { user ->

                DatabaseService.getRecipesFromUser(user) { result, error ->
                    result?.let { recipes ->
                        viewController.setData(user, recipes)
                    }

                    error?.let { message ->
                        viewController.showErrorMessage(message)
                    }
                }
                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        }
    }
}