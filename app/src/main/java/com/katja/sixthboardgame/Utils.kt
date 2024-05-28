package com.katja.sixthboardgame

class Utils {
    companion object {
        fun getReceiverId(selectedUser: String, userMap: Map<String?, String?>): String? {
            return userMap[selectedUser]
        }
    }
}
