package com.example.hellodoc.viewmodel

import androidx.lifecycle.*
import android.app.Application
import com.example.hellodoc.HelloDocApp

data class UiState<T>(val loading: Boolean = false, val data: T? = null, val error: String? = null)
class OneTime<T>(private val item: T) {
    private var handled = false
    fun consume(): T? = if (handled) null else {
        handled = true; item
    }
}
fun friendlyError(e: Exception): String = when (e) {
    is java.io.IOException -> "Connexion impossible. Vérifiez le Wi-Fi, l'IP du PC et Spring Boot."
    else -> e.message ?: "Une erreur est survenue."
}


