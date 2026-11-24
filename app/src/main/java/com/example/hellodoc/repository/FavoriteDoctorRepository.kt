package com.example.hellodoc.repository

import androidx.lifecycle.LiveData
import com.example.hellodoc.data.local.dao.FavoriteDoctorDao
import com.example.hellodoc.data.local.entity.FavoriteDoctorEntity

class FavoriteDoctorRepository(
    private val dao: FavoriteDoctorDao
) {

    fun observeAll(
        ownerId: String
    ): LiveData<List<FavoriteDoctorEntity>> {
        return dao.observeAll(ownerId)
    }

    fun search(
        ownerId: String,
        query: String
    ): LiveData<List<FavoriteDoctorEntity>> {
        return dao.search(ownerId, query.trim())
    }

    suspend fun insert(favorite: FavoriteDoctorEntity): Long {
        require(favorite.ownerId.isNotBlank()) {
            "Le compte patient est introuvable."
        }
        require(favorite.doctorId.isNotBlank()) {
            "Le médecin est introuvable."
        }

        return dao.insert(favorite)
    }

    suspend fun updateLabel(
        ownerId: String,
        id: Long,
        label: String
    ): Boolean {
        val favorite = dao.getById(ownerId, id) ?: return false

        val updatedFavorite = favorite.copy(
            personalLabel = label.trim()
        )

        return dao.update(updatedFavorite) == 1
    }

    suspend fun delete(
        ownerId: String,
        id: Long
    ): Boolean {
        val favorite = dao.getById(ownerId, id) ?: return false

        return dao.delete(favorite) == 1
    }
}