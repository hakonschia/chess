package com.example.chess

enum class ChessPiece {
    Pawn,
    King,
    Queen,
    Horse,
    Bishop,
    Rook,
}

data class ChessPieceButMore(
    val piece: ChessPiece,
    val isWhite: Boolean,
    val hasBeenMoved: Boolean,
    val id: Int
)
