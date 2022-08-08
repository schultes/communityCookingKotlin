package de.thm.mow2.communitycooking.model.service

import de.thm.mow2.communitycooking.model.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LocationService {
    companion object {

        fun calculateDistance(userLocation: Location, eventLocation: Location): Double {
            //calculates distance between 2 sets of coordinates based on the haversine formula
            val earthRadius = 6371
            val differenceLatitude = degreeToRadius(eventLocation.latitude - userLocation.latitude)
            val differenceLongitude =
                degreeToRadius(eventLocation.longitude - userLocation.longitude)

            val a =
                sin(differenceLatitude / 2) * sin(differenceLatitude / 2) +
                        cos(degreeToRadius(userLocation.latitude)) * cos(
                    degreeToRadius(
                        eventLocation.latitude
                    )
                ) *
                        sin(differenceLongitude / 2) * sin(differenceLongitude / 2)

            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return earthRadius * c
        }

        private fun degreeToRadius(deg: Double): Double {
            return deg * (Math.PI / 180)
        }
    }
}