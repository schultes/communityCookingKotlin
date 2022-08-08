import de.thm.mow2.communitycooking.model.Ingredient
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.model.service.DatabaseService

interface IRecipeEditViewController {
    fun setData(recipe: Recipe)
    fun exitActivity()
    fun showErrorMessage(message: String)
}

class RecipeEditModelController(private val viewController: IRecipeEditViewController) {

    fun downloadData(recipeId: String) {
        DatabaseService.getRecipeById(recipeId) { result, error ->
            result?.let { recipe ->
                viewController.setData(recipe)
            }

            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }

    fun onSaveClicked(
        timestamp: Double,
        title: String,
        description: String,
        image: String,
        type: RecipeType,
        steps: MutableList<String>,
        ingredients: MutableList<Ingredient>,
        portionSize: Int,
        duration: Int,
        documentId: String?
    ) {
        DatabaseService.getMyself { result, error ->
            result?.let { user ->
                val recipe = Recipe(
                    timestamp,
                    user,
                    title,
                    description,
                    image,
                    type,
                    steps,
                    ingredients,
                    portionSize,
                    duration,
                    documentId
                )
                DatabaseService.setRecipe(recipe) { error ->
                    error?.also { message ->
                        viewController.showErrorMessage(message)
                    } ?: run {
                        viewController.exitActivity()
                    }
                }
            }

            error?.let { message ->
                viewController.showErrorMessage(message)
            }
        }
    }
}