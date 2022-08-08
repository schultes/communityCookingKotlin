package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.Message
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.DatabaseService

interface IEventChatViewController {
    fun setEvent(event: Event)
    fun setMessages(messages: List<Message>)
}

class EventChatModelController(
    private val viewController: IEventChatViewController
) {
    private var user: User? = null

    init {
        DatabaseService.getMyself { user, error ->
            user.let { this.user = user }
        }
    }

    fun sendMessage(text: String, eventID: String, timestamp: Double) {
        user?.let { user ->
            DatabaseService.sendMessage(
                Message(
                    eventID,
                    timestamp,
                    user,
                    text,
                    null
                )
            ) { }
        }
    }

    fun downloadData(context: String) {
        DatabaseService.getMessagesByContext(context) { result, error ->
            result?.let { messages ->
                viewController.setMessages(messages)
            }
        }
        DatabaseService.getEventById(context) { result, error ->
            result?.let { event ->
                viewController.setEvent(event)
            }
        }
    }
}