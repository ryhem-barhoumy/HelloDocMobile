package com.example.hellodoc.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity
import com.example.hellodoc.model.DoctorDetail
import com.example.hellodoc.repository.AuthRepository
import com.example.hellodoc.repository.FavoriteDoctorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoriteDoctorRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _query = MutableLiveData("")

    val favorites: LiveData<List<FavoriteDoctorEntity>> =
        authRepository.currentUser.switchMap { user ->
            if (user == null || !user.isPatient) {
                MutableLiveData<List<FavoriteDoctorEntity>>(emptyList())
            } else {
                _query.switchMap { text ->
                    val query = text.orEmpty().trim()

                    if (query.isEmpty()) {
                        repository.observeAll(user.id)
                    } else {
                        repository.search(user.id, query)
                    }
                }
            }
        }

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean> = _busy

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _message = MutableLiveData<OneTime<String>>()
    val message: LiveData<OneTime<String>> = _message

    fun setSearchQuery(query: String) {
        _query.value = query.trim()
    }

    fun add(doctor: DoctorDetail) = runAction { ownerId ->
        val name = doctor.fullName.orEmpty().trim()

        require(name.isNotEmpty()) {
            "Rechargez la fiche du médecin avant de l’enregistrer."
        }

        val favorite = FavoriteDoctorEntity(
            ownerId = ownerId,
            doctorId = doctor.id,
            doctorName = name,
            specialty = doctor.specialty,
            city = doctor.city
        )

        val insertedId = repository.insert(favorite)

        if (insertedId == -1L) {
            "Ce médecin est déjà dans vos favoris."
        } else {
            "Médecin ajouté aux favoris."
        }
    }

    fun updateLabel(
        favorite: FavoriteDoctorEntity,
        label: String
    ) = runAction { ownerId ->
        require(favorite.ownerId == ownerId) {
            "Favori introuvable."
        }

        check(repository.updateLabel(ownerId, favorite.id, label)) {
            "Ce favori n’existe plus."
        }

        "Libellé modifié."
    }

    fun delete(favorite: FavoriteDoctorEntity) =
        runAction { ownerId ->
            require(favorite.ownerId == ownerId) {
                "Favori introuvable."
            }

            check(repository.delete(ownerId, favorite.id)) {
                "Ce favori n’existe plus."
            }

            "Médecin retiré des favoris."
        }

    private fun runAction(
        block: suspend (ownerId: String) -> String
    ) {
        if (_busy.value == true) return

        val user = authRepository.currentUser.value

        if (user == null || !user.isPatient || user.id.isBlank()) {
            _error.value = "Connectez-vous à votre compte patient."
            return
        }

        val ownerId = user.id
        _busy.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = block(ownerId)

                if (authRepository.currentUser.value?.id == ownerId) {
                    _message.value = OneTime(result)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (authRepository.currentUser.value?.id == ownerId) {
                    _error.value = friendlyError(e)
                }
            } finally {
                _busy.value = false
            }
        }
    }
}