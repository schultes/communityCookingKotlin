package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.RecipeEditViewController
import de.thm.mow2.communitycooking.databinding.ActivityRecipeEditBinding

class RecipeEditActivity : AppCompatActivity() {
    private lateinit var viewController: RecipeEditViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRecipeEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = RecipeEditViewController(this, binding)
    }
}