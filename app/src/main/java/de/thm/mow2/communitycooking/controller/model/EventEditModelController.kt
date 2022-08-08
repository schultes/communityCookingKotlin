package de.thm.mow2.communitycooking.controller.model

import de.thm.mow2.communitycooking.model.Event
import de.thm.mow2.communitycooking.model.Location
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.User
import de.thm.mow2.communitycooking.model.service.DatabaseService

interface IEventEditViewController {
    fun setData(event: Event)
    fun setDataNewEvent(user: User)
    fun exitActivity()
    fun showErrorMessage(message: String)
}

class EventEditModelController(private val viewController: IEventEditViewController) {

    fun downloadData(eventID: String?) {
        if (!eventID.isNullOrEmpty()) {
            DatabaseService.getEventById(eventID) { result, error ->
                result?.let { event ->
                    viewController.setData(event)
                }
                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        } else {
            DatabaseService.getMyself { result, error ->
                result?.let { user ->
                    viewController.setDataNewEvent(user)
                }
                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        }
    }

    fun onSaveClicked(
        timestamp: Double,
        owner: User,
        location: Location,
        public: Boolean,
        title: String,
        description: String,
        recipe: Recipe,
        requested: List<User>,
        participating: List<User>,
        documentId: String?

    ) {
        //Creating new Event
        if (documentId == null) {
            DatabaseService.setEvent(
                Event(
                    timestamp,
                    owner,
                    location,
                    public,
                    title,
                    description,
                    recipe,
                    requested,
                    participating,
                    null
                )
            ) { error ->
                error?.also { message ->
                    viewController.showErrorMessage(message)
                } ?: run {
                    viewController.exitActivity()
                }
            }
        } else {
            //Overwriting Original, with making sure that users didn't leave during editing process
            DatabaseService.getEventById(documentId) { result, error ->
                result?.let { event ->
                    DatabaseService.setEvent(
                        Event(
                            timestamp,
                            owner,
                            location,
                            public,
                            title,
                            description,
                            recipe,
                            requested.filter { user ->
                                event.requested.any { temp -> temp.documentId == user.documentId }
                            },
                            participating.filter { user ->
                                event.participating.any { temp -> temp.documentId == user.documentId } || event.requested.any { temp -> temp.documentId == user.documentId }
                            },
                            event.documentId
                        )
                    ) { error ->
                        error?.also { message ->
                            viewController.showErrorMessage(message)
                        } ?: run {
                            viewController.exitActivity()
                        }
                    }
                }
                error?.let { message ->
                    viewController.showErrorMessage(message)
                }
            }
        }
    }

    fun onDeleteClicked(documentId: String) {
        DatabaseService.deleteEventById(documentId) { error ->
            error?.also { message ->
                viewController.showErrorMessage(message)
            } ?: run {
                viewController.exitActivity()
            }
        }
    }
}