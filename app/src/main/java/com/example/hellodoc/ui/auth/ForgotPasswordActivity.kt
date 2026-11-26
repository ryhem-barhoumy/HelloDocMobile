package com.example.hellodoc.ui.auth

import android.os.Bundle
import android.widget.Toast
import android.util.Patterns
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.hellodoc.R
import com.example.hellodoc.databinding.ActivityForgotPasswordBinding
import com.example.hellodoc.model.ResetPasswordRequest
import com.example.hellodoc.viewmodel.AuthViewModel
import com.example.hellodoc.viewmodel.HelloDocViewModelFactory

class ForgotPasswordActivity : AppCompatActivity() {

    private val vm: AuthViewModel by viewModels {
        HelloDocViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnBack.setOnClickListener {
            finish()
        }

        b.btnContinue.setOnClickListener {

            val email = b.etEmail.text.toString().trim()
            val phone = b.etPhone.text.toString().trim()
            val firstName = b.etFirstName.text.toString().trim()
            val lastName = b.etLastName.text.toString().trim()

            val error = when {
                email.isBlank() || phone.isBlank() ||
                        firstName.isBlank() || lastName.isBlank() ->
                    "Veuillez remplir tous les champs."

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                    "E-mail invalide."

                !Regex("[0-9]{8,15}").matches(phone) ->
                    "Numéro de téléphone invalide."

                firstName.length < 2 || lastName.length < 2 ->
                    "Nom et prénom invalides."

                else -> null
            }

            b.tvError.text = error
            b.tvError.isVisible = error != null

            if (error == null) {
                b.identityForm.isVisible = false
                b.resetForm.isVisible = true

                b.tvTitle.setText(R.string.forgot_password_new)
                b.tvDescription.text =
                    "Choisissez votre nouveau mot de passe."
            }
        }

        b.btnReset.setOnClickListener {

            val password = b.etNewPassword.text.toString()
            val confirm = b.etConfirmPassword.text.toString()

            when {
                password.length !in 8..72 -> {
                    b.tvError.text =
                        "Le mot de passe doit contenir 8 à 72 caractères."
                    b.tvError.isVisible = true
                }

                password != confirm -> {
                    b.tvError.setText(R.string.forgot_password_mismatch)
                    b.tvError.isVisible = true
                }

                else -> {
                    b.tvError.isVisible = false

                    val request = ResetPasswordRequest(
                        email = b.etEmail.text.toString().trim(),
                        phone = b.etPhone.text.toString().trim(),
                        firstName = b.etFirstName.text.toString().trim(),
                        lastName = b.etLastName.text.toString().trim(),
                        newPassword = password
                    )

                    vm.resetPassword(request)
                }
            }
        }

        vm.busy.observe(this) { busy ->
            b.btnReset.isEnabled = !busy
            b.btnContinue.isEnabled = !busy
        }

        vm.error.observe(this) { error ->
            b.tvError.text = error
            b.tvError.isVisible = !error.isNullOrBlank()
        }

        vm.passwordReset.observe(this) { event ->
            event.consume()?.let {
                Toast.makeText(
                    this,
                    "Mot de passe réinitialisé avec succès.",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
        }
    }
}
