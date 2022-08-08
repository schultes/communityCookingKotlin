package de.thm.mow2.communitycooking.model.service

import de.thm.mow2.communitycooking.model.*
import de.thm.tp.library.firebase.authentication.TPFirebaseAuthentication
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestoreQueryBuilder

class DatabaseService {
    companion object {

        fun isUsernameUnique(username: String, callback: (isUnique: Boolean) -> Unit) {
            TPFirebaseFirestore.getDocuments(
                TPFirebaseFirestoreQueryBuilder(User.COLLECTION_NAME).whereEqualTo(
                    "username",
                    username
                )
            ) { result, error ->
                error?.let { message ->
                    callback(true)
                }

                result?.let { resultSet ->
                    callback(resultSet.isEmpty())
                }
            }
        }

        fun getUserById(documentId: String, callback: (user: User?, error: String?) -> Unit) {

            val userCacheService = UserCacheService.getInstance()
            val cachedUser = userCacheService.getUserByDocumentId(documentId)
            cachedUser?.also { user ->
                callback(user, null)
            } ?: run {
                TPFirebaseFirestore.getDocument(User.COLLECTION_NAME, documentId) { result, error ->

                    error?.let { message ->
                        callback(null, message)
                    }

                    result?.let { resultItem ->
                        val userObject = User.toObject(resultItem.documentId, resultItem.data)
                        userObject?.also { user ->
                            userCacheService.setUser(user)
                            callback(userObject, null)
                        } ?: run {
                            callback(null, "Bitte tragen Sie alle nötigen Informationen ein!")
                        }
                    }
                }
            }
        }

        fun getUserByUsername(username: String, callback: (user: User?, error: String?) -> Unit) {

            val userCacheService = UserCacheService.getInstance()
            val cachedUser = userCacheService.getUserByUsername(username)

            cachedUser?.also { user ->
                callback(user, null)
            } ?: run {
                TPFirebaseFirestore.getDocuments(
                    TPFirebaseFirestoreQueryBuilder(User.COLLECTION_NAME).whereEqualTo(
                        "username",
                        username
                    )
                ) { result, error ->
                    error?.let { message ->
                        callback(null, message)
                    }

                    result?.also { resultItem ->
                        if (resultItem.isNotEmpty()) {
                            val userObject = User.toObject(resultItem[0].documentId, resultItem[0].data)
                            userObject?.let { user ->
                                userCacheService.setUser(user)
                                callback(userObject, null)
                            }
                        } else {
                            callback(null, null)
                        }
                    } ?: run {
                        callback(null, "Bitte tragen Sie alle nötigen Informationen ein!")
                    }
                }
            }
        }

        fun getUsersByUsernames(usernames: List<String>, callback: (users: List<User>?, error: String?) -> Unit) {

            val userCacheService = UserCacheService.getInstance()
            val cachedUsers = userCacheService.getUsersByUsernames(usernames)
            val unCachedUsernames =
                usernames.filter { username -> cachedUsers.firstOrNull { user -> user.username == username } == null }

            if (unCachedUsernames.isEmpty()) {
                callback(cachedUsers, null)
            } else {
                TPFirebaseFirestore.getDocuments(
                    TPFirebaseFirestoreQueryBuilder(User.COLLECTION_NAME).whereIn(
                        "username",
                        unCachedUsernames
                    )
                ) { result, error ->

                    error?.let { message ->
                        callback(null, message)
                    }

                    result?.let { resultSet ->
                        userCacheService.setUsers(resultSet.mapNotNull { element ->
                            User.toObject(
                                element.documentId,
                                element.data
                            )
                        })
                        callback(userCacheService.getUsersByUsernames(usernames), null)
                    }
                }
            }
        }

        fun getMyself(callback: (user: User?, error: String?) -> Unit) {
            TPFirebaseAuthentication.getUser()?.let { user ->
                getUserById(user.uid, callback)
            }
        }

        fun setUser(user: User, callback: (error: String?) -> Unit) {
            TPFirebaseFirestore.setDocument(
                User.COLLECTION_NAME,
                user.documentId,
                User.toMap(user)
            ) { error ->
                callback(error)
            }
        }

        fun setEvent(event: Event, callback: (error: String?) -> Unit) {
            if (event.documentId == null)
                TPFirebaseFirestore.addDocument(
                    Event.COLLECTION_NAME,
                    Event.toMap(event)
                ) { document, error ->
                    callback(error)
                }
            else
                TPFirebaseFirestore.setDocument(
                    Event.COLLECTION_NAME,
                    event.documentId,
                    Event.toMap(event),
                    callback
                )
        }

        fun deleteEventById(documentId: String, callback: (error: String?) -> Unit) {
            TPFirebaseFirestore.deleteDocument(Event.COLLECTION_NAME, documentId, callback)
        }

        fun getEventById(
            documentId: String,
            callback: (event: Event?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.getDocument(
                Event.COLLECTION_NAME,
                documentId
            ) { result, error ->
                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultItem ->
                    Event.toObject(resultItem.documentId, resultItem.data, callback)
                }
            }
        }

        fun getEventsByRadius(
            user: User,
            userLocation: Location,
            callback: (events: List<Event>?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.addCollectionSnapshotListener(
                TPFirebaseFirestoreQueryBuilder(Event.COLLECTION_NAME)
                    .whereEqualTo("isPublic", true)
                    .orderBy("timestamp", true)
            ) { result, error ->
                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultSet ->
                    val eventInRadiusList = mutableListOf<Event>()
                    val tempEventList = mutableListOf<Event>()
                    var finishedProcesses = 0
                    if (resultSet.isNotEmpty()) {
                        resultSet.forEach { tpFirebaseFirestoreDocument ->
                            Event.toObject(
                                tpFirebaseFirestoreDocument.documentId,
                                tpFirebaseFirestoreDocument.data
                            ) { result, _ ->
                                result?.let { event ->
                                    tempEventList.add(event)
                                }
                                finishedProcesses++
                                if (finishedProcesses == resultSet.size) {
                                    tempEventList.forEach { event ->
                                        if (event.owner.documentId != user.documentId &&
                                            !event.requested.contains(user) &&
                                            !event.participating.contains(user)
                                            && checkPreference(event.recipe, user.preference)
                                        ) {
                                            if (LocationService.calculateDistance(
                                                    userLocation,
                                                    event.location
                                                ) <= user.radiusInKilometer
                                            )
                                                eventInRadiusList.add(event)
                                        }
                                    }
                                    callback(eventInRadiusList, null)
                                }
                            }
                        }
                    } else {
                        callback(emptyList(), null)
                    }
                }
            }
        }

        fun getEventsParticipating(
            user: User,
            callback: (events: List<Event>?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.addCollectionSnapshotListener(
                TPFirebaseFirestoreQueryBuilder(Event.COLLECTION_NAME)
                    .orderBy("timestamp", true)
            ) { result, error ->

                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultSet ->
                    val tempEventList = mutableListOf<Event>()
                    val filteredResultSet = resultSet.filter { document ->
                        (document.data["owner"] as String) == user.username || (document.data["participating"] as List<String>).contains(
                            user.username
                        )
                    }
                    var finishedProcesses = 0

                    if (filteredResultSet.isNotEmpty()) {
                        filteredResultSet.forEach { tpFirebaseFirestoreDocument ->
                            Event.toObject(
                                tpFirebaseFirestoreDocument.documentId,
                                tpFirebaseFirestoreDocument.data
                            ) { result, _ ->
                                result?.let { event ->
                                    tempEventList.add(event)
                                }
                                finishedProcesses++
                                if (finishedProcesses == filteredResultSet.size)
                                    callback(tempEventList, null)
                            }
                        }
                    } else {
                        callback(emptyList(), null)
                    }
                }
            }
        }

        fun getEventsRequested(
            user: User,
            callback: (events: List<Event>?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.addCollectionSnapshotListener(
                TPFirebaseFirestoreQueryBuilder(Event.COLLECTION_NAME)
                    .whereArrayContains("requested", user.username)
                    .orderBy("timestamp", true)
            ) { result, error ->

                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultSet ->
                    val tempEventList = mutableListOf<Event>()
                    var finishedProcesses = 0
                    if (resultSet.isNotEmpty()) {
                        resultSet.forEach { tpFirebaseFirestoreDocument ->
                            Event.toObject(
                                tpFirebaseFirestoreDocument.documentId,
                                tpFirebaseFirestoreDocument.data
                            ) { result, _ ->
                                result?.let { event ->
                                    tempEventList.add(event)
                                }
                                finishedProcesses++
                                if (finishedProcesses == resultSet.size)
                                    callback(tempEventList, null)
                            }
                        }
                    } else {
                        callback(emptyList(), null)
                    }
                }
            }
        }

        fun setRecipe(recipe: Recipe, callback: (error: String?) -> Unit) {
            if (recipe.documentId == null)
                TPFirebaseFirestore.addDocument(
                    Recipe.COLLECTION_NAME,
                    Recipe.toMap(recipe)
                ) { document, error ->
                    callback(error)
                }
            else
                TPFirebaseFirestore.setDocument(
                    Recipe.COLLECTION_NAME,
                    recipe.documentId,
                    Recipe.toMap(recipe),
                    callback
                )
        }

        fun getRecipeById(
            documentId: String,
            callback: (event: Recipe?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.getDocument(
                Recipe.COLLECTION_NAME,
                documentId
            ) { result, error ->
                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultItem ->
                    Recipe.toObject(resultItem.documentId, resultItem.data, callback)
                }
            }
        }

        fun getAllRecipes(callback: (recipes: List<Recipe>?, error: String?) -> Unit) {
            val query = TPFirebaseFirestoreQueryBuilder(Recipe.COLLECTION_NAME)
                .orderBy("title", false)

            TPFirebaseFirestore.addCollectionSnapshotListener(query) { result, error ->
                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultSet ->
                    val tempRecipeList = mutableListOf<Recipe>()
                    var finishedProcesses = 0
                    if (resultSet.isNotEmpty()) {
                        resultSet.forEach { tpFirebaseFirestoreDocument ->
                            Recipe.toObject(
                                tpFirebaseFirestoreDocument.documentId,
                                tpFirebaseFirestoreDocument.data
                            ) { result, _ ->
                                result?.let { recipe ->
                                    tempRecipeList.add(recipe)
                                }
                                finishedProcesses++
                                if (finishedProcesses == resultSet.size)
                                    callback(tempRecipeList, null)

                            }

                        }
                    } else {
                        callback(emptyList(), null)
                    }
                }
            }
        }

