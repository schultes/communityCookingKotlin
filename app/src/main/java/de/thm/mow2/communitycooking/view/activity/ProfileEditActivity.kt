package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.ProfileEditViewController
import de.thm.mow2.communitycooking.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {
    private lateinit var viewController: ProfileEditViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = ProfileEditViewController(this, binding)
    }
}
