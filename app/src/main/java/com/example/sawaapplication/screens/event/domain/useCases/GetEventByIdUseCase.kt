package com.example.sawaapplication.screens.event.domain.useCases

import com.example.sawaapplication.screens.event.domain.model.Event
import com.example.sawaapplication.screens.event.domain.repository.EventRepository
import javax.inject.Inject

class GetEventByIdUseCase @Inject constructor(
    private val repository: EventRepository
) {
    suspend operator fun invoke(communityId: String, eventId: String): Event {
        return repository.getEventById(communityId,eventId)
    }
}