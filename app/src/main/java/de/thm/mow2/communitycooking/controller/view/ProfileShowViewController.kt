package de.thm.mow2.communitycooking.controller.view

import android.content.Intent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.IProfileShowViewController
import de.thm.mow2.communitycooking.controller.model.ProfileShowModelController
import de.thm.mow2.communitycooking.databinding.ActivityProfileShowBinding
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.view.activity.ProfileEditActivity
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.mow2.communitycooking.view.adapter.ProfileShowRecipeListAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication


class ProfileShowViewController(
    private val activity: ProfileShowActivity,
    private val binding: ActivityProfileShowBinding
) : IProfileShowViewController {

    private val modelController: ProfileShowModelController = ProfileShowModelController(this)

    init {
        binding.apply {
            fabEdit.setOnClickListener(::onRedirectToProfileEditActivity)
            buttonBack.setOnClickListener(::onExitClicked)

            recyclerviewRecipeList.adapter = ProfileShowRecipeListAdapter()
            recyclerviewRecipeList.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }

        loadUser()
    }

    fun loadUser() {
        activity.intent.getStringExtra(EXTRA_PARAMETER_UID)?.let { uid ->
            modelController.downloadData(uid)
            TPFirebaseAuthentication.getUser()?.uid?.let { myId ->
                if (myId != uid) {
                    binding.fabEdit.isVisible = false
                }
            }
        }
    }

    override fun setData(user: User, recipes: List<Recipe>) {
        binding.apply {
            textViewUsername.text = user.username
            textViewUserWelcome.text = "Hallo ich bin ${user.forename}"
            textViewDescription.text = user.description
            textViewPreference.text =
                activity.resources.getStringArray(R.array.recipeTypes)[RecipeType.values()
                    .indexOf(user.preference)]
            (recyclerviewRecipeList.adapter as? ProfileShowRecipeListAdapter)?.recipes = recipes

            FirebaseStorageService.downloadImageIntoImageView(
                user.image,
                imageViewProfileImage,
                R.mipmap.default_user
            )
        }


    }

    private fun onExitClicked(view: View) {
        activity.finish()
    }

    private fun onRedirectToProfileEditActivity(view: View) {
        activity.startActivity(Intent(activity, ProfileEditActivity::class.java))
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    companion object {
        const val EXTRA_PARAMETER_UID = "uid"
    }
}