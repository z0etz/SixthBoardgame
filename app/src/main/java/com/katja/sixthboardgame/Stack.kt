package com.katja.sixthboardgame

class Stack(var discs: MutableList<DiscColor>) {

    enum class DiscColor {
        GRAY,
        BROWN
    }
    fun push(discColor: DiscColor) {
        discs.add(discColor)
    }
}