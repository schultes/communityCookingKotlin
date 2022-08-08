package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication

interface IProfileEditViewController {
    fun redirectToMainActivity()
    fun exitActivity()
    fun showErrorMessage(message: String)
}

class ProfileEditModelController(private val viewController: IProfileEditViewController) {

    fun onSaveClicked(
        username: String,
        firstname: String,
        lastname: String,
        email: String,
        description: String,
        image: String,
        preference: RecipeType,
        radius: Int,
        create: Boolean
    ) {
        TPFirebaseAuthentication.getUser()?.let { user ->
            val newUser = User(
                user.uid,
                if (create) "@$username" else username,
                firstname,
                lastname,
                description,
                image,
                preference,
                radius
            )

            if (create) {
                DatabaseService.isUsernameUnique(newUser.username) { isUnique ->
                    if (isUnique) {
                        DatabaseService.setUser(newUser) { error ->
                            error?.also { message ->
                                viewController.showErrorMessage(message)
                            } ?: run {
                                if (create) viewController.redirectToMainActivity() else viewController.exitActivity()
                            }
                        }
                    }
                }
            } else {
                DatabaseService.setUser(newUser) { error ->
                    error?.also { message ->
                        viewController.showErrorMessage(message)
                    } ?: run {
                        if (create) viewController.redirectToMainActivity() else viewController.exitActivity()
                    }
                }
            }

            if (email != user.email) {
                TPFirebaseAuthentication.updateCurrentUserEmail(email) { error ->
                    error?.let { message -> viewController.showErrorMessage(message) }
                }
            }
        }
    }
}