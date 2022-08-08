package de.thm.mow2.communitycooking.model

import de.thm.mow2.communitycooking.model.service.DatabaseService
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore

data class Event(
    val timestamp: Double,
    val owner: User,
    val location: Location,
    val isPublic: Boolean,
    val title: String,
    val description: String,
    val recipe: Recipe,
    val requested: List<User>,
    val participating: List<User>,
    val documentId: String?

) {
    companion object {
        const val COLLECTION_NAME = "event"

        fun toMap(event: Event): Map<String, Any> {
            return mapOf(
                "timestamp" to event.timestamp,
                "owner" to event.owner.username,
                "location" to Location.toMap(event.location),
                "isPublic" to event.isPublic,
                "title" to event.title,
                "description" to event.description,
                "recipe" to event.recipe.documentId!!,
                "requested" to event.requested.map { user -> user.username },
                "participating" to event.participating.map { user -> user.username }
            )
        }

        fun toObject(
            documentId: String,
            map: Map<String, Any>,
            callback: (event: Event?, error: String?) -> Unit
        ) {

            if (
                map["timestamp"] == null
                || map["owner"] == null
                || map["location"] == null
                || map["isPublic"] == null
                || map["title"] == null
                || map["description"] == null
                || map["recipe"] == null
                || map["requested"] == null
                || map["participating"] == null
            ) {
                callback(null, "Fatal Error")
            } else {

                val locationObject = Location.toObject(map["location"] as Map<String, Any>)
                locationObject?.also { location ->

                    val ownerUsername = map["owner"]!! as String
                    val requestedUsernames = map["requested"]!! as List<String>
                    val participatingUsernames = map["participating"]!! as List<String>

                    val necessaryUsernames = mutableListOf<String>()
                    necessaryUsernames += ownerUsername
                    necessaryUsernames += requestedUsernames
                    necessaryUsernames += participatingUsernames

                    DatabaseService.getUsersByUsernames(necessaryUsernames) { result, error ->
                        error?.let { message ->
                            callback(null, message)
                        }

                        result?.let { tempUserList ->
                            val ownerUserObject =
                                tempUserList.firstOrNull { user -> ownerUsername == user.username }
                            val requestedUserObjects =
                                tempUserList.filter { user -> requestedUsernames.contains(user.username) }
                            val participatingUserObjects =
                                tempUserList.filter { user -> participatingUsernames.contains(user.username) }

                            ownerUserObject?.also { owner ->
                                val recipeDocumentId = map["recipe"]!! as String
                                TPFirebaseFirestore.getDocument(
                                    Recipe.COLLECTION_NAME,
                                    recipeDocumentId
                                ) { result, error ->
                                    error?.let { message ->
                                        callback(null, message)
                                    }

                                    result?.let { resultItem ->
                                        Recipe.toObject(
                                            resultItem.documentId,
                                            resultItem.data
                                        ) { result, error ->
                                            error?.let { message ->
                                                callback(null, message)
                                            }

                                            result?.let { recipe ->

                                                val event = Event(
                                                    (map["timestamp"]!! as Number).toDouble(),
                                                    owner,
                                                    location,
                                                    map["isPublic"]!! as Boolean,
                                                    map["title"]!! as String,
                                                    map["description"]!! as String,
                                                    recipe,
                                                    requestedUserObjects,
                                                    participatingUserObjects,
                                                    documentId
                                                )

                                                callback(event, null)
                                            }
                                        }
                                    }
                                }
                            } ?: run {
                                callback(null, "Owner Error")
                            }
                        }
                    }
                } ?: run {
                    callback(null, "Location Error")
                }
            }
        }
    }
}