package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.Location
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.DatabaseService

interface IModelViewController {
    fun showErrorMessage(message: String)
    fun setRequestedEvents(events: List<Event>)
    fun setParticipatingEvents(events: List<Event>)
    fun setNearEvents(events: List<Event>)
    fun setUser(user: User)
}

class MainModelController(private val viewController: IModelViewController) {
    fun setRequestForEvent(user: User, event: Event) {
        val requested = event.requested.filter { temp ->
            temp.documentId != user.documentId
        }.toMutableList()

        requested += user

        val temp = Event(
            event.timestamp,
            event.owner,
            event.location,
            event.isPublic,
            event.title,
            event.description,
            event.recipe,
            requested,
            event.participating.filter { temp ->
                temp.documentId != user.documentId
            },
            event.documentId!!
        )
        DatabaseService.setEvent(temp) { error ->
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }

    fun removeRequestForEvent(user: User, event: Event) {
        val temp = Event(
            event.timestamp,
            event.owner,
            event.location,
            event.isPublic,
            event.title,
            event.description,
            event.recipe,
            event.requested.filter { temp ->
                temp.documentId != user.documentId
            },
            event.participating.filter { temp ->
                temp.documentId != user.documentId
            },
            event.documentId!!
        )
        DatabaseService.setEvent(temp) { error ->
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }

    fun downloadData() {
        DatabaseService.getMyself { result, error ->
            result?.let { user ->
                viewController.setUser(user)
                DatabaseService.getEventsRequested(user) { events, error ->
                    events?.let { list ->
                        viewController.setRequestedEvents(list)
                    }
                    error?.let { message ->
                        viewController.showErrorMessage(message)
                    }
                }
                DatabaseService.getEventsParticipating(user) { events, error ->
                    events?.let { list ->
                        viewController.setParticipatingEvents(list)
                    }
                    error?.let { message ->
                        viewController.showErrorMessage(message)
                    }
                }
            }
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }

    fun downloadLocationBasedData(user: User, location: Location) {
        DatabaseService.getEventsByRadius(user, location) { events, error ->
            events?.let { list ->
                viewController.setNearEvents(list)
            }
            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }
}