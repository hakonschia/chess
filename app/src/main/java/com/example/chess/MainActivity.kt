package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chess.components.ChessBoard
import com.example.chess.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel<ChessViewModel>()
                    val showPawnDialog by viewModel.showPawnConversionDialog.collectAsStateWithLifecycle()
                    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
                    val moves by viewModel.moves.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                    ) {
                        var isShowingForWhite by rememberSaveable { mutableStateOf(true) }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    isShowingForWhite = !isShowingForWhite
                                }
                            ) {
                                Text("Change player perspective")
                            }

                            Button(
                                onClick = viewModel::reset
                            ) {
                                Text("Reset")
                            }
                        }

                        Spacer(Modifier.size(24.dp))

                        Text(
                            text = if (moves.size % 2 == 0) {
                                "White's turn"
                            } else {
                                "Black's turn"
                            }
                        )

                        Spacer(Modifier.size(24.dp))

                        ChessBoard(
                            pieces = viewModel.pieces.collectAsStateWithLifecycle().value,
                            allPieces = viewModel.allPieces.collectAsStateWithLifecycle().value,
                            takenPieces = viewModel.takenPieces.collectAsStateWithLifecycle().value,
                            selectedPiece = viewModel.selectedPiece.collectAsStateWithLifecycle().value,
                            moves = moves,
                            onSelectPiece = viewModel::selectPiece,
                            onMovePiece = viewModel::movePiece,
                            isShowingForWhite = isShowingForWhite,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }

                    if (showPawnDialog != null) {
                        Dialog(
                            onDismissRequest = {
                                // Not really optional to make a choice
                            }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(Color.Gray)
                                    .padding(vertical = 48.dp)
                                    .fillMaxWidth()
                            ) {
                                Text("Choose which piece you want")

                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    PieceDialogButton(
                                        piece = ChessPiece.Rook,
                                        onClick = viewModel::onPawnDialogConfirm
                                    )

                                    PieceDialogButton(
                                        piece = ChessPiece.Queen,
                                        onClick = viewModel::onPawnDialogConfirm
                                    )
                                    PieceDialogButton(
                                        piece = ChessPiece.Bishop,
                                        onClick = viewModel::onPawnDialogConfirm
                                    )

                                    PieceDialogButton(
                                        piece = ChessPiece.Knight,
                                        onClick = viewModel::onPawnDialogConfirm
                                    )
                                }
                            }
                        }
                    }

                    when (val gameState = gameState) {
                        is GameState.Mate -> {
                            Dialog(
                                onDismissRequest = viewModel::reset
                            ) {
                                if (gameState.whiteWon) {
                                    Text("White wins!")
                                } else {
                                    Text("Black wins!")
                                }
                            }
                        }

                        is GameState.Playing -> {

                        }

                        is GameState.Stalemate -> {
                            Dialog(
                                onDismissRequest = viewModel::reset
                            ) {
                                Text("It's a stalemate!")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieceDialogButton(
    piece: ChessPiece,
    onClick: (ChessPiece) -> Unit
) {
    Button(
        onClick = {
            onClick(piece)
        }
    ) {
        Text(piece.name)
    }
}
