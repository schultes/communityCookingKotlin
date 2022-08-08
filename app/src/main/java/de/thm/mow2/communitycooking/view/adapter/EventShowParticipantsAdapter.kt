package de.thm.mow2.communitycooking.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementEventShowParticipantBinding
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService

class EventShowParticipantsAdapter :
    RecyclerView.Adapter<EventShowParticipantsAdapter.EventShowParticipantsViewHolder>() {
    var participants: List<User> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventShowParticipantsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EventShowParticipantsViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_event_show_participant,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: EventShowParticipantsViewHolder, position: Int) {
        holder.userId = participants[position].documentId
        holder.binding.apply {
            textViewUsername.text = participants[position].username

            FirebaseStorageService.downloadImageIntoImageView(
                participants[position].image,
                imageViewProfileImage,
                R.mipmap.default_user
            )
        }

    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class EventShowParticipantsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementEventShowParticipantBinding.bind(itemView)
        var userId: String = ""

        init {
            binding.cardView.setOnClickListener(::onUsernameClicked)
        }

        private fun onUsernameClicked(view: View) {
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(ProfileShowViewController.EXTRA_PARAMETER_UID, userId)
            view.context.startActivity(intent)
        }
    }
}