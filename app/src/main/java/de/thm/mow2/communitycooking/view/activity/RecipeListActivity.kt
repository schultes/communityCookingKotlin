package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.RecipeListViewController
import de.thm.mow2.communitycooking.databinding.ActivityRecipeListBinding

class RecipeListActivity : AppCompatActivity() {
    private lateinit var viewController: RecipeListViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = RecipeListViewController(this, binding)
    }
}