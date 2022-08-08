package de.thm.mow2.communitycooking.model

import de.thm.mow2.communitycooking.model.service.DatabaseService

data class Message(
    val context: String,
    val timestamp: Double,
    val user: User,
    val text: String,
    val documentId: String?
) {
    companion object {
        const val COLLECTION_NAME = "message"

        fun toMap(message: Message): Map<String, Any> {
            return mapOf(
                "context" to message.context,
                "timestamp" to message.timestamp,
                "user" to message.user.username,
                "text" to message.text
            )
        }

        fun toObject(
            documentId: String,
            map: Map<String, Any>,
            callback: (message: Message?, error: String?) -> Unit
        ) {

            if (
                map["context"] == null
                || map["timestamp"] == null
                || map["user"] == null
                || map["text"] == null
            ) {
                callback(null, "Fatal Error")
            } else {
                DatabaseService.getUserByUsername(map["user"]!! as String) { result, error ->
                    error?.let { message ->
                        callback(null, message)
                    }

                    result?.let { user ->
                        val message = Message(
                            map["context"]!! as String,
                            (map["timestamp"]!! as Number).toDouble(),
                            user,
                            map["text"]!! as String,
                            documentId
                        )

                        callback(message, null)
                    }
                }
            }
        }
    }
}
