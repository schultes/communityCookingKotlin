package de.thm.mow2.communitycooking.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.EventShowViewController
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.controller.view.RecipeShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementMainEventsParticipatingBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.view.activity.EventShowActivity
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.text.SimpleDateFormat
import java.util.*

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
class ParticipatedEventsAdapter :
    RecyclerView.Adapter<ParticipatedEventsAdapter.ViewHolder>() {
    val events: SortedList<Event> = SortedList(
        Event::class.java,
        object : SortedListAdapterCallback<Event>(this) {
            override fun compare(o1: Event, o2: Event): Int =
                if (o1.timestamp == o2.timestamp) o1.documentId!!.compareTo(o2.documentId!!) else o1.timestamp.compareTo(
                    o2.timestamp
                )

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean =
                oldItem.documentId == newItem.documentId && oldItem.title == newItem.title && oldItem.timestamp == newItem.timestamp && oldItem.recipe.title == newItem.recipe.title && oldItem.recipe.documentId == newItem.recipe.documentId && oldItem.recipe.image == newItem.recipe.image

            override fun areItemsTheSame(item1: Event, item2: Event): Boolean =
                areContentsTheSame(item1, item2)
        })

    inner class ViewHolder(itemView: View, private val adapter: ParticipatedEventsAdapter) :
        RecyclerView.ViewHolder(itemView) {

        val binding = ListElementMainEventsParticipatingBinding.bind(itemView)

        init {
            binding.apply {
                cardView.setOnClickListener(::onItemClicked)
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

        private fun onItemClicked(view: View) {
            val intent = Intent(view.context, EventShowActivity::class.java)
            intent.putExtra(
                EventShowViewController.EXTRA_PARAMETER_EVENT_ID,
                adapter.events[layoutPosition].documentId
            )

            view.context.startActivity(intent)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipatedEventsAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.list_element_main_events_participating, parent, false), this
        )
    }


    override fun onBindViewHolder(viewHolder: ParticipatedEventsAdapter.ViewHolder, position: Int) {
        viewHolder.binding.apply {
            textViewEventTitle.text = events[position].title
            textViewUsername.text = events[position].owner.fullname
            textViewDate.text =
                SimpleDateFormat("dd. MMMM yyyy HH:mm").format(Date(events[position].timestamp.toLong() * 1000))
            textViewRecipeTitle.text = events[position].recipe.title

            FirebaseStorageService.downloadImageIntoImageView(
                events[position].recipe.image,
                imageViewRecipeImage,
                R.mipmap.default_recipe
            )
        }
    }

    override fun getItemCount() = events.size()
}