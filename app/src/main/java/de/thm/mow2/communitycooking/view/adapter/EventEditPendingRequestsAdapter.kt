package de.thm.mow2.communitycooking.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementEventEditPendingRequestsBinding
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity

class EventEditPendingRequestsAdapter(private val participantsAdapter: EventEditParticipantsAdapter) :
    RecyclerView.Adapter<EventEditPendingRequestsAdapter.EventEditPendingRequestsViewHolder>() {
    var requests = mutableListOf<User>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EventEditPendingRequestsViewHolder {
        return EventEditPendingRequestsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_element_event_edit_pending_requests,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: EventEditPendingRequestsViewHolder, position: Int) {
        holder.binding.textViewUsername.text = requests[position].username
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    class EventEditPendingRequestsViewHolder(
        itemView: View,
        private val adapter: EventEditPendingRequestsAdapter
    ) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementEventEditPendingRequestsBinding.bind(itemView)

        init {
            binding.apply {
                textViewUsername.setOnClickListener(::onUsernameClicked)
                buttonAccept.setOnClickListener(::onAcceptClicked)
                buttonDecline.setOnClickListener(::onDeclineClicked)
            }
        }

        private fun onAcceptClicked(view: View) {
            adapter.participantsAdapter.participants.add(adapter.requests[layoutPosition])
            adapter.participantsAdapter.notifyDataSetChanged()
            adapter.requests.removeAt(layoutPosition)
            adapter.notifyDataSetChanged()
        }

        private fun onDeclineClicked(view: View) {
            adapter.requests.removeAt(layoutPosition)
            adapter.notifyDataSetChanged()
        }

        private fun onUsernameClicked(view: View) {
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(
                ProfileShowViewController.EXTRA_PARAMETER_UID,
                adapter.requests[layoutPosition].documentId
            )
            view.context.startActivity(intent)
        }
    }
}
