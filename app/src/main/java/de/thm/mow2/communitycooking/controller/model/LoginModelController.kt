package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.service.AuthenticationService
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication


interface ILoginViewController {
    fun redirectToMainActivity()
    fun redirectToProfileEditActivity()
    fun showErrorMessage(message: String)
}

class LoginModelController(private val viewController: ILoginViewController) {

    fun onLoginClicked(email: String, password: String) {
        AuthenticationService.signIn(email, password) { result, error ->
            result?.let { firebaseAuthenticationUser ->
                DatabaseService.getMyself { result, error ->
                    result?.let { user ->
                        viewController.redirectToMainActivity()
                    }

                    if (result == null && TPFirebaseAuthentication.isSignedIn())
                        viewController.redirectToProfileEditActivity()

                    error?.let { message ->
                        viewController.showErrorMessage(message)
                    }
                }
            }
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }
}