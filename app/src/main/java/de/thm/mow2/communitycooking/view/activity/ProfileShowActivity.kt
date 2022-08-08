package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.ProfileShowViewController
import de.thm.mow2.communitycooking.databinding.ActivityProfileShowBinding

class ProfileShowActivity : AppCompatActivity() {
    private lateinit var viewController: ProfileShowViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfileShowBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = ProfileShowViewController(this, binding)
    }

    override fun onResume() {
        super.onResume()
        viewController.loadUser()
    }

    companion object {
        const val EXTRA_PARAMETER_UID = "uid"
    }
}