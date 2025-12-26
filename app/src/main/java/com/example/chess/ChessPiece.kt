package com.example.chess

enum class ChessPiece {
    Bonde,
    Konge,
    Dronning,
    Hest,
    Løper,
    Tårn,
}

data class ChessPieceButMore(
    val piece: ChessPiece,
    val isWhite: Boolean,
    val hasBeenMoved: Boolean
)
