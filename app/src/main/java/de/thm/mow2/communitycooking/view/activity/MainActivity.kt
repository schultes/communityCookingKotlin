package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.MainViewController
import de.thm.mow2.communitycooking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var viewController: MainViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = MainViewController(this, binding)
    }

    override fun onResume() {
        super.onResume()
        viewController.loadData()
        viewController.updateLocation()
    }
}