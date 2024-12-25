package com.example.sudoku3
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun showWinDialog(onDismiss: () -> Unit, onNewGame: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Congratulations!") },
        text = { Text("You have successfully completed the Sudoku puzzle!") },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("New Game")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun showLoseDialog(onDismiss: () -> Unit, onNewGame: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Game Over") },
        text = { Text("You have made 3 errors! Game over.") },
        confirmButton = {
            Button(onClick = onNewGame) {
                Text("New Game")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
fun maskGrid(fullGrid: List<List<Int>>, difficulty: String): List<List<Int>> {
    val mutableGrid = fullGrid.map { it.toMutableList() }.toMutableList()
    val totalCells = 81
    val cellsToKeep = when (difficulty) {
        "Easy" -> 40
        "Medium" -> 30
        "Hard" -> 20
        else -> 30
    }

    val cellsToRemove = totalCells - cellsToKeep
    val positions = (0 until totalCells).shuffled()

    for (index in 0 until cellsToRemove) {
        val row = positions[index] / 9
        val col = positions[index] % 9
        mutableGrid[row][col] = 0 // Hücre boşaltılıyor
    }

    return mutableGrid.map { it.toList() }
}
