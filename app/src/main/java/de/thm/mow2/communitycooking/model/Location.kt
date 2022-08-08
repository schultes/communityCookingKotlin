package de.thm.mow2.communitycooking.model

data class Location(
    val timestamp: Double,
    val latitude: Double,
    val longitude: Double
) {
    companion object {
        fun toMap(locationContent: Location): Map<String, Any> {
            return mapOf(
                "timestamp" to locationContent.timestamp,
                "latitude" to locationContent.latitude,
                "longitude" to locationContent.longitude
            )
        }

        fun toObject(map: Map<String, Any>): Location? {
            return if (
                map["timestamp"] == null
                || map["latitude"] == null
                || map["longitude"] == null
            )
                null
            else
                Location(
                    (map["timestamp"]!! as Number).toDouble(),
                    (map["latitude"]!! as Number).toDouble(),
                    (map["longitude"]!! as Number).toDouble()
                )
        }
    }
}