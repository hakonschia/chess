package com.example.chess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.chess.components.ChessBoard
import com.example.chess.ui.theme.ChessTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = remember { ChessViewModel() }

                    ChessBoard(
                        pieces = viewModel.pieces.collectAsState().value,
                        selectedPiece = viewModel.selectedPiece.collectAsState().value,
                        onSelectPiece = viewModel::selectPiece,
                        onMovePiece = viewModel::movePiece,
                        modifier = Modifier
                            .background(Color.Black)
                            .padding(innerPadding)
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}
