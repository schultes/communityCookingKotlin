package de.thm.mow2.communitycooking.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.MainViewController
import de.thm.mow2.communitycooking.controller.view.RecipeShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementMainEventsNearByBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.service.LocationService
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.text.SimpleDateFormat
import java.util.*

class NearEventsAdapter(private val viewController: MainViewController) :
    RecyclerView.Adapter<NearEventsAdapter.ViewHolder>() {
    val events: SortedList<Event> = SortedList(
        Event::class.java,
        object : SortedListAdapterCallback<Event>(this) {
            override fun compare(o1: Event, o2: Event): Int =
                LocationService.calculateDistance(viewController.userLocation!!, o1.location)
                    .compareTo(
                        LocationService.calculateDistance(
                            viewController.userLocation!!,
                            o2.location
                        )
                    )

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(item1: Event, item2: Event): Boolean = item1 == item2
        })


    inner class ViewHolder(view: View, private val adapter: NearEventsAdapter) :
        RecyclerView.ViewHolder(view) {

        val binding = ListElementMainEventsNearByBinding.bind(view)

        init {
            binding.cardViewImageContainer.setOnClickListener(::onImageClicked)
            binding.buttonSendRequest.setOnClickListener(::onSendRequestClicked)
        }

        private fun onImageClicked(view: View) {
            val intent = Intent(view.context, RecipeShowActivity::class.java)
            intent.putExtra(
                RecipeShowViewController.EXTRA_PARAMETER_RID,
                adapter.events[layoutPosition].recipe.documentId
            )
            view.context.startActivity(intent)
        }

        private fun onSendRequestClicked(view: View) {
            adapter.openAlert(view.context, adapter.events[layoutPosition])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_element_main_events_near_by, parent, false), this
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = events[position]
        holder.binding.apply {
            textViewTitle.text = data.title
            textViewDescription.text = data.description
            textViewTimestamp.text =
                SimpleDateFormat("dd. MMMM yyyy HH:mm").format(Date(data.timestamp.toLong() * 1000))
            textViewDistance.text = "Das Event ist nur ${
                String.format(
                    "%.1f",
                    LocationService.calculateDistance(viewController.userLocation!!, data.location)
                )
            } km von Dir entfernt!"

            FirebaseStorageService.downloadImageIntoImageView(
                data.recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
        }

    }

    fun openAlert(context: Context, data: Event) {
        MaterialAlertDialogBuilder(context)
            .setMessage("Möchtest Du an diesem Event teilnehmen?")
            .setTitle(data.title)
            .setPositiveButton("Bestätigen") { _, _ ->
                viewController.setRequestForEvent(data)
                events.remove(data)
                viewController.showErrorMessage("Die Anfrage wurde verschickt!")
            }
            .setNegativeButton("Abbrechen") { _, _ ->
                viewController.showErrorMessage("Du hast keine Anfrage verschickt!")
            }
            .show()
    }

    override fun getItemCount() = events.size()
}