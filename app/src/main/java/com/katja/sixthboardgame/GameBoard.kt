package com.katja.sixthboardgame

class GameBoard {

    var matrix : List<List<Stack<Stack.DiscColor>>> = List(5) { List(5) { Stack(mutableListOf()) } }

}