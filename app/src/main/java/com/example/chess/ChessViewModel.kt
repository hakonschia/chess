package com.example.chess

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.math.abs

sealed interface GameState {
    data object Playing : GameState

    data class Mate(val whiteWon: Boolean) : GameState

    data object Stalemate : GameState
}

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

    private val _gameState = MutableStateFlow<GameState>(GameState.Playing)
    val gameState = _gameState.asStateFlow()

    fun selectPiece(position: Pair<Int, Int>) {
        if (_selectedPiece.value?.first == position) {
            _selectedPiece.value = null
        } else {
            val piece = _pieces.value[position] ?: return

            // Gotta wait for your turn mate
            if ((piece.isWhite && moves.value.size % 2 == 1) || (!piece.isWhite && moves.value.size % 2 == 0)) {
                return
            }

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

        if (isStalemate(board = pieces.value, whiteToPlay = moves.value.size % 2 == 0)) {
            _gameState.value = GameState.Stalemate
        } else if (isMate(board = pieces.value, white = true)) {
            _gameState.value = GameState.Mate(whiteWon = false)
        } else if (isMate(board = pieces.value, white = false)) {
            _gameState.value = GameState.Mate(whiteWon = true)
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
        _gameState.value = GameState.Playing
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

/**
 * @param previousMove The previous move that was made, this is used to generate en passant moves and can be set to null if you
 * don't care about en passant
 */
private fun getValidPositionsForPiece(
    board: Map<Pair<Int, Int>, ChessPieceButMore>,
    position: Pair<Int, Int>,
    previousMove: Pair<Pair<Int, Int>, Pair<Int, Int>>?,
    filterKingMoves: Boolean = true
): List<Pair<Int, Int>> {
    val piece = board[position] ?: return emptyList()

    fun isPositionEmpty(position: Pair<Int, Int>): Boolean {
        return !board.containsKey(position)
    }

    fun isPositionEmptyAndEnemyCannotMoveToPosition(position: Pair<Int, Int>): Boolean {
        return !board.containsKey(position) && !canEnemyMoveToPosition(
            board = board,
            position = position,
            isEnemyWhite = !piece.isWhite,
            // Need to filter out the current piece to avoid an infinite loop (at the very least this works for checking
            // for king castle moves, which is the only place we call this function now)
            filterPiece = piece.piece
        )
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
                        val rightTower = board[7 to 0]?.takeIf { it.piece == ChessPiece.Rook }
                        val leftTower = board[0 to 0]?.takeIf { it.piece == ChessPiece.Rook }

                        if (
                            rightTower?.hasBeenMoved == false &&
                            // You're only allowed to castle if the places between the king and the rook are empty and not
                            // no enemy pieces are able to move there
                            isPositionEmptyAndEnemyCannotMoveToPosition(5 to 0) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(6 to 0)
                        ) {
                            add(6 to 0)
                        }

                        if (
                            leftTower?.hasBeenMoved == false &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(1 to 0) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(2 to 0) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(3 to 0)
                        ) {
                            add(2 to 0)
                        }
                    } else {
                        val rightTower = board[7 to 7]?.takeIf { it.piece == ChessPiece.Rook }
                        val leftTower = board[0 to 7]?.takeIf { it.piece == ChessPiece.Rook }

                        if (
                            rightTower?.hasBeenMoved == false &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(5 to 7) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(6 to 7)
                        ) {
                            add(6 to 7)
                        }
                        if (
                            leftTower?.hasBeenMoved == false &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(1 to 7) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(2 to 7) &&
                            isPositionEmptyAndEnemyCannotMoveToPosition(3 to 7)
                        ) {
                            add(2 to 7)
                        }
                    }
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

        // We don't bother checking above if the positions are outside the board, but they should be removed here
        removeAll { move ->
            move.first !in 0..7 || move.second !in 0..7
        }

        // We are not allowed to put the king in check. If one of the positions would result in a check then it
        // needs to be removed
        if (filterKingMoves) {
            removeAll { move ->
                val boardAfterMove = board.toMutableMap().apply {
                    remove(position)?.let { piece ->
                        put(move, piece.copy(hasBeenMoved = true))
                    }
                }

                isKingInCheck(
                    board = boardAfterMove,
                    white = piece.isWhite
                )
            }
        }
    }
}

private fun isKingInCheck(board: Map<Pair<Int, Int>, ChessPieceButMore>, white: Boolean): Boolean {
    // To check if white is in a check we just get all the valid positions for black and check if the king is in one of
    // those positions
    val allMovesForOtherPlayer = board
        .filter { it.value.isWhite != white }
        .flatMap { piece ->
            getValidPositionsForPiece(
                board = board,
                position = piece.key,
                previousMove = null,
                // If we filter out king moves here it will result in an infinite loop
                // I imagine this is because I'm doing something weird and it's probably a more proper way of avoiding it
                // but this works so why not just do this
                filterKingMoves = false
            )
        }

    val positionOfKing = board.filter { it.value.piece == ChessPiece.King && it.value.isWhite == white }.keys.single()

    return allMovesForOtherPlayer.contains(positionOfKing)
}

/**
 * Checks if [white] is in a mate (ie. if [white] is true and this function returns true that means black won)
 */
private fun isMate(board: Map<Pair<Int, Int>, ChessPieceButMore>, white: Boolean): Boolean {
    // To check if white is in a mate we:
    // - Find every white piece
    //  - Find all movements that white piece can make
    //   - Check if all those move put the king in check
    // If all pieces match this then white is in a mate

    return board
        .filter { it.value.isWhite == white }
        .all { (currentPosition, _) ->
            val validPositionsForPiece = getValidPositionsForPiece(
                board = board,
                position = currentPosition,
                previousMove = null
            )

            // All moves result in a check
            validPositionsForPiece.all { validPosition ->
                val newBoard = board.toMutableMap().apply {
                    remove(currentPosition)?.let { piece ->
                        put(validPosition, piece)
                    }
                }

                isKingInCheck(
                    board = newBoard,
                    white = white
                )
            }
        }
}

private fun isStalemate(
    board: Map<Pair<Int, Int>, ChessPieceButMore>,
    whiteToPlay: Boolean
): Boolean {
    return board
        .filter { it.value.isWhite == whiteToPlay }
        .all { (position, _) ->
            getValidPositionsForPiece(
                board = board,
                position = position,
                previousMove = null
            ).isEmpty()
        } && !isMate(board = board, white = whiteToPlay)
}

private fun canEnemyMoveToPosition(
    board: Map<Pair<Int, Int>, ChessPieceButMore>,
    position: Pair<Int, Int>,
    isEnemyWhite: Boolean,
    filterPiece: ChessPiece
): Boolean {
    return board
        .filter { it.value.isWhite == isEnemyWhite }
        .filter { it.value.piece != filterPiece}
        .any { (piecePosition, _) ->
            getValidPositionsForPiece(
                board = board,
                position = piecePosition,
                previousMove = null,
                filterKingMoves = false
            ).contains(position)
        }
}
