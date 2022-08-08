package de.thm.mow2.communitycooking.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementEventEditParticipantBinding
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity

class EventEditParticipantsAdapter :
    RecyclerView.Adapter<EventEditParticipantsAdapter.EventEditParticipantsViewHolder>() {
    var participants = mutableListOf<User>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventEditParticipantsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EventEditParticipantsViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_event_edit_participant,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: EventEditParticipantsViewHolder, position: Int) {
        holder.binding.textViewUsername.text = participants[position].username
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    class EventEditParticipantsViewHolder(
        itemView: View,
        private val adapter: EventEditParticipantsAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementEventEditParticipantBinding.bind(itemView)

        init {
            binding.textViewUsername.setOnClickListener(::onUsernameClicked)
            binding.deleteButton.setOnClickListener(::onDeleteClicked)
        }

        private fun onDeleteClicked(view: View) {
            adapter.participants.removeAt(layoutPosition)
            adapter.notifyDataSetChanged()
        }

        private fun onUsernameClicked(view: View) {
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(
                ProfileShowViewController.EXTRA_PARAMETER_UID,
                adapter.participants[layoutPosition].documentId
            )
            view.context.startActivity(intent)
        }
    }
}