package com.example.chess

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

class ChessViewModel : ViewModel() {

    private var idCounter = 0

    private val _pieces = MutableStateFlow(generateStartBoard())
    val pieces = _pieces.asStateFlow()

    private val _allPieces = MutableStateFlow(_pieces.value.values.toList().toList())
    val allPieces = _allPieces.asStateFlow()

    private val _takenPieces = MutableStateFlow<List<ChessPieceButMore>>(emptyList())
    val takenPieces = _takenPieces.asStateFlow()

    private val _selectedPiece = MutableStateFlow<Pair<Pair<Int, Int>, List<Pair<Int, Int>>>?>(null)
    val selectedPiece = _selectedPiece.asStateFlow()

    private val _moves = MutableStateFlow<List<Pair<Pair<Int, Int>, Pair<Int, Int>>>>(emptyList())
    val moves = _moves.asStateFlow()

    private val _showPawnConversionDialog = MutableStateFlow<Pair<Int, Int>?>(null)
    val showPawnConversionDialog = _showPawnConversionDialog.asStateFlow()

    fun selectPiece(position: Pair<Int, Int>) {
        if (_selectedPiece.value?.first == position) {
            _selectedPiece.value = null
        } else {
            _selectedPiece.value = position to getValidPositionsForPiece(
                board = pieces.value,
                position = position,
                previousMove = moves.value.lastOrNull()
            )
        }
    }

    fun movePiece(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        _selectedPiece.value = null

        _pieces.update { currentPieces ->
            currentPieces.toMutableMap().apply {
                remove(from)?.let { piece ->
                    remove(to)?.let { pieceAlreadyAtPosition ->
                        _takenPieces.update { it + pieceAlreadyAtPosition }
                    }

                    put(to, piece.copy(hasBeenMoved = true))

                    val lastMove = moves.value.lastOrNull()

                    when (piece.piece) {
                        ChessPiece.Pawn -> {
                            if (
                                lastMove != null &&
                                // Last move was a pawn move
                                currentPieces[lastMove.second]?.piece == ChessPiece.Pawn &&
                                // The pawn we are moving changed the row
                                from.first != to.first &&
                                // The pawn we're moving moved to the same row as the previous pawn
                                to.first == lastMove.second.first &&
                                // The last move was a 2 square move
                                abs(lastMove.second.second - lastMove.first.second) == 2
                            ) {
                                // holy hell
                                remove(lastMove.second)?.let { pieceAlreadyAtPosition ->
                                    _takenPieces.update { it + pieceAlreadyAtPosition }
                                }
                            } else {
                                if (piece.isWhite) {
                                    if (to.second == 7) {
                                        _showPawnConversionDialog.value = to
                                    }
                                } else {
                                    if (to.second == 0) {
                                        _showPawnConversionDialog.value = to
                                    }
                                }
                            }
                        }

                        ChessPiece.King -> {
                            // We are castling, need to manually move the tower
                            if (to.first - from.first == 2) {
                                put(5 to from.second, remove(7 to from.second)!!)
                            } else if (from.first - to.first == 2) {
                                put(3 to from.second, remove(0 to from.second)!!)
                            }
                        }

                        ChessPiece.Queen,
                        ChessPiece.Knight,
                        ChessPiece.Bishop,
                        ChessPiece.Rook -> {
                            // No special moves
                        }
                    }

                    _moves.update { it + (from to to) }
                }
            }
        }
    }

    fun onPawnDialogConfirm(piece: ChessPiece) {
        showPawnConversionDialog.value?.let { pawnPosition ->
            _pieces.update { board ->
                board.toMutableMap().apply {
                    val currentPiece = board[pawnPosition]!!
                    val newPiece = ChessPieceButMore(
                        piece = piece,
                        isWhite = currentPiece.isWhite,
                        hasBeenMoved = true,
                        id = idCounter++
                    )
                    _allPieces.update { it + newPiece }

                    put(
                        pawnPosition,
                        newPiece
                    )
                }
            }
        }
        _showPawnConversionDialog.value = null
    }

    fun reset() {
        _pieces.value = generateStartBoard()
        _allPieces.value = _pieces.value.values.toList()
        _takenPieces.value = emptyList()
        _selectedPiece.value = null
        _moves.value = emptyList()
    }

    private fun generateStartBoard(): Map<Pair<Int, Int>, ChessPieceButMore> {
        idCounter = 0

        return mapOf(
            // White
            0 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            1 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            2 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            3 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            4 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            5 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            6 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            7 to 1 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = true, hasBeenMoved = false, id = idCounter++),
            0 to 0 to ChessPieceButMore(piece = ChessPiece.Rook, isWhite = true, hasBeenMoved = false, id = idCounter++),
            1 to 0 to ChessPieceButMore(piece = ChessPiece.Knight, isWhite = true, hasBeenMoved = false, id = idCounter++),
            2 to 0 to ChessPieceButMore(piece = ChessPiece.Bishop, isWhite = true, hasBeenMoved = false, id = idCounter++),
            3 to 0 to ChessPieceButMore(piece = ChessPiece.Queen, isWhite = true, hasBeenMoved = false, id = idCounter++),
            4 to 0 to ChessPieceButMore(piece = ChessPiece.King, isWhite = true, hasBeenMoved = false, id = idCounter++),
            5 to 0 to ChessPieceButMore(piece = ChessPiece.Bishop, isWhite = true, hasBeenMoved = false, id = idCounter++),
            6 to 0 to ChessPieceButMore(piece = ChessPiece.Knight, isWhite = true, hasBeenMoved = false, id = idCounter++),
            7 to 0 to ChessPieceButMore(piece = ChessPiece.Rook, isWhite = true, hasBeenMoved = false, id = idCounter++),

            // Black
            0 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            1 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            2 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            3 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            4 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            5 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            6 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            7 to 6 to ChessPieceButMore(piece = ChessPiece.Pawn, isWhite = false, hasBeenMoved = false, id = idCounter++),
            0 to 7 to ChessPieceButMore(piece = ChessPiece.Rook, isWhite = false, hasBeenMoved = false, id = idCounter++),
            1 to 7 to ChessPieceButMore(piece = ChessPiece.Knight, isWhite = false, hasBeenMoved = false, id = idCounter++),
            2 to 7 to ChessPieceButMore(piece = ChessPiece.Bishop, isWhite = false, hasBeenMoved = false, id = idCounter++),
            3 to 7 to ChessPieceButMore(piece = ChessPiece.Queen, isWhite = false, hasBeenMoved = false, id = idCounter++),
            4 to 7 to ChessPieceButMore(piece = ChessPiece.King, isWhite = false, hasBeenMoved = false, id = idCounter++),
            5 to 7 to ChessPieceButMore(piece = ChessPiece.Bishop, isWhite = false, hasBeenMoved = false, id = idCounter++),
            6 to 7 to ChessPieceButMore(piece = ChessPiece.Knight, isWhite = false, hasBeenMoved = false, id = idCounter++),
            7 to 7 to ChessPieceButMore(piece = ChessPiece.Rook, isWhite = false, hasBeenMoved = false, id = idCounter++),
            )
    }
}

private fun getValidPositionsForPiece(
    board: Map<Pair<Int, Int>, ChessPieceButMore>,
    position: Pair<Int, Int>,
    previousMove: Pair<Pair<Int, Int>, Pair<Int, Int>>?
): List<Pair<Int, Int>> {
    val piece = board[position] ?: return emptyList()

    fun isPositionEmpty(position: Pair<Int, Int>): Boolean {
        return !board.containsKey(position)
    }

    fun isPositionTakenByEnemy(position: Pair<Int, Int>): Boolean {
        val pieceAtPosition = board[position] ?: return false

        return pieceAtPosition.isWhite != piece.isWhite
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
            ChessPiece.Pawn -> {
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

                    // en passant
                    if (
                        previousMove != null &&
                        previousMove.first.second - previousMove.second.second == 2 &&
                        previousMove.second.second == position.second
                    ) {
                        add(previousMove.second.first to previousMove.second.second + 1)
                    }
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

                    // en passant
                    if (
                        previousMove != null &&
                        previousMove.second.second - previousMove.first.second == 2 &&
                        previousMove.second.second == position.second
                    ) {
                        add(previousMove.second.first to previousMove.second.second - 1)
                    }
                }
            }

            ChessPiece.King -> {
                addIfPositionIsEmptyOrTakenByEnemy(position.first to position.second + 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first to position.second - 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second)
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second + 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second - 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second)
                addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second + 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second - 1)

                // Castling moves
                if (!piece.hasBeenMoved) {
                    if (piece.isWhite) {
                        val rightTower = board[7 to 0]?.takeIf { !it.hasBeenMoved && it.piece == ChessPiece.Rook }
                        val leftTower = board[0 to 0]?.takeIf { !it.hasBeenMoved && it.piece == ChessPiece.Rook }

                        if (
                            isPositionEmpty(5 to 0) &&
                            isPositionEmpty(6 to 0) &&
                            rightTower?.hasBeenMoved == false
                        ) {
                            add(6 to 0)
                        }

                        if (
                            isPositionEmpty(1 to 0) &&
                            isPositionEmpty(2 to 0) &&
                            isPositionEmpty(3 to 0) &&
                            leftTower?.hasBeenMoved == false
                        ) {
                            add(2 to 0)
                        }
                    } else {
                        val rightTower = board[7 to 7]?.takeIf { !it.hasBeenMoved && it.piece == ChessPiece.Rook }
                        val leftTower = board[0 to 7]?.takeIf { !it.hasBeenMoved && it.piece == ChessPiece.Rook }

                        if (
                            isPositionEmpty(5 to 7) &&
                            isPositionEmpty(6 to 7) &&
                            rightTower?.hasBeenMoved == false
                        ) {
                            add(6 to 7)
                        }
                        if (
                            isPositionEmpty(1 to 7) &&
                            isPositionEmpty(2 to 7) &&
                            isPositionEmpty(3 to 7) &&
                            leftTower?.hasBeenMoved == false
                        ) {
                            add(2 to 7)
                        }
                    }
                }

                // The king isn't allowed to put itself in a mate, if one of the positions would return in a mate
                // then it needs to be removed
                removeAll { move ->
                    val boardAfterMove = board.toMutableMap().apply {
                        remove(position)?.let { piece ->
                            put(move, piece)
                        }
                    }

                    isMate(board = boardAfterMove, white = piece.isWhite)
                }
            }

            ChessPiece.Queen -> {
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

            ChessPiece.Knight -> {
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second + 2)
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 1 to position.second - 2)

                addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second + 2)
                addIfPositionIsEmptyOrTakenByEnemy(position.first - 1 to position.second - 2)

                addIfPositionIsEmptyOrTakenByEnemy(position.first + 2 to position.second - 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first + 2 to position.second + 1)

                addIfPositionIsEmptyOrTakenByEnemy(position.first - 2 to position.second - 1)
                addIfPositionIsEmptyOrTakenByEnemy(position.first - 2 to position.second + 1)
            }

            ChessPiece.Bishop -> {
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

            ChessPiece.Rook -> {
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

private fun isMate(board: Map<Pair<Int, Int>, ChessPieceButMore>, white: Boolean): Boolean {
    // To check if white is in a mate we just get all the valid positions for black and check if the king is in one of
    // those positions
    val allMovesForOtherPlayer = board
        .filter { it.value.isWhite != white }
        .filter { it.value.piece != ChessPiece.King }
        .flatMap { piece ->
            getValidPositionsForPiece(board, piece.key, null)
        }

    val positionOfKing = board.filter { it.value.piece == ChessPiece.King && it.value.isWhite == white }.keys.single()

    return allMovesForOtherPlayer.contains(positionOfKing)
}
