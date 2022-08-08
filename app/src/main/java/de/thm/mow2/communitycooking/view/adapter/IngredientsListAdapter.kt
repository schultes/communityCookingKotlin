package de.thm.mow2.communitycooking.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.ListElementIngredientBinding
import de.thm.mow2.communitycooking.model.Ingredient
import de.thm.mow2.communitycooking.model.IngredientUnit
import java.text.DecimalFormat

class IngredientsListAdapter(private val context: Context) :
    RecyclerView.Adapter<IngredientsListAdapter.IngredientsListViewHolder>() {
    private var ingredients: List<Ingredient> = emptyList()
    private var portionSize: Int = 1
    private var participants: Int = 1

    fun updateContents(ingredients: List<Ingredient>, portionSize: Int, participants: Int) {
        this.ingredients = ingredients
        this.portionSize = portionSize
        this.participants = participants
        notifyDataSetChanged()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientsListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return IngredientsListViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_ingredient,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: IngredientsListViewHolder, position: Int) {
        holder.binding.apply {
            root.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    if (position % 2 == 1) R.color.selectable_background_tint else R.color.colorPrimaryTransparent
                )
            )
            textViewIngredient.text = ingredients[position].name
            val df = DecimalFormat()
            df.maximumFractionDigits = 2
            textViewAmount.text =
                "${df.format(ingredients[position].amount / portionSize.toDouble() * participants.toDouble())} ${context.resources.getStringArray(R.array.ingredientUnitDropDown)[IngredientUnit.values()
                    .indexOf(ingredients[position].unit)]}"

        }
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    class IngredientsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementIngredientBinding.bind(itemView)
    }
}