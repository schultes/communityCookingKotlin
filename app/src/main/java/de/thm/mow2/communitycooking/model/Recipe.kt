package de.thm.mow2.communitycooking.model

import de.thm.mow2.communitycooking.model.service.DatabaseService

data class Recipe(
    val timestamp: Double,
    val owner: User,
    val title: String,
    val description: String,
    val image: String,
    val type: RecipeType,
    val steps: List<String>,
    val ingredients: List<Ingredient>,
    val portionSize: Int,
    val duration: Int,
    val documentId: String?
) {
    companion object {
        const val COLLECTION_NAME = "recipe"

        fun toMap(recipe: Recipe): Map<String, Any> {
            return mapOf(
                "timestamp" to recipe.timestamp,
                "owner" to recipe.owner.username,
                "title" to recipe.title,
                "description" to recipe.description,
                "image" to recipe.image,
                "type" to recipe.type.rawValue,
                "steps" to recipe.steps,
                "ingredients" to recipe.ingredients.map { ingredient -> Ingredient.toMap(ingredient) },
                "portionSize" to recipe.portionSize,
                "duration" to recipe.duration
            )
        }

        fun toObject(
            documentId: String,
            map: Map<String, Any>,
            callback: (recipe: Recipe?, error: String?) -> Unit
        ) {
            if (
                map["timestamp"] == null
                || map["owner"] == null
                || map["title"] == null
                || map["description"] == null
                || map["image"] == null
                || map["type"] == null
                || map["steps"] == null
                || map["ingredients"] == null
                || map["portionSize"] == null
                || map["duration"] == null
            ) {
                callback(null, "Fatal Error")
            } else {


                val ingredientList = mutableListOf<Ingredient>()
                val ingredientArray = map["ingredients"] as ArrayList<Map<String, Any>>
                ingredientArray.forEach { ingredientMap ->
                    Ingredient.toObject(ingredientMap)
                        ?.let { ingredientObject -> ingredientList.add(ingredientObject) }
                }

                DatabaseService.getUserByUsername(map["owner"]!! as String) { result, error ->

                    error?.let { message ->
                        callback(null, message)
                    }

                    result?.let { user ->
                        val recipe = Recipe(
                            (map["timestamp"]!! as Number).toDouble(),
                            user,
                            map["title"]!! as String,
                            map["description"]!! as String,
                            map["image"]!! as String,
                            RecipeType(map["type"]!! as String)!!,
                            map["steps"] as List<String>,
                            ingredientList,
                            (map["portionSize"]!! as Number).toInt(),
                            (map["duration"]!! as Number).toInt(),
                            documentId
                        )
                        callback(recipe, null)
                    }
                }
            }
        }
    }
}