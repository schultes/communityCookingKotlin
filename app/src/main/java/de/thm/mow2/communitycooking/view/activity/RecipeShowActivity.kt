package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.RecipeShowViewController
import de.thm.mow2.communitycooking.databinding.ActivityRecipeShowBinding

class RecipeShowActivity : AppCompatActivity() {
    private lateinit var viewController: RecipeShowViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = RecipeShowViewController(this, binding)
    }

    override fun onResume() {
        super.onResume()
        viewController.loadRecipe()
    }
}