package com.example.chess.components

import android.annotation.SuppressLint
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.unit.dp
import com.example.chess.ChessPieceButMore
import com.example.chess.asPainter

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ChessBoard(
    pieces: Map<Pair<Int, Int>, ChessPieceButMore>,
    takenPieces: List<ChessPieceButMore>,
    selectedPiece: Pair<Pair<Int, Int>, List<Pair<Int, Int>>>?,
    onSelectPiece: (Pair<Int, Int>) -> Unit,
    onMovePiece: (Pair<Int, Int>, Pair<Int, Int>) -> Unit,
    isShowingForWhite: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
    ) {
        LookaheadScope {
            // TODO this assumes that at startup pieces include all the pieces, which probably won't be correct
            //  if pawns are promoted. Might be enough to just remember this by pieces.size
            val pieceComposables = remember {
                pieces.map { (_, piece) ->
                    piece.id to movableContentOf {
                        Icon(
                            painter = piece.piece.asPainter(),
                            contentDescription = piece.piece.name,
                            tint = if (piece.isWhite) Color.LightGray else Color.DarkGray,
                            modifier = Modifier
                                .animateBounds(
                                    lookaheadScope = this@LookaheadScope,
                                    boundsTransform = BoundsTransform { _, _ ->
                                        tween(
                                            durationMillis = 1000,
                                        )
                                    }
                                )
                        )
                    }
                }.associate { it }
            }

            BoxWithConstraints {
                val boxSize = if (maxWidth > maxHeight) {
                    maxHeight / 8
                } else {
                    maxWidth / 8
                }

                Row(
                    modifier = Modifier
                        // The background for the entire board is drawn on this Row instead of on each piece
                        // because the background causes clipping issues when animating the pieces across the board
                        .drawBehind {
                            for (i in 0 until 64) {
                                val row = i % 8
                                val column = i / 8

                                val color = if (row % 2 == 0) {
                                    if (column % 2 == 0) {
                                        Color.Gray
                                    } else {
                                        Color.White
                                    }
                                } else {
                                    if (column % 2 == 0) {
                                        Color.White
                                    } else {
                                        Color.Gray
                                    }
                                }

                                drawRect(
                                    color = color,
                                    topLeft = Offset(
                                        x = (boxSize * column).toPx(),
                                        y = (boxSize * row).toPx()
                                    ),
                                    size = Size(boxSize.toPx(), boxSize.toPx())
                                )
                            }
                        }
                ) {
                    for (i in 0 until 8) {
                        Column {
                            val range = if (isShowingForWhite) {
                                7 downTo 0
                            } else {
                                0 until 8
                            }

                            for (j in range) {
                                val piecePosition = i to j
                                val canMoveToPosition = selectedPiece?.second?.contains(piecePosition) == true

                                val backgroundColor by animateColorAsState(
                                    if (selectedPiece?.first == piecePosition) {
                                        Color.Red.copy(alpha = 0.25f)
                                    } else if (canMoveToPosition) {
                                        Color.Green.copy(alpha = 0.25f)
                                    } else {
                                        Color.Unspecified
                                    }
                                )

                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clickable {
                                            if (canMoveToPosition) {
                                                onMovePiece(selectedPiece.first, piecePosition)
                                            } else {
                                                onSelectPiece(piecePosition)
                                            }
                                        }
                                        .drawBehind {
                                            drawRect(backgroundColor)
                                        }
                                        .size(boxSize)
                                ) {
                                    val piece = pieces.getOrDefault(piecePosition, null)

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        pieceComposables[piece?.id]?.invoke()

                                        Text(
                                            text = "${('A'.code + i).toChar()}${j + 1}",
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.size(36.dp))

            Text(
                text = "White pieces taken"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                takenPieces.filter { it.isWhite }.forEach { piece ->
                    pieceComposables[piece.id]?.invoke()
                }
            }

            Spacer(Modifier.size(24.dp))

            Text(
                text = "Black pieces taken"
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                takenPieces.filter { !it.isWhite }.forEach { piece ->
                    pieceComposables[piece.id]?.invoke()
                }
            }
        }
    }
}
