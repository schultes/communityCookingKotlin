package de.thm.mow2.communitycooking.view.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.thm.mow2.communitycooking.controller.view.RegisterViewController
import de.thm.mow2.communitycooking.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    private lateinit var viewController: RegisterViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewController = RegisterViewController(this, binding)
    }
}