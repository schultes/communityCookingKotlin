package de.thm.mow2.communitycooking.model

data class Ingredient(
    val name: String,
    val amount: Double,
    val unit: IngredientUnit
) {
    companion object {
        fun toMap(ingredient: Ingredient): Map<String, Any> {
            return mapOf(
                "name" to ingredient.name,
                "amount" to ingredient.amount,
                "unit" to ingredient.unit.rawValue
            )
        }

        fun toObject(map: Map<String, Any?>): Ingredient? {
            return if (
                map["name"] == null
                || map["amount"] == null
                || map["unit"] == null
            )
                null
            else
                Ingredient(
                    map["name"]!! as String,
                    (map["amount"]!! as Number).toDouble(),
                    IngredientUnit(map["unit"]!! as String)!!
                )
        }
    }
}