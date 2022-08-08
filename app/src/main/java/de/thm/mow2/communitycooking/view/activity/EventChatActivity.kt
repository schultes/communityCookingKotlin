package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.EventChatViewController
import de.thm.mow2.communitycooking.databinding.ActivityEventChatBinding

class EventChatActivity : AppCompatActivity() {
    private lateinit var viewController: EventChatViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityEventChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = EventChatViewController(this, binding)
    }
}