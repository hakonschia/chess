package com.example.chess.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.chess.ChessPieceButMore

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChessBoard(
    pieces: Map<Pair<Int, Int>, ChessPieceButMore>,
    selectedPiece: Pair<Pair<Int, Int>, List<Pair<Int, Int>>>?,
    onSelectPiece: (Pair<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
    ) {
        val boxSize = if (maxWidth > maxHeight) {
            maxHeight / 8
        } else {
            maxWidth / 8
        }

        Row {
            for (i in 0 until 8) {
                Column {
                    for (j in 7 downTo 0) {
                        val piecePosition = i to j

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clickable {
                                    onSelectPiece(i to j)
                                }
                                .size(boxSize)
                                .background(
                                    if (selectedPiece?.first == piecePosition) {
                                        Color.Red.copy(alpha = 0.25f)
                                    } else if (selectedPiece?.second?.contains(piecePosition) == true) {
                                        Color.Green.copy(alpha = 0.25f)
                                    } else {
                                        if (i % 2 == 0) {
                                            if (j % 2 == 0) {
                                                Color.Gray
                                            } else {
                                                Color.White
                                            }
                                        } else {
                                            if (j % 2 == 0) {
                                                Color.White
                                            } else {
                                                Color.Gray
                                            }
                                        }
                                    }
                                )
                        ) {
                            val piece = pieces.getOrDefault(piecePosition, null)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (piece != null) {
                                    Text(
                                        text = piece.piece.name
                                    )
                                }

                                Text(
                                    text = "${('A'.code + i).toChar()}$j",
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
