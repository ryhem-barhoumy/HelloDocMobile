package com.example.hellodoc.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.hellodoc.HelloDocApp

class HelloDocViewModelFactory(
    application: Application
) : ViewModelProvider.Factory {

    private val app = application as HelloDocApp

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            AuthViewModel::class.java ->
                AuthViewModel(app.authRepository)

            DoctorsViewModel::class.java ->
                DoctorsViewModel(app.doctorRepository)

            AppointmentsViewModel::class.java ->
                AppointmentsViewModel(app.appointmentRepository)

            ProfileViewModel::class.java ->
                ProfileViewModel(app.patientRepository)

            FavoritesViewModel::class.java ->
                FavoritesViewModel(
                    app.favoriteDoctorRepository,
                    app.authRepository
                )

            else -> throw IllegalArgumentException(
                "ViewModel inconnu : ${modelClass.name}"
            )
        } as T
    }
}