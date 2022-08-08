package de.thm.mow2.communitycooking.model

enum class IngredientUnit(val rawValue: String) {
    ML("ml"),
    G("g"),
    X("x"),
    EL("el"),
    PINCH("prise"),
    TL("tl");

    companion object {
        operator fun invoke(rawValue: String) = values().firstOrNull { it.rawValue == rawValue }
    }
}