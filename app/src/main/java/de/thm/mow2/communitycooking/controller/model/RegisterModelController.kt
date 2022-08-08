package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.service.AuthenticationService
import de.thm.mow2.communitycooking.model.service.DatabaseService


interface IRegisterViewController {
    fun redirectToProfileEditActivity()
    fun showErrorMessage(message: String)
}

class RegisterModelController(private val viewController: IRegisterViewController) {

    fun onRegisterClicked(email: String, password: String, username: String) {
        DatabaseService.isUsernameUnique("@$username") { unique ->
            if (unique) {
                AuthenticationService.signUp(email, password, username) { result, error ->
                    result?.let { user ->
                        viewController.redirectToProfileEditActivity()
                    }
                    error?.let { message ->
                        viewController.showErrorMessage(message)
                    }
                }
            } else {
                viewController.showErrorMessage("Benutzername ist bereits vergeben!")
            }
        }
    }
}