        fun getRecipesFromUser(
            user: User,
            callback: (recipes: List<Recipe>?, error: String?) -> Unit
        ) {
            getAllRecipes { result, error ->
                result?.let { recipes ->
                    callback(
                        recipes.filter { recipe -> recipe.owner.username == user.username },
                        null
                    )

                }
                error?.let { message ->
                    callback(null, message)
                }
            }
        }

        private fun checkPreference(recipe: Recipe, preference: RecipeType): Boolean {
            return if (preference == RecipeType.NONE)
                true
            else if (preference == RecipeType.VEGETARIAN && (recipe.type == RecipeType.VEGETARIAN || recipe.type == RecipeType.VEGAN))
                true
            else preference == RecipeType.VEGAN && recipe.type == RecipeType.VEGAN
        }

        fun sendMessage(message: Message, callback: (error: String?) -> Unit) {
            if (message.documentId == null)
                TPFirebaseFirestore.addDocument(
                    Message.COLLECTION_NAME,
                    Message.toMap(message)
                ) { document, error ->
                    error?.let { message ->
                        callback(message)
                    }
                }
            else
                TPFirebaseFirestore.setDocument(
                    Message.COLLECTION_NAME,
                    message.documentId,
                    Message.toMap(message),
                    callback
                )
        }

        fun getMessagesByContext(
            context: String,
            callback: (messages: List<Message>?, error: String?) -> Unit
        ) {
            TPFirebaseFirestore.addCollectionSnapshotListener(
                TPFirebaseFirestoreQueryBuilder(Message.COLLECTION_NAME)
                    .whereEqualTo("context", context)
                    .orderBy("timestamp", true)
            ) { result, error ->
                error?.let { message ->
                    callback(null, message)
                }

                result?.let { resultSet ->
                    val messageList = mutableListOf<Message>()
                    var finishedProcesses = 0
                    if (resultSet.isNotEmpty()) {
                        resultSet.forEach { tpFirebaseFirestoreDocument ->
                            Message.toObject(
                                tpFirebaseFirestoreDocument.documentId,
                                tpFirebaseFirestoreDocument.data
                            ) { result, _ ->
                                result?.let { messageObject ->
                                    messageList.add(messageObject)
                                }
                                finishedProcesses++
                                if (finishedProcesses == resultSet.size)
                                    callback(messageList, null)
                            }
                        }
                    } else {
                        callback(emptyList(), null)
                    }
                }
            }
        }
    }
}