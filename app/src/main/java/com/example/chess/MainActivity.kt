package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

                    ChessBoard(
                        pieces = viewModel.pieces.collectAsStateWithLifecycle().value,
                        selectedPiece = viewModel.selectedPiece.collectAsStateWithLifecycle().value,
                        onSelectPiece = viewModel::selectPiece,
                        onMovePiece = viewModel::movePiece,
                        isShowingForWhite = true,
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(innerPadding)
                            .fillMaxSize()
                    )

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
                                        piece = ChessPiece.Horse,
                                        onClick = viewModel::onPawnDialogConfirm
                                    )
                                }
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
