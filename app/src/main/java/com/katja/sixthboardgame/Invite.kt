package com.katja.sixthboardgame

data class Invite(
    val inviteId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val selectedTime: Int = 0,
    val status: String = "pending"
)
