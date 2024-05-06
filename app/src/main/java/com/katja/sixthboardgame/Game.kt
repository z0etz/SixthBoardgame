package com.katja.sixthboardgame

class Game(var id: String, val playerIds: List<String>, var nextPlayer: String, var gameboard: List<List<Stack>>) {
}