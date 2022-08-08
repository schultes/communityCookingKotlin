package de.thm.mow2.communitycooking.model.service

import de.thm.mow2.communitycooking.model.User
import de.thm.tp.library.firebase.firestore.TPFirebaseFirestore


class UserCacheService {

    companion object {
        private var instance = UserCacheService()

        fun getInstance(): UserCacheService {
            return instance
        }
    }

    private var cache = mutableListOf<User>()

    init {
        this.addSnapshotListener()
    }

    private fun addSnapshotListener() {
        TPFirebaseFirestore.addCollectionSnapshotListener(User.COLLECTION_NAME) { result, error ->
            result?.let { documents ->
                this.cache =
                    documents.filter { document -> this.cache.any { cachedUser -> cachedUser.documentId === document.documentId } }
                        .mapNotNull { document -> User.toObject(document.documentId, document.data) }.toMutableList()
            }
        }
    }

    fun getUserByDocumentId(documentId: String): User? {
        return cache.firstOrNull { user -> user.documentId == documentId }
    }

    fun getUserByUsername(username: String): User? {
        return cache.firstOrNull { user -> user.username == username }
    }

    fun getUsersByUsernames(usernames: List<String>): List<User> {
        return cache.filter { element -> usernames.contains(element.username) }
    }

    fun setUser(user: User) {
        cache.removeAll { temp -> temp.documentId == user.documentId }
        cache.add(user)
    }

    fun setUsers(users: List<User>) {
        users.forEach { user ->
            cache.removeAll { temp -> temp.documentId == user.documentId }
            cache.add(user)
        }
    }
}