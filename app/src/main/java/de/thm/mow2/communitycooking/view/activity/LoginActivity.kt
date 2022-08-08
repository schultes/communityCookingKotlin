package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.LoginViewController
import de.thm.mow2.communitycooking.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {

    private lateinit var viewController: LoginViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = LoginViewController(this, binding)
    }
}