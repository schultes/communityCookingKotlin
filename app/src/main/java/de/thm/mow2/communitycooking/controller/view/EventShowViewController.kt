package de.thm.mow2.communitycooking.controller.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.EventShowModelController
import de.thm.mow2.communitycooking.controller.model.IEventShowViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventShowBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.view.activity.*
import de.thm.mow2.communitycooking.view.adapter.EventShowParticipantsAdapter
import de.thm.mow2.communitycooking.view.adapter.IngredientsListAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import java.text.SimpleDateFormat
import java.util.*

class EventShowViewController(
    private val activity: EventShowActivity,
    private val binding: ActivityEventShowBinding
) : IEventShowViewController {

    private val modelController: EventShowModelController = EventShowModelController(this)
    private val eventID: String = activity.intent.getStringExtra(EXTRA_PARAMETER_EVENT_ID).orEmpty()
    private var event: Event? = null

    init {
        binding.apply {
            buttonShowUser.setOnClickListener(::onShowUserClicked)
            cardViewRecipeCard.setOnClickListener(::onRecipeClicked)
            buttonChat.setOnClickListener(::onChatClicked)
            fabEdit.setOnClickListener(::onEditClicked)
            buttonExitEvent.setOnClickListener(::onExitEventClicked)
            buttonBack.setOnClickListener(::onBackClicked)

            recyclerViewIngredientsList.adapter = IngredientsListAdapter(activity)
            recyclerViewIngredientsList.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            recyclerViewParticipants.adapter = EventShowParticipantsAdapter()
            recyclerViewParticipants.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun setData(userId: String, event: Event) {
        this.event = event
        binding.apply {
            if (event.owner.documentId == TPFirebaseAuthentication.getUser()?.uid) {
                fabEdit.visibility = View.VISIBLE
                buttonExitEvent.visibility = View.GONE
            } else {
                fabEdit.visibility = View.GONE
                buttonExitEvent.visibility = View.VISIBLE
            }

            textViewEventTitle.text = "Willkommen bei\n${event.title}!"
            textViewDate.text =
                SimpleDateFormat("dd. MMMM yyyy HH:mm").format(Date(event.timestamp.toLong() * 1000))
            buttonShowUser.text = event.owner.fullname
            textViewEventDescription.text = event.description
            textViewRecipeOwner.text = event.recipe.owner.fullname
            textViewRecipeTitle.text = event.recipe.title
            textViewRecipeDescription.text = event.recipe.description
            textViewRecipeTime.text = "${event.recipe.duration} Min."
            textViewRecipeType.text =
                activity.resources.getStringArray(R.array.recipeTypes)[RecipeType.values()
                    .indexOf(event.recipe.type)]

            textViewPortions.text = (event.participating.size + 1).toString()

            (recyclerViewIngredientsList.adapter as? IngredientsListAdapter)?.updateContents(
                event.recipe.ingredients,
                event.recipe.portionSize,
                event.participating.size + 1
            )

            (recyclerViewParticipants.adapter as? EventShowParticipantsAdapter)?.participants =
                event.participating

            FirebaseStorageService.downloadImageIntoImageView(
                event.recipe.image,
                imageViewEventImage,
                R.mipmap.default_recipe
            )
            FirebaseStorageService.downloadImageIntoImageView(
                event.recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
        }
    }

    fun loadEvent() {
        modelController.downloadData(eventID)
    }

    private fun onExitEventClicked(view: View) {
        MaterialAlertDialogBuilder(activity)
            .setMessage(activity.resources.getString(R.string.event_exit_confirmation))
            .setNeutralButton(activity.resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(activity.resources.getString(R.string.confirm)) { _, _ ->
                event?.let { event -> modelController.leaveEvent(event) }
                exitActivity()
            }
            .show()

    }

    override fun exitActivity() {
        activity.finish()
    }

    private fun onShowUserClicked(view: View) {
        event?.let { event ->
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(ProfileShowViewController.EXTRA_PARAMETER_UID, event.owner.documentId)
            view.context.startActivity(intent)
        }
    }

    private fun onChatClicked(view: View) {
        event?.let { event ->
            val intent = Intent(activity, EventChatActivity::class.java)
            intent.putExtra(EventChatViewController.EXTRA_PARAMETER_EVENT_ID, event.documentId)
            activity.startActivity(intent)
        }
    }

    private fun onEditClicked(view: View) {
        event?.let { event ->
            if (event.owner.documentId == TPFirebaseAuthentication.getUser()?.uid) {
                val intent = Intent(activity, EventEditActivity::class.java)
                intent.putExtra(EventEditViewController.EXTRA_PARAMETER_EVENT_ID, eventID)
                activity.startActivity(intent)
            }
        }
    }

    private fun onBackClicked(view: View) {
        activity.finish()
    }

    private fun onRecipeClicked(view: View) {
        val intent = Intent(activity, RecipeShowActivity::class.java)
        intent.putExtra(RecipeShowViewController.EXTRA_PARAMETER_RID, event!!.recipe.documentId)
        activity.startActivity(intent)
    }

    companion object {
        const val EXTRA_PARAMETER_EVENT_ID = "event_id"
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }
}