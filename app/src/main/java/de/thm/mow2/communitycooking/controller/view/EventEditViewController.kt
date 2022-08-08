package de.thm.mow2.communitycooking.controller.view

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.EventEditModelController
import de.thm.mow2.communitycooking.controller.model.IEventEditViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventEditBinding
import de.thm.mow2.communitycooking.model.*
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.mow2.communitycooking.view.activity.EventEditActivity
import de.thm.mow2.communitycooking.view.activity.RecipeListActivity
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.adapter.EventEditParticipantsAdapter
import de.thm.mow2.communitycooking.view.adapter.EventEditPendingRequestsAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import de.thm.tp.library.gps.TPLocation
import de.thm.tp.library.gps.TPLocationListener
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

class EventEditViewController(
    private val activity: EventEditActivity,
    private val binding: ActivityEventEditBinding
) : IEventEditViewController {

    private val modelController: EventEditModelController = EventEditModelController(this)
    private val eventID: String? = activity.intent.getStringExtra(EXTRA_PARAMETER_EVENT_ID)
    private val locationListener: TPLocationListener
    private var event: Event? = null
    private var recipe: Recipe? = null
    private var location: Location? = null
    private var date = Date(Instant.now().toEpochMilli())
    private var startForResult: ActivityResultLauncher<Intent>


    init {
        binding.apply {
            buttonBack.setOnClickListener(::onBackClicked)
            buttonTime.setOnClickListener(::onTimeClicked)
            buttonDate.setOnClickListener(::onDateClicked)
            buttonLocation.setOnClickListener { getLocation() }
            cardViewRecipeCard.setOnClickListener(::onRecipeClicked)
            buttonChangeRecipe.setOnClickListener(::onChangeRecipeClicked)
            fabSave.setOnClickListener(::onSaveClicked)
            buttonDelete.setOnClickListener(::onDeleteClicked)

            editTextEventTitle.doOnTextChanged { text, _, _, _ ->
                inputLayoutEventTitle.error =
                    if (text.isNullOrBlank()) "Kein Titel eingegeben!" else null
            }

            editTextEventDescription.doOnTextChanged { text, _, _, _ ->
                inputLayoutEventDescription.error =
                    if (text.isNullOrBlank()) "Keine Beschreibung eingegeben!" else null
            }

            recyclerViewParticipants.adapter = EventEditParticipantsAdapter()
            recyclerViewParticipants.layoutManager = LinearLayoutManager(activity)

            recyclerViewRequests.adapter =
                EventEditPendingRequestsAdapter(recyclerViewParticipants.adapter as EventEditParticipantsAdapter)
            recyclerViewRequests.layoutManager = LinearLayoutManager(activity)

        }

        locationListener = TPLocationListener(activity)
        locationListener.setCallback(::onLocationResult)

        startForResult = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ::onSelectRecipeResult
        )

        loadEvent()
    }

    private fun loadEvent() {
        modelController.downloadData(eventID)
    }

    override fun setDataNewEvent(user: User) {
        binding.apply {
            textViewUsername.text = user.username
            buttonTime.text = SimpleDateFormat("HH:mm").format(date)
            buttonDate.text = SimpleDateFormat("dd.MM.yy").format(date)
            constrainedLayoutRecipe.children.iterator()
                .forEach { view -> view.visibility = View.GONE }
            constrainedLayoutEditOnly.children.iterator()
                .forEach { view -> view.visibility = View.GONE }
            getLocation()
            buttonChangeRecipe.text = activity.resources.getString(R.string.chose_recipe)

            FirebaseStorageService.downloadImageIntoImageView(
                user.image,
                imageViewProfileImage,
                R.mipmap.default_user
            )
        }
    }

    override fun setData(event: Event) {
        this.event = event
        binding.apply {
            textViewUsername.text = event.owner.username
            editTextEventTitle.setText(event.title)
            editTextEventDescription.setText(event.description)

            date.time = event.timestamp.toLong() * 1000
            buttonTime.text = SimpleDateFormat("HH:mm").format(date)
            buttonDate.text = SimpleDateFormat("dd.MM.yy").format(date)

            progressBarLocationLoading.visibility = View.GONE
            location = event.location
            textViewLongitude.text = event.location.longitude.toString()
            textViewLatitude.text = event.location.latitude.toString()
            switchIsPublic.isChecked = event.isPublic
            recipe = event.recipe
            loadRecipe()

            (recyclerViewParticipants.adapter as? EventEditParticipantsAdapter)?.participants =
                event.participating.toMutableList()

            (recyclerViewRequests.adapter as? EventEditPendingRequestsAdapter)?.requests =
                event.requested.toMutableList()

            FirebaseStorageService.downloadImageIntoImageView(
                event.owner.image,
                imageViewProfileImage,
                R.mipmap.default_user
            )
        }
    }

    private fun loadRecipe() {
        recipe?.let { recipe ->
            binding.apply {
                textViewRecipeOwner.text = recipe.owner.fullname
                textViewRecipeTime.text = "${recipe.duration} Min."
                textViewRecipeTitle.text = recipe.title
                textViewRecipeDescription.text = recipe.description
                textViewRecipeType.text =
                    activity.resources.getStringArray(R.array.recipeTypes)[RecipeType.values()
                        .indexOf(recipe.type)]

                constrainedLayoutRecipe.children.iterator()
                    .forEach { view -> view.visibility = View.VISIBLE }
                buttonChangeRecipe.text = activity.resources.getString(R.string.change_recipe)
                FirebaseStorageService.downloadImageIntoImageView(
                    recipe.image,
                    imageViewRecipeImage,
                    R.mipmap.default_recipe
                )
            }
        }

    }

    private fun onTimeClicked(view: View) {
        val cal = Calendar.getInstance()
        cal.time = date
        val picker = TimePickerDialog(
            activity,
            { _: TimePicker, hour: Int, minute: Int ->
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                date = cal.time
                binding.buttonTime.text = SimpleDateFormat("HH:mm").format(date)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        )
        picker.show()
    }

    private fun onDateClicked(view: View) {
        val cal = Calendar.getInstance()
        cal.time = date
        val picker = DatePickerDialog(
            activity,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                date = cal.time
                binding.buttonDate.text = SimpleDateFormat("dd.MM.yy").format(date)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        picker.show()
    }

    private fun onLocationResult(location: TPLocation) {
        this.location = Location(
            Instant.now().epochSecond.toDouble(),
            location.latitude,
            location.longitude
        )
        binding.apply {
            textViewLatitude.text = location.latitude.toString()
            textViewLongitude.text = location.longitude.toString()
            progressBarLocationLoading.visibility = View.GONE
            buttonLocation.visibility = View.VISIBLE
        }
    }

    private fun onSelectRecipeResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.getStringExtra(RecipeListViewController.EXTRA_PARAMETER_RECIPE_ID)
                ?.let {
                    DatabaseService.getRecipeById(it) { recipe, error ->
                        recipe?.let {
                            this.recipe = recipe
                            loadRecipe()
                        }
                    }
                }
        }
    }

    private fun onSaveClicked(view: View) {
        binding.apply {
            if (!editTextEventTitle.text.isNullOrEmpty() && !editTextEventDescription.text.isNullOrEmpty() && recipe != null && location != null) {
                recipe?.let { recipe ->
                    location?.let { location ->
                        event?.also { event ->
                            modelController.onSaveClicked(
                                (date.time / 1000).toDouble(),
                                event.owner,
                                Location(
                                    Instant.now().epochSecond.toDouble(),
                                    location.latitude,
                                    location.longitude
                                ),
                                switchIsPublic.isChecked,
                                editTextEventTitle.text.toString(),
                                editTextEventDescription.text.toString(),
                                recipe,
                                (recyclerViewRequests.adapter as EventEditPendingRequestsAdapter).requests,
                                (recyclerViewParticipants.adapter as EventEditParticipantsAdapter).participants,
                                event.documentId
                            )
                        } ?: run {
                            DatabaseService.getMyself { result, error ->
                                result?.let { user ->
                                    modelController.onSaveClicked(
                                        (date.time / 1000).toDouble(),
                                        user,
                                        Location(
                                            Instant.now().epochSecond.toDouble(),
                                            location.latitude,
                                            location.longitude
                                        ),
                                        switchIsPublic.isChecked,
                                        editTextEventTitle.text.toString(),
                                        editTextEventDescription.text.toString(),
                                        recipe,
                                        emptyList(),
                                        emptyList(),
                                        null
                                    )
                                }
                                error?.let {
                                    showErrorMessage(error)
                                }
                            }
                        }
                    }

                }
            } else {
                showErrorMessage(activity.resources.getString(R.string.error_edit_event_incomplete_information))
            }
        }

    }

    private fun getLocation() {
        binding.apply {
            if (TPLocationListener.hasPermission(activity)) {
                progressBarLocationLoading.visibility = View.VISIBLE
                buttonLocation.visibility = View.INVISIBLE
                locationListener.getCurrentLocation()
            } else {
                progressBarLocationLoading.visibility = View.GONE
                buttonLocation.visibility = View.VISIBLE
                textViewLongitude.text = "kein Längengrad ermittelt"
                textViewLatitude.text = "kein Breitengrad ermittelt"
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    activity.requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                } else {
                    MaterialAlertDialogBuilder(activity)
                        .setMessage(activity.resources.getString(R.string.error_location_access_denied))
                        .setNeutralButton(activity.resources.getString(R.string.ignore)) { _, _ -> }
                        .setPositiveButton(activity.resources.getString(R.string.fix)) { _, _ ->
                            val i = Intent()
                            i.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            i.addCategory(Intent.CATEGORY_DEFAULT)
                            i.data = Uri.parse("package:" + activity.packageName)
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            activity.startActivity(i)
                        }
                        .show()
                }
            }
        }
    }

    private fun onDeleteClicked(view: View) {
        MaterialAlertDialogBuilder(activity)
            .setMessage(activity.resources.getString(R.string.event_delete_confirmation))
            .setNeutralButton(activity.resources.getString(R.string.cancel)) { _, _ -> }
            .setPositiveButton(activity.resources.getString(R.string.confirm)) { _, _ ->
                eventID?.let {
                    modelController.onDeleteClicked(eventID)
                }
            }
            .show()
    }

    private fun onChangeRecipeClicked(view: View) {
        val intent = Intent(activity, RecipeListActivity::class.java)
        intent.putExtra(RecipeListViewController.EXTRA_PARAMETER_IS_SELECT_RECIPE, true)
        startForResult.launch(intent)
    }

    private fun onRecipeClicked(view: View) {
        recipe?.let { recipe ->
            val intent = Intent(activity, RecipeShowActivity::class.java)
            intent.putExtra(RecipeShowViewController.EXTRA_PARAMETER_RID, recipe.documentId)
            activity.startActivity(intent)
        }
    }

    private fun onBackClicked(view: View) {
        exitActivity()
    }

    override fun exitActivity() {
        activity.finish()
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    companion object {
        const val EXTRA_PARAMETER_EVENT_ID = "event_id"
    }
}