package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication


interface IEventShowViewController {
    fun setData(userId: String, event: Event)
    fun exitActivity()
    fun showErrorMessage(message: String)
}

class EventShowModelController(private val viewController: IEventShowViewController) {
    fun downloadData(eventId: String) {
        TPFirebaseAuthentication.getUser()?.uid?.let { userId ->
            DatabaseService.getEventById(eventId) { result, error ->
                result?.let { event ->
                    viewController.setData(userId, event)
                }

                error?.let { message ->
                    viewController.exitActivity()
                }
            }
        }
    }

    fun leaveEvent(event: Event) {
        val user = TPFirebaseAuthentication.getUser()
        user?.let { me ->
            DatabaseService.setEvent(
                Event(
                    event.timestamp,
                    event.owner,
                    event.location,
                    event.isPublic,
                    event.title,
                    event.description,
                    event.recipe,
                    event.requested,
                    event.participating.filter { user ->
                        user.documentId != me.uid
                    },
                    event.documentId
                )
            ) { error ->
                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        }
    }
}