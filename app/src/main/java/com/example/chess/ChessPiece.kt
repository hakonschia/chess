package com.example.chess

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

enum class ChessPiece {
    Pawn,
    King,
    Queen,
    Knight,
    Bishop,
    Rook,
}

@Composable
fun ChessPiece.asPainter(): Painter {
    return painterResource(
        when (this) {
            ChessPiece.Pawn -> R.drawable.pawn
            ChessPiece.King -> R.drawable.king
            ChessPiece.Queen -> R.drawable.queen
            ChessPiece.Knight -> R.drawable.knight
            ChessPiece.Bishop -> R.drawable.bishop
            ChessPiece.Rook -> R.drawable.rook
        }
    )
}

data class ChessPieceButMore(
    val piece: ChessPiece,
    val isWhite: Boolean,
    val hasBeenMoved: Boolean,
    val id: Int
)
