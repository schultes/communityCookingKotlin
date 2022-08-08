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
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.controller.view.RecipeShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementMainEventsRequestedBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService


class RequestedEventsAdapter(
    private val viewController: MainViewController
) : RecyclerView.Adapter<RequestedEventsAdapter.ViewHolder>() {
    val events: SortedList<Event> = SortedList(
        Event::class.java,
        object : SortedListAdapterCallback<Event>(this) {
            override fun compare(o1: Event, o2: Event): Int =
                if (o1.timestamp == o2.timestamp) o1.documentId!!.compareTo(o2.documentId!!) else o1.timestamp.compareTo(
                    o2.timestamp
                )

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem.documentId == newItem.documentId && oldItem.title == newItem.title && oldItem.recipe.documentId == newItem.recipe.documentId && oldItem.recipe.image == newItem.recipe.image

            override fun areItemsTheSame(item1: Event, item2: Event): Boolean =
                areContentsTheSame(item1, item2)
        })

    inner class ViewHolder(itemView: View, val adapter: RequestedEventsAdapter) :
        RecyclerView.ViewHolder(itemView) {

        val binding = ListElementMainEventsRequestedBinding.bind(itemView)

        init {
            binding.apply {
                buttonWithdrawRequest.setOnClickListener(::onWithdrawRequestClicked)
                cardViewImageContainer.setOnClickListener(::onImageClicked)
                textViewUsername.setOnClickListener(::onOwnerClicked)
            }
        }

        private fun onOwnerClicked(view: View) {
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(
                ProfileShowViewController.EXTRA_PARAMETER_UID,
                adapter.events[layoutPosition].owner.documentId
            )
            view.context.startActivity(intent)
        }

        private fun onImageClicked(view: View) {
            val intent = Intent(view.context, RecipeShowActivity::class.java)
            intent.putExtra(
                RecipeShowViewController.EXTRA_PARAMETER_RID,
                adapter.events[layoutPosition].recipe.documentId
            )
            view.context.startActivity(intent)
        }

        private fun onWithdrawRequestClicked(view: View) {
            openAlert(view.context, events[layoutPosition])
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestedEventsAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_element_main_events_requested, parent, false), this
        )
    }

    override fun onBindViewHolder(viewHolder: RequestedEventsAdapter.ViewHolder, position: Int) {
        viewHolder.binding.apply {
            textViewEventTitle.text = events[position].title
            textViewUsername.text = events[position].owner.fullname

            FirebaseStorageService.downloadImageIntoImageView(
                events[position].recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
        }
    }

    fun openAlert(context: Context, data: Event) {
        MaterialAlertDialogBuilder(context)
            .setMessage("Möchtest Du nicht mehr an diesem Event teilnehmen?")
            .setTitle(data.title)
            .setPositiveButton("Bestätigen") { _, _ ->
                viewController.removeRequestForEvent(data)
                events.remove(data)
                viewController.showErrorMessage("Du nimmst nicht mehr an ${data.title} teil!")
            }
            .setNegativeButton("Abbrechen") { _, _ ->
                viewController.showErrorMessage("Vorgang wurde abgebrochen!")
            }
            .show()
    }

    override fun getItemCount() = events.size()
}