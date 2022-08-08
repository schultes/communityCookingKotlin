package de.thm.mow2.communitycooking.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SortedList
import androidx.recyclerview.widget.SortedListAdapterCallback
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.ListElementRecipeListBinding
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.model.RecipeType
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService
import java.time.Instant
import java.util.concurrent.TimeUnit

class RecipeListAdapter(
    val itemClicked: (recipe: Recipe) -> Unit,
    val itemLongPress: ((recipe: Recipe) -> Unit)?
) :
    RecyclerView.Adapter<RecipeListAdapter.RecipeListViewHolder>() {
    val recipes: SortedList<Recipe> = SortedList(
        Recipe::class.java,
        object : SortedListAdapterCallback<Recipe>(this) {
            override fun compare(o1: Recipe, o2: Recipe): Int =
                o1.title.compareTo(o2.title)

            override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean =
                oldItem == newItem

            override fun areItemsTheSame(item1: Recipe, item2: Recipe): Boolean = item1 == item2
        })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeListViewHolder {
        return RecipeListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_element_recipe_list,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: RecipeListViewHolder, position: Int) {
        val recipe = recipes[position]
        holder.binding.apply {
            textViewTitle.text = recipe.title
            textViewUsername.text = recipe.owner.fullname
            textViewPrepTime.text =
                "${recipe.duration}min"
            textViewPortions.text =
                "${recipe.portionSize} ${holder.itemView.context.getString(R.string.people)}"
            cardViewNewIndicator.visibility =
                if (TimeUnit.SECONDS.toDays(Instant.now().epochSecond - recipe.timestamp.toLong()) < 7) View.VISIBLE else View.GONE

            textViewRecipeType.text =
                root.context.resources.getStringArray(R.array.recipeTypes)[RecipeType.values()
                    .indexOf(recipe.type)]

            FirebaseStorageService.downloadImageIntoImageView(
                recipe.image,
                imageViewRecipe,
                R.mipmap.default_recipe
            )
        }
    }

    override fun getItemCount(): Int {
        return recipes.size()
    }

    class RecipeListViewHolder(itemView: View, private val adapter: RecipeListAdapter) :
        RecyclerView.ViewHolder(itemView) {

        val binding = ListElementRecipeListBinding.bind(itemView)

        init {
            binding.cardView.setOnClickListener(::onItemClicked)
            adapter.itemLongPress?.let {
                binding.cardViewImageContainer.setOnClickListener(::onImageClicked)
            }
        }

        private fun onItemClicked(view: View) {
            adapter.itemClicked(adapter.recipes[layoutPosition])
        }

        private fun onImageClicked(view: View) {
            adapter.itemLongPress?.let { itemLongPress ->
                itemLongPress(adapter.recipes[layoutPosition])
            }
        }
    }
}
