package de.thm.mow2.communitycooking.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.AlertShowIngredientsBinding
import de.thm.mow2.communitycooking.databinding.ListElementRecipeEditEditIngredientBinding
import de.thm.mow2.communitycooking.model.Ingredient
import de.thm.mow2.communitycooking.model.IngredientUnit
import java.text.DecimalFormat

class RecipeEditIngredientListAdapter(private val context: Context) :
    RecyclerView.Adapter<RecipeEditIngredientListAdapter.RecipeEditIngredientViewHolder>() {
    var ingredients = mutableListOf<Ingredient>()

    fun updateContents(ingredients: MutableList<Ingredient>) {
        this.ingredients = ingredients
        notifyDataSetChanged()
    }

    fun addItem(name: String, amount: Double, unit: IngredientUnit) {
        ingredients.add(
            Ingredient(
                name,
                amount,
                unit
            )
        )
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipeEditIngredientViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecipeEditIngredientViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_recipe_edit_edit_ingredient,
                parent,
                false
            ),
            this
        )
    }

    override fun onBindViewHolder(holder: RecipeEditIngredientViewHolder, position: Int) {
        val df = DecimalFormat()
        df.maximumFractionDigits = 2
        holder.binding.apply {
            textViewAmount.text = df.format(ingredients[position].amount)
            textViewUnit.text =
                context.resources.getStringArray(R.array.ingredientUnitDropDown)[IngredientUnit.values()
                    .indexOf(ingredients[position].unit)]
            textViewIngredientName.text = ingredients[position].name
        }
    }

    override fun getItemCount(): Int {
        return ingredients.size
    }

    inner class RecipeEditIngredientViewHolder(
        itemView: View,
        private val adapter: RecipeEditIngredientListAdapter
    ) : RecyclerView.ViewHolder(itemView) {

        val binding = ListElementRecipeEditEditIngredientBinding.bind(itemView)

        init {
            binding.buttonEdit.setOnClickListener(::onEditClicked)
        }

        private fun onEditClicked(view: View) {
            MaterialAlertDialogBuilder(view.context).apply {
                setTitle("Zutat hinzufügen")
                AlertShowIngredientsBinding.inflate(LayoutInflater.from(context)).apply {
                    setView(root)

                    editTextAmount.setText(adapter.ingredients[layoutPosition].amount.toString())
                    editTextIngredientName.setText(adapter.ingredients[layoutPosition].name)

                    spinnerUnit.adapter = ArrayAdapter.createFromResource(
                        context,
                        R.array.ingredientUnitDropDown,
                        android.R.layout.simple_spinner_dropdown_item
                    )
                    spinnerUnit.setSelection(
                        IngredientUnit.values().indexOf(adapter.ingredients[layoutPosition].unit)
                    )
                    editTextAmount.doOnTextChanged { text, _, _, _ ->
                        inputLayoutAmount.error =
                            if (text.isNullOrBlank()) "Keine Menge eingegeben!" else null
                    }
                    editTextIngredientName.doOnTextChanged { text, _, _, _ ->
                        inputLayoutIngredientName.error =
                            if (text.isNullOrBlank()) "Kein Name eingegeben!" else null
                    }

                    create().apply {
                        buttonConfirm.setOnClickListener {
                            if (editTextAmount.text!!.isNotEmpty() && editTextIngredientName.text!!.isNotEmpty()) {
                                adapter.ingredients[layoutPosition] = Ingredient(
                                    editTextIngredientName.text.toString().trim(),
                                    editTextAmount.text.toString().toDouble(),
                                    IngredientUnit.values()[spinnerUnit.selectedItemPosition]
                                )
                                adapter.notifyItemChanged(layoutPosition)
                                dismiss()
                            }
                            if (editTextIngredientName.text.isNullOrEmpty()) {
                                inputLayoutIngredientName.error = "Kein Name eingegeben!"
                            }
                            if (editTextAmount.text.isNullOrEmpty()) {
                                inputLayoutAmount.error = "Keine Menge eingegeben!"
                            }
                        }
                        buttonCancel.setOnClickListener { dismiss() }
                        buttonDelete.setOnClickListener {
                            adapter.ingredients.removeAt(layoutPosition)
                            adapter.notifyItemRemoved(layoutPosition)
                            dismiss()
                        }
                    }.show()
                }
            }
        }
    }
}
