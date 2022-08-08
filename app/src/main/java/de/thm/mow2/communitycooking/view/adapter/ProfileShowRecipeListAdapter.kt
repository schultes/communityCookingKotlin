package de.thm.mow2.communitycooking.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.thm.mow2.communitycooking.R
import de.thm.mow2.communitycooking.controller.view.RecipeShowViewController
import de.thm.mow2.communitycooking.databinding.ListElementEventShowParticipantBinding
import de.thm.mow2.communitycooking.model.Recipe
import de.thm.mow2.communitycooking.view.activity.RecipeShowActivity
import de.thm.mow2.communitycooking.view.service.FirebaseStorageService

class ProfileShowRecipeListAdapter :
    RecyclerView.Adapter<ProfileShowRecipeListAdapter.ProfileShowRecipeListViewHolder>() {
    var recipes: List<Recipe> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProfileShowRecipeListViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ProfileShowRecipeListViewHolder(
            layoutInflater.inflate(
                R.layout.list_element_event_show_participant,
                parent,
                false
            ), this
        )
    }

    override fun onBindViewHolder(holder: ProfileShowRecipeListViewHolder, position: Int) {
        holder.binding.apply {
            textViewUsername.text = recipes[position].title
            FirebaseStorageService.downloadImageIntoImageView(
                recipes[position].image,
                imageViewProfileImage,
                R.mipmap.default_recipe
            )
        }
    }


    override fun getItemCount(): Int {
        return recipes.size
    }

    class ProfileShowRecipeListViewHolder(
        itemView: View,
        val adapter: ProfileShowRecipeListAdapter
    ) : RecyclerView.ViewHolder(itemView) {

        val binding = ListElementEventShowParticipantBinding.bind(itemView)

        init {
            binding.cardView.setOnClickListener(::onItemClicked)
        }

        private fun onItemClicked(view: View) {
            val intent = Intent(view.context, RecipeShowActivity::class.java)
            intent.putExtra(
                RecipeShowViewController.EXTRA_PARAMETER_RID,
                adapter.recipes[layoutPosition].documentId
            )
            view.context.startActivity(intent)
        }
    }
}