package de.thm.mow2.communitycooking.model

enum class RecipeType(val rawValue: String) {
    NONE("NONE"),
    VEGETARIAN("VEGETARIAN"),
    VEGAN("VEGAN");

    companion object {
        operator fun invoke(rawValue: String) = values().firstOrNull { it.rawValue == rawValue }
    }
}