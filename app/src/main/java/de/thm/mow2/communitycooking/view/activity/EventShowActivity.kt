package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.EventShowViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventShowBinding

class EventShowActivity : AppCompatActivity() {
    private lateinit var viewController: EventShowViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEventShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = EventShowViewController(this, binding)
    }

    override fun onResume() {
        super.onResume()
        viewController.loadEvent()
    }
}