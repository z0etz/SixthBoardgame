package com.katja.sixthboardgame

class DiscStack(var discs: MutableList<DiscColor>) {

    enum class DiscColor {
        GRAY,
        BROWN
    }
    fun push(discColor: DiscColor) {
        discs.add(discColor)
    }
}