
package com.example.hellodoc.viewmodel

import androidx.lifecycle.*
import com.example.hellodoc.model.*
import com.example.hellodoc.repository.DoctorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DoctorsViewModel(private val repo: DoctorRepository) : ViewModel() {

    private val _doctors = MutableLiveData(UiState<List<Doctor>>())
    val doctors: LiveData<UiState<List<Doctor>>> = _doctors

    private val _specialties = MutableLiveData<List<Specialty>>(emptyList())
    val specialties: LiveData<List<Specialty>> = _specialties

    private val _detail = MutableLiveData(UiState<DoctorDetail>())
    val detail: LiveData<UiState<DoctorDetail>> = _detail

    private val _slots = MutableLiveData(UiState<List<TimeSlot>>())
    val slots: LiveData<UiState<List<TimeSlot>>> = _slots

    private var searchJob: Job? = null

    var selectedDate: String =
        java.time.LocalDate.now(java.time.ZoneId.of("Africa/Tunis")).toString()
        private set

    fun search(q: String?, specialty: String?, city: String?, date: String?) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _doctors.value = UiState(
                loading = true,
                data = _doctors.value?.data
            )

            try {
                _doctors.value = UiState(
                    data = repo.search(q, specialty, city, date)
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _doctors.value = UiState(error = friendlyError(e))
            }
        }
    }

    fun searchAll(query: String) {
        searchJob?.cancel()

        searchJob = viewModelScope.launch {
            _doctors.value = UiState(
                loading = true,
                data = _doctors.value?.data
            )

            try {
                val allDoctors = repo.search(null, null, null, null)

                val keyword = query.trim()

                val filtered = if (keyword.isBlank()) {
                    allDoctors
                } else {
                    allDoctors.filter { doctor ->
                        doctor.displayName().contains(keyword, ignoreCase = true) ||
                                doctor.city.orEmpty().contains(keyword, ignoreCase = true) ||
                                doctor.location.orEmpty().contains(keyword, ignoreCase = true) ||
                                doctor.specialty.orEmpty().contains(keyword, ignoreCase = true)
                    }
                }

                _doctors.value = UiState(data = filtered)

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _doctors.value = UiState(error = friendlyError(e))
            }
        }
    }

    fun loadSpecialties() = viewModelScope.launch {
        try {
            _specialties.value = repo.specialties()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            _specialties.value = emptyList()
        }
    }

    fun loadDetail(id: String) = viewModelScope.launch {
        _detail.value = UiState(loading = true)

        try {
            _detail.value = UiState(data = repo.detail(id))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _detail.value = UiState(error = friendlyError(e))
        }
    }

    fun loadSlots(id: String, date: String) = viewModelScope.launch {
        selectedDate = date
        _slots.value = UiState(loading = true)

        try {
            _slots.value = UiState(data = repo.slots(id, date))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _slots.value = UiState(error = friendlyError(e))
        }
    }
}
