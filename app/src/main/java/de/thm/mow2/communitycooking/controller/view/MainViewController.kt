package de.thm.mow2.communitycooking.controller.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.IModelViewController
import de.thm.mow2.communitycooking.controller.model.MainModelController
import de.thm.mow2.communitycooking.databinding.ActivityMainBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.Location
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.AuthenticationService
import de.thm.mow2.communitycooking.view.activity.*
import de.thm.mow2.communitycooking.view.adapter.NearEventsAdapter
import de.thm.mow2.communitycooking.view.adapter.ParticipatedEventsAdapter
import de.thm.mow2.communitycooking.view.adapter.RequestedEventsAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import de.thm.tp.library.gps.TPLocationListener

class MainViewController(
    private val activity: MainActivity,
    private val binding: ActivityMainBinding
) : IModelViewController {

    private val modelController: MainModelController = MainModelController(this)

    private val randomTextUser = arrayOf(
        "Heute ist ein schöner Tag zum kochen!",
        "So viele Rezepte! Was kochst Du nach?"
    )
    private val welcomeText = arrayOf("Willkommen", "Hallo", "Moin")

    private var user: User? = null

    var userLocation: Location? = null

    private val locationManager: TPLocationListener = TPLocationListener(activity)
    private var requestPermissionResultLauncher: ActivityResultLauncher<String>

    init {
        binding.apply {
            textViewRandomText.text = changeRandomText()

            swipeRefreshLayout.setOnRefreshListener(::updateLocation)
            cardViewEditProfile.setOnClickListener(::onViewProfileClicked)
            cardViewRecipeListShow.setOnClickListener(::onViewRecipeClicked)
            buttonSignOut.setOnClickListener(::signOut)
            buttonCreateEvent.setOnClickListener(::redirectCreateEvent)

            recyclerViewNearByEvents.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
            recyclerViewParticipatingEvents.layoutManager = LinearLayoutManager(activity)
            recyclerViewRequestedEvents.layoutManager = LinearLayoutManager(activity)
            recyclerViewNearByEvents.adapter = NearEventsAdapter(this@MainViewController)
            recyclerViewParticipatingEvents.adapter = ParticipatedEventsAdapter()
            recyclerViewRequestedEvents.adapter = RequestedEventsAdapter(this@MainViewController)
        }

        requestPermissionResultLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::onRequestPermissionResult
        )
        loadData()
    }

    fun loadData() {
        modelController.downloadData()
    }

    fun updateLocation() {
        if (TPLocationListener.hasPermission(activity)) {
            locationManager.setCallback { location ->
                user?.let { user ->
                    userLocation = Location(location.time, location.latitude, location.longitude)
                    modelController.downloadLocationBasedData(user, userLocation!!)
                }
            }
            locationManager.getCurrentLocation()
            binding.progressBarSearchingIndicator.visibility = View.VISIBLE
        } else {
            if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissionResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                MaterialAlertDialogBuilder(activity)
                    .setMessage(activity.resources.getString(R.string.error_location_access_denied))
                    .setNeutralButton(activity.resources.getString(R.string.ignore)) { _, _ -> }
                    .setPositiveButton(activity.resources.getString(R.string.fix)) { _, _ ->
                        Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            addCategory(Intent.CATEGORY_DEFAULT)
                            data = Uri.parse("package:" + activity.packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                            activity.startActivity(this)
                        }
                    }
                    .show()
            }
        }
    }


    private fun onViewProfileClicked(view: View) {
        activity.startActivity(Intent(activity, ProfileEditActivity::class.java))
    }


    private fun onViewRecipeClicked(view: View) {
        activity.startActivity(Intent(activity, RecipeListActivity::class.java))
    }

    fun setRequestForEvent(event: Event) {
        modelController.setRequestForEvent(user!!, event)
    }

    fun removeRequestForEvent(event: Event) {
        modelController.removeRequestForEvent(user!!, event)
    }

    private fun changeUserWelcomeText() {
        binding.textViewUserWelcome.text =
            this.user?.let { "${welcomeText.random()} ${it.forename}!" }
        if (user == null) {
            TPFirebaseAuthentication.getUser()?.displayName?.let { displayName ->
                binding.textViewUserWelcome.text = "${welcomeText.random()} $displayName!"
            }
        }
    }

    private fun changeRandomText(): String {
        return randomTextUser.random()
    }

    private fun signOut(view: View) {
        AuthenticationService.signOut()
        activity.startActivity(Intent(activity, LoginActivity::class.java))
        activity.finish()
    }

    private fun redirectCreateEvent(view: View) {
        activity.startActivity(Intent(activity, EventEditActivity::class.java))
    }

    private fun onRequestPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            updateLocation()
        }
    }

    override fun showErrorMessage(message: String) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .apply {
                setAction("OK") { dismiss() }
            }.show()
    }

    override fun setRequestedEvents(events: List<Event>) {
        binding.apply {
            (recyclerViewRequestedEvents.adapter as RequestedEventsAdapter).events.replaceAll(events)

            textViewRequestedEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            dividerRequestedEvents.visibility = if (events.isEmpty()) View.GONE else View.VISIBLE
            recyclerViewRequestedEvents.visibility =
                if (events.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun setParticipatingEvents(events: List<Event>) {
        binding.apply {
            (recyclerViewParticipatingEvents.adapter as ParticipatedEventsAdapter).events.replaceAll(
                events
            )

            textViewParticipatingEvents.visibility =
                if (events.isEmpty()) View.GONE else View.VISIBLE
            dividerParticipatingEvents.visibility =
                if (events.isEmpty()) View.GONE else View.VISIBLE
            recyclerViewParticipatingEvents.visibility =
                if (events.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun setNearEvents(events: List<Event>) {
        binding.apply {
            progressBarSearchingIndicator.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
            (recyclerViewNearByEvents.adapter as NearEventsAdapter).events.replaceAll(events)
            textViewNearByEvents.text = when {
                events.size == 1 -> "${events.size} Event in Deiner Nähe"
                events.size > 1 -> "${events.size} Events in Deiner Nähe"
                else -> "Leider gibt es keine Events in Deiner Nähe"
            }
        }
    }

    override fun setUser(user: User) {
        this.user = user
        changeUserWelcomeText()
        FirebaseStorageService.downloadImageIntoImageView(
            user.image,
            binding.imageViewProfile,
            R.mipmap.default_user
        )
    }
}