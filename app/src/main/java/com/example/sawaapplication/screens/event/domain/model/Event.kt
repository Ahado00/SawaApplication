package com.example.sawaapplication.screens.event.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: String = "",
    val time:Timestamp? = null,
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val memberLimit: Int = 0,
    val createdBy: String ="",
    val imageUri:String = "",
    val joinedUsers: List<String> = emptyList(),
    val communityId: String = "", // for fetch events
    val participants: Int = 0 ,// for fetch events
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)