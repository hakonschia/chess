package com.example.chess

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChessViewModel : ViewModel() {

    private val _pieces = MutableStateFlow(
        mapOf(
            // White
            0 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            1 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            2 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            3 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            4 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            5 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            6 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            7 to 1 to ChessPieceButMore(ChessPiece.Bonde, true),
            0 to 0 to ChessPieceButMore(ChessPiece.Tårn, true),
            1 to 0 to ChessPieceButMore(ChessPiece.Hest, true),
            2 to 0 to ChessPieceButMore(ChessPiece.Løper, true),
            3 to 4 to ChessPieceButMore(ChessPiece.Dronning, true),
            4 to 0 to ChessPieceButMore(ChessPiece.Konge, true),
            5 to 0 to ChessPieceButMore(ChessPiece.Løper, true),
            6 to 0 to ChessPieceButMore(ChessPiece.Hest, true),
            7 to 0 to ChessPieceButMore(ChessPiece.Tårn, true),

            // Black
            0 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            1 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            2 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            3 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            4 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            5 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            6 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            7 to 6 to ChessPieceButMore(ChessPiece.Bonde, false),
            0 to 7 to ChessPieceButMore(ChessPiece.Tårn, false),
            1 to 7 to ChessPieceButMore(ChessPiece.Hest, false),
            2 to 7 to ChessPieceButMore(ChessPiece.Løper, false),
            3 to 7 to ChessPieceButMore(ChessPiece.Dronning, false),
            4 to 7 to ChessPieceButMore(ChessPiece.Konge, false),
            5 to 7 to ChessPieceButMore(ChessPiece.Løper, false),
            6 to 7 to ChessPieceButMore(ChessPiece.Hest, false),
            7 to 7 to ChessPieceButMore(ChessPiece.Tårn, false),
        )
    )
    val pieces = _pieces.asStateFlow()

    private val _selectedPiece = MutableStateFlow<Pair<Pair<Int, Int>, List<Pair<Int, Int>>>?>(null)
    val selectedPiece = _selectedPiece.asStateFlow()

    fun selectPiece(position: Pair<Int, Int>) {
        if (_selectedPiece.value?.first == position) {
            _selectedPiece.value = null
        } else {
            _selectedPiece.value = position to getValidPositionsForPiece(position)
        }
    }

    private fun isPositionEmpty(position: Pair<Int, Int>): Boolean {
        return _pieces.value[position] == null
    }

    private fun getValidPositionsForPiece(position: Pair<Int, Int>): List<Pair<Int, Int>> {
        val piece = _pieces.value[position] ?: return emptyList()

        fun isPositionTakenByEnemy(position: Pair<Int, Int>): Boolean {
            return pieces.value[position]?.isWhite != piece.isWhite
        }

        fun MutableList<Pair<Int, Int>>.addIfPositionIsEmpty(position: Pair<Int, Int>) {
            if (isPositionEmpty(position)) {
                add(position)
            }
        }

        fun MutableList<Pair<Int, Int>>.addIfPositionIsTakenByEnemy(position: Pair<Int, Int>) {
            if (isPositionTakenByEnemy(position)) {
                add(position)
            }
        }

        fun MutableList<Pair<Int, Int>>.addIfPositionIsEmptyOrTakenByEnemy(position: Pair<Int, Int>) {
            addIfPositionIsEmpty(position)
            addIfPositionIsTakenByEnemy(position)
        }

        fun MutableList<Pair<Int, Int>>.addWhilePositionIsEmptyOrTakenByEnemy(positionLambda: (Int) -> Pair<Int, Int>) {
            for (i in 0 until 8) {
                val newPosition = positionLambda(i)

                if (position == newPosition) {
                    continue
                }

                if (isPositionEmpty(newPosition)) {
                    add(newPosition)
                } else if (isPositionTakenByEnemy(newPosition)) {
                    add(newPosition)
                    return
                } else {
                    // Taken by friendly piece
                    return
                }
            }
        }

        return buildList {
            when (piece.piece) {
                ChessPiece.Bonde -> {
                    // TODO en passant

                    if (piece.isWhite) {
                        if (isPositionEmpty(position.first to position.second + 1)) {
                            addIfPositionIsEmpty(position.first to position.second + 1)

                            if (position.second == 1) {
                                addIfPositionIsEmpty(position.first to position.second + 2)
                            }
                        }

                        // Piece at diagonal can be taken by the pawn
                        addIfPositionIsTakenByEnemy(position.first + 1 to position.second + 1)
                        addIfPositionIsTakenByEnemy(position.first - 1 to position.second + 1)
                    } else {
                        if (isPositionEmpty(position.first to position.second - 1)) {
                            // Pawn can go forward one if it is empty
                            addIfPositionIsEmpty(position.first to position.second - 1)

                            if (position.second == 6) {
                                addIfPositionIsEmpty(position.first to position.second - 2)
                            }
                        }

                        // Piece at diagonal can be taken by the pawn
                        addIfPositionIsTakenByEnemy(position.first - 1 to position.second - 1)
                        addIfPositionIsTakenByEnemy(position.first + 1 to position.second - 1)
                    }
                }

                ChessPiece.Konge -> {
                    addIfPositionIsEmptyOrTakenByEnemy(position.first to position.second + 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first to position.second - 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second + 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second - 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second + 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second - 1)
                }

                ChessPiece.Dronning -> {
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second - counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second - counter
                    }

                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first to position.second - counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second
                    }
                }

                ChessPiece.Hest -> {
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second + 2)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second - 2)

                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second + 2)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second - 2)

                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 2 to position.second - 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first + 2 to position.second + 1)

                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 2 to position.second - 1)
                    addIfPositionIsEmptyOrTakenByEnemy(position.first - 2 to position.second + 1)
                }

                ChessPiece.Løper -> {
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second - counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second - counter
                    }
                }

                ChessPiece.Tårn -> {
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first to position.second + counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first to position.second - counter
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first + counter to position.second
                    }
                    addWhilePositionIsEmptyOrTakenByEnemy { counter ->
                        position.first - counter to position.second
                    }
                }
            }
        }
    }
}
