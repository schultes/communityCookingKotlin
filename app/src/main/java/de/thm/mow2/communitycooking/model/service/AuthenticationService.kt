package de.thm.mow2.communitycooking.model.service

import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthenticationUser

class AuthenticationService {
    companion object {
        fun signUp(
            email: String,
            password: String,
            username: String,
            callback: (TPFirebaseAuthenticationUser?, String?) -> Unit
        ) {
            TPFirebaseAuthentication.signUp(email, password, username) { result, error ->
                result?.let { user ->
                    callback(user, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun signIn(
            email: String,
            password: String,
            callback: (TPFirebaseAuthenticationUser?, String?) -> Unit
        ) {
            TPFirebaseAuthentication.signIn(email, password) { result, error ->
                result?.let { user ->
                    callback(user, null)
                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        fun signOut() {
            TPFirebaseAuthentication.signOut()
        }
    }
}