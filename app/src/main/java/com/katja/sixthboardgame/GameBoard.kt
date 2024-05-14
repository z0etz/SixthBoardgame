package com.katja.sixthboardgame

class GameBoard {

    var matrix: List<List<DiscStack>> = List(5) { List(5) { DiscStack(mutableListOf()) } }

}
