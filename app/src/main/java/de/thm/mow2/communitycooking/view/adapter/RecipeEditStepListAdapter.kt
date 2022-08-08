package de.thm.mow2.communitycooking.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.AlertShowStepsBinding
import de.thm.mow2.communitycooking.databinding.ListElementRecipeEditEditStepBinding

class RecipeEditStepListAdapter :
    RecyclerView.Adapter<RecipeEditStepListAdapter.RecipeEditStepViewholder>() {
    var steps = mutableListOf<String>()

    fun updateContents(steps: MutableList<String>) {
        this.steps = steps
        notifyDataSetChanged()
    }

    fun addItem(step: String) {
        steps.add(step)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecipeEditStepViewholder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return RecipeEditStepViewholder(
            layoutInflater.inflate(
                R.layout.list_element_recipe_edit_edit_step,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: RecipeEditStepViewholder, position: Int) {
        holder.binding.textViewStep.text = steps[position]
    }

    override fun getItemCount(): Int {
        return steps.size
    }

    class RecipeEditStepViewholder(
        itemView: View,
        private val adapter: RecipeEditStepListAdapter
    ) : RecyclerView.ViewHolder(itemView) {

        val binding = ListElementRecipeEditEditStepBinding.bind(itemView)

        init {
            binding.buttonEdit.setOnClickListener(::onEditClicked)
        }

        private fun onEditClicked(view: View) {
            MaterialAlertDialogBuilder(view.context).apply {
                setTitle("Arbeitsschritt hinzufügen")
                AlertShowStepsBinding.inflate(LayoutInflater.from(context)).apply {
                    setView(root)

                    editTextStepDescription.setText(adapter.steps[layoutPosition])
                    editTextStepDescription.doOnTextChanged { text, _, _, _ ->
                        inputLayoutStepDescription.error =
                            if (text.isNullOrBlank()) "Keine Beschreibung eingegeben!" else null
                    }

                    create().apply {
                        buttonConfirm.setOnClickListener {
                            if (editTextStepDescription.text!!.isNotEmpty()) {
                                adapter.steps[layoutPosition] =
                                    editTextStepDescription.text.toString().trim()
                                adapter.notifyItemChanged(layoutPosition)
                                dismiss()
                            }
                            if (editTextStepDescription.text.isNullOrEmpty()) {
                                inputLayoutStepDescription.error = "Keine Beschreibung eingegeben!"
                            }
                        }
                        buttonCancel.setOnClickListener { dismiss() }
                        buttonDelete.setOnClickListener {
                            adapter.steps.removeAt(layoutPosition)
                            adapter.notifyItemRemoved(layoutPosition)
                            dismiss()
                        }
                    }.show()
                }
            }
        }
    }
}
