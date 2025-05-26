package com.example.sawaapplication.screens.event.presentation.vmModels

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.event.domain.useCases.CreateEventUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import android.util.Log
import com.example.sawaapplication.core.permissions.PermissionHandler
import com.example.sawaapplication.screens.event.domain.repository.EventRepository
import com.example.sawaapplication.screens.event.presentation.screens.toMap
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Job
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale



@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val createEventUseCase: CreateEventUseCase,
    private val permissionHandler: PermissionHandler,
    private val eventRepository : EventRepository

) : ViewModel() {

    var communityId by mutableStateOf<String?>("")
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var eventDate by mutableStateOf<Long?>(System.currentTimeMillis())
    var imageUri by mutableStateOf<Uri?>(null)
    var membersLimitInput by mutableStateOf("")
    var eventTime by mutableStateOf("")
    var location by mutableStateOf(GeoPoint(0.0, 0.0))
    var locationText by mutableStateOf("Location not set")
    private var job: Job? = null

    // val loading = mutableStateOf(false)
    // val success = mutableStateOf(false)
    // val error = mutableStateOf<String?>(null)
    var isMapVisible by mutableStateOf(false)
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    var editingEvent: Event? = null
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    val membersLimit: Int?
        get() = membersLimitInput.toIntOrNull()

    val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    fun createEvent(communityId: String) {
        if (loading.value) return // Prevent double submissions

        if (imageUri == null) {
            _error.value = "Please select an image."
            return
        }

        if (communityId.isEmpty()) {
            _error.value = "Community ID is missing."
            return
        }


        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // Change to 24-hour format
        val parsedTime = timeFormat.parse(eventTime)

        val calendar = Calendar.getInstance()

        calendar.timeInMillis = eventDate ?: System.currentTimeMillis()

        parsedTime?.let {
            val timeCal = Calendar.getInstance()
            timeCal.time = it

            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        val finalTimestamp = Timestamp(calendar.time)

        val event = Event(
            title = name.trim(),
            location = location,
            date = Date(eventDate ?: System.currentTimeMillis()).toString(),
            time = finalTimestamp,
            description = description.trim(),
            memberLimit = membersLimit ?: 0,
            createdBy = uid,
            imageUri = imageUri?.toString().orEmpty(),
            latitude = location.latitude,
            longitude = location.longitude
        )

    job = viewModelScope.launch {
            _loading.value = true
            try {
                createEventUseCase(communityId, event, imageUri!!)
                _success.value = true
                Log.d("CreateEvent", "Event created successfully: $communityId")
            } catch (e: Exception) {
                _error.value = "Failed to create event: ${e.message}"
                Log.e("CreateEvent", "Error creating event", e)
            } finally {
                _loading.value = false
            }
        }
    }
    fun loadEventForEdit(event: Event) {
        name = event.title
        description = event.description
        location = event.location
        locationText = "${event.location.latitude}, ${event.location.longitude}"
        eventDate = event.time?.toDate()?.time

        // Format Timestamp to "HH:mm"
        eventTime = event.time?.toDate()?.let {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
        } ?: ""

        membersLimitInput = event.memberLimit.toString()

        imageUri = if (event.imageUri.isNotEmpty()) Uri.parse(event.imageUri) else null
    }
    fun updateEvent(eventId: String) {
        job = viewModelScope.launch {
            _loading.value = true
            try {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val parsedTime = timeFormat.parse(eventTime)

                val calendar = Calendar.getInstance()
                calendar.timeInMillis = eventDate ?: System.currentTimeMillis()

                parsedTime?.let {
                    val timeCal = Calendar.getInstance()
                    timeCal.time = it
                    calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
                    calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                }

                val finalTimestamp = Timestamp(calendar.time)

                val updatedEvent = Event(
                    id = eventId,
                    title = name.trim(),
                    description = description.trim(),
                    location = location,
                    date = Date(eventDate ?: System.currentTimeMillis()).toString(),
                    time = finalTimestamp,
                    memberLimit = membersLimit ?: 0,
                    createdBy = uid,
                    imageUri = imageUri?.toString().orEmpty(),
                    latitude = location.latitude,
                    longitude = location.longitude

                )

                val cid = communityId ?: return@launch
                Log.d("EventUpdate", "cid=$cid, eventId=$eventId")

                eventRepository.updateEvent(cid, eventId, updatedEvent.toMap())
                Log.d("EventUpdate", "Update sent for eventId=$eventId with data=${updatedEvent.toMap()}")
                _success.value = true
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetSuccess() {
        _success.value = false
    }

    fun shouldRequestLocation() = permissionHandler.shouldRequestLocationPermission()
    fun markLocationPermissionRequested() = permissionHandler.markLocationPermissionRequested()

    fun shouldRequestPhoto() = permissionHandler.shouldRequestPhotoPermission()
    fun markPhotoPermissionRequested() = permissionHandler.markPhotoPermissionRequested()
}
