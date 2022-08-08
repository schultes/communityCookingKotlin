package de.thm.mow2.communitycooking.controller.view

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.model.EventChatModelController
import de.thm.mow2.communitycooking.controller.model.IEventChatViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventChatBinding
import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.Message
import de.thm.mow2.communitycooking.view.activity.EventChatActivity
import de.thm.mow2.communitycooking.view.adapter.EventChatAdapter
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.time.Instant

class EventChatViewController(
    private val activity: EventChatActivity,
    private val binding: ActivityEventChatBinding
) : IEventChatViewController {

    private val modelController: EventChatModelController = EventChatModelController(this)
    private val eventID: String? = activity.intent.getStringExtra(EXTRA_PARAMETER_EVENT_ID)

    init {
        binding.apply {
            buttonBack.setOnClickListener(::onBackClicked)
            buttonSend.setOnClickListener(::onSendClicked)

            recyclerViewMessages.adapter = EventChatAdapter(activity)
            recyclerViewMessages.layoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
        }

        eventID?.also { id ->
            modelController.downloadData(id)
        } ?: run {
            exitActivity()
        }
    }

    override fun setMessages(messages: List<Message>) {
        (binding.recyclerViewMessages.adapter as? EventChatAdapter)?.messages?.replaceAll(messages)
        binding.recyclerViewMessages.smoothScrollToPosition(0)
    }

    override fun setEvent(event: Event) {
        binding.textViewEventTitle.text = event.title

        FirebaseStorageService.downloadImageIntoImageView(
            event.recipe.image,
            binding.imageViewEventImage,
            R.mipmap.default_recipe
        )
    }

    private fun onSendClicked(view: View) {
        binding.apply {
            if (editTextMessage.text.isNotEmpty())
                modelController.sendMessage(
                    editTextMessage.text.toString(), eventID!!,
                    Instant.now().epochSecond.toDouble()
                )
            editTextMessage.text.clear()
        }

    }

    private fun onBackClicked(view: View) {
        exitActivity()
    }

    private fun exitActivity() {
        activity.finish()
    }

    companion object {
        const val EXTRA_PARAMETER_EVENT_ID = "event_id"
    }
}