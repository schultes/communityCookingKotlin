package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.EventEditViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventEditBinding

class EventEditActivity : AppCompatActivity() {
    private lateinit var viewController: EventEditViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEventEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = EventEditViewController(this, binding)
    }
}