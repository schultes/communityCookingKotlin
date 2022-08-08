package de.thm.mow2.communitycooking.view.adapter

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementEventChatMessageBinding
import de.thm.mow2.communitycooking.model.Message
import de.thm.mow2.communitycooking.view.activity.ProfileShowActivity
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import java.text.SimpleDateFormat
import java.util.*

class EventChatAdapter(val context: Context) :
    RecyclerView.Adapter<EventChatAdapter.EventChatViewHolder>() {
    val messages: SortedList<Message> = SortedList(Message::class.java,
        object : SortedListAdapterCallback<Message>(this) {
            override fun compare(o1: Message, o2: Message): Int =
                o2.timestamp.compareTo(o1.timestamp)

            override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(item1: Message, item2: Message): Boolean = item1 == item2
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventChatViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EventChatViewHolder(layoutInflater.inflate(R.layout.list_element_event_chat_message, parent, false))
    }

    override fun onBindViewHolder(holder: EventChatViewHolder, position: Int) {
        val user = messages[position].user
        holder.userId = user.documentId
        holder.binding.apply {
            textViewUser.text = user.username
            textViewMessage.text = messages[position].text
            textViewDate.text =
                SimpleDateFormat("dd. MMMM yyyy HH:mm").format(Date(messages[position].timestamp.toLong() * 1000))
            val constraintSet = ConstraintSet()
            constraintSet.clone(root)
            if (user.documentId == TPFirebaseAuthentication.getUser()?.uid) {
                constraintSet.setHorizontalBias(cardView.id, 1F)
                textViewUser.visibility = View.GONE
                textViewMessage.setPadding(
                    0,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        4f,
                        root.resources.displayMetrics
                    )
                        .toInt(), 0, 0
                )
                root.updatePadding(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        32F,
                        context.resources.displayMetrics
                    ).toInt(), 0, 0, 0
                )
                constraintLayoutMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimaryTransparent
                    )
                )
            } else {
                constraintSet.setHorizontalBias(cardView.id, 0F)
                textViewUser.visibility = View.VISIBLE
                textViewMessage.setPadding(0, 0, 0, 0)
                root.updatePadding(
                    0,
                    0,
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        32F,
                        context.resources.displayMetrics
                    ).toInt(),
                    0
                )
                constraintLayoutMessage.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.transparent
                    )
                )
            }

            constraintSet.applyTo(root)
        }
    }

    override fun getItemCount(): Int {
        return messages.size()
    }

    class EventChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementEventChatMessageBinding.bind(itemView)
        var userId: String = ""

        init {
            binding.textViewUser.setOnClickListener(::onUsernameClicked)
        }

        private fun onUsernameClicked(view: View) {
            val intent = Intent(view.context, ProfileShowActivity::class.java)
            intent.putExtra(ProfileShowViewController.EXTRA_PARAMETER_UID, userId)
            view.context.startActivity(intent)
        }
    }
}
