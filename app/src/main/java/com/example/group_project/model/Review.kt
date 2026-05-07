package com.example.group_project.model

// Firebase needs a no-arg class with mutable fields
class Review {
    var reviewId: String = ""
    var spotId: String = ""
    var authorId: String = ""
    var userId: String = ""
    var username: String = ""
    var userEmail: String = ""
    var timestamp: Long = 0L
    var clientCreatedAt: Long = 0L
    var spotName: String = ""
    var buildingName: String = ""
    var roomNumber: String = ""
    var starRating: Double = 0.0
    var description: String = ""
    var traits: List<String> = emptyList()
    var quiet: Boolean = false
    var moderatelyLoud: Boolean = false
    var loud: Boolean = false
    var visible: Boolean = false
    var secluded: Boolean = false
}
