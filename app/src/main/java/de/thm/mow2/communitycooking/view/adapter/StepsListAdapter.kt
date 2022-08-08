package de.thm.mow2.communitycooking.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.databinding.ListElementRecipeShowStepsBinding

class StepsListAdapter :
    RecyclerView.Adapter<StepsListAdapter.StepsListViewHolder>() {
    private var steps: List<String> = emptyList()

    fun updateContents(steps: List<String>) {
        this.steps = steps
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepsListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return StepsListViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_recipe_show_steps,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StepsListViewHolder, position: Int) {
        holder.binding.apply {
            textViewStepIndex.text = "Schritt ${position + 1}"
            textViewStepDescription.text = steps[position]
        }
    }

    override fun getItemCount(): Int {
        return steps.size
    }

    class StepsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = ListElementRecipeShowStepsBinding.bind(itemView)
    }

}