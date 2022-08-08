package de.thm.mow2.communitycooking.model

data class User(
    val documentId: String,
    val username: String,
    val forename: String,
    val lastname: String,
    val description: String,
    val image: String,
    val preference: RecipeType,
    val radiusInKilometer: Int
) {
    val fullname: String
        get() = "$forename $lastname"

    companion object {
        const val COLLECTION_NAME = "user"

        fun toMap(user: User): Map<String, Any> {
            return mapOf(
                "username" to user.username,
                "forename" to user.forename,
                "lastname" to user.lastname,
                "description" to user.description,
                "image" to user.image,
                "preference" to user.preference.rawValue,
                "radiusInKilometer" to user.radiusInKilometer
            )
        }

        fun toObject(documentId: String, map: Map<String, Any>): User? {
            return if (
                map["username"] == null
                || map["forename"] == null
                || map["lastname"] == null
                || map["description"] == null
                || map["image"] == null
                || map["preference"] == null
                || map["radiusInKilometer"] == null
            )
                null
            else
                User(
                    documentId,
                    map["username"]!! as String,
                    map["forename"]!! as String,
                    map["lastname"]!! as String,
                    map["description"]!! as String,
                    map["image"]!! as String,
                    RecipeType(map["preference"]!! as String)!!,
                    (map["radiusInKilometer"]!! as Number).toInt()
                )
        }
    }
}
