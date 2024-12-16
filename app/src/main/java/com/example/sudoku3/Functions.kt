package com.example.sudoku3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/*
fun updateCellValue(
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int,
    errorMessage: MutableState<String>
): Boolean {
    // Sayının geçerli olup olmadığını kontrol et
    if (!isValidMove(grid.value, row, col, value) { it.value }) {
        errorMessage.value = "Invalid move!"
        return false // Geçersiz hareket
    }

    // Geçerli ise hücreyi güncelle
    grid.value = grid.value.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            if (r == row && c == col) {
                cell.copy(value = value, possibilities = mutableSetOf()) // Değer güncelleme
            } else cell
        }
    }

    errorMessage.value = "" // Hata mesajını temizle
    return true // Geçerli hareket
}
*/

/*
@Composable
fun NumbersPanel(onNumberSelected: (Int) -> Unit) {
    val numbers = (1..9).toList()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp // Ekran genişliği
    val cellWidth = screenWidth / numbers.size // Her kutunun eşit genişliği

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        numbers.forEach { number ->
            Box(
                modifier = Modifier
                    .width(cellWidth - 8.dp) // Genişlik ekranın bir parçasına eşit
                    .height(50.dp) // Yükseklik sabit
                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    .clickable { onNumberSelected(number) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
*/
/*
fun updateCellPossibilities(
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int
) {
    grid.value = grid.value.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            if (r == row && c == col) {
                val updatedPossibilities = cell.possibilities.toMutableSet() // MutableSet'e dönüştür
                if (updatedPossibilities.contains(value)) {
                    updatedPossibilities.remove(value) // Olasılık kaldır
                } else {
                    updatedPossibilities.add(value) // Olasılık ekle
                }
                cell.copy(possibilities = updatedPossibilities)
            } else cell
        }
    }
}
*/
// SudokuGrid bileşeni
/*@Composable

fun SudokuGrid(
    grid: List<List<SudokuCell>>,
    errorMessage: String,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    isNotesMode: Boolean
) {
    val cellSize = calculateCellSize()

    Column {
        grid.forEach { row ->
            Row {
                row.forEach { cell ->
                    SudokuCellView(
                        cell = cell,
                        cellSize = cellSize,
                        isSelected = selectedCell?.let { it.first == cell.row && it.second == cell.col } == true,
                        onCellClick = { onCellClick(cell.row, cell.col) }
                    )
                }
            }
        }
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}*/

// SudokuCellView bileşeni
/*
@Composable
fun SudokuCellView(
    cell: SudokuCell,
    cellSize: Dp,
    isSelected: Boolean,
    onCellClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isError -> Color.Red.copy(alpha = 0.3f) // Yanlış giriş
        isSelected -> Color.Yellow.copy(alpha = 0.3f) // Seçili hücre
        else -> Color.White
    }
    val borderColorBlack = Color.Black
    val borderColorGray = Color.Gray

    Box(
        modifier = Modifier
            .size(cellSize)
           .border(1.dp, Color.Black)
            .background(backgroundColor)
            .clickable { onCellClick() },
        contentAlignment = Alignment.Center

    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                fontSize = (cellSize.value * 0.5).sp,
                fontWeight = FontWeight.Bold,
                color = if (cell.isError) Color.Red else Color.Black
            )
        } else if (cell.possibilities.isNotEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                cell.possibilities.chunked(3).forEach { row ->
                    Row {
                        row.forEach { possibility ->
                            Text(
                                text = possibility.toString(),
                                fontSize = (cellSize.value * 0.15).sp
                            )
                        }
                    }
                }
            }
        }
    }
}
*/
/*
fun getSelectedCell(grid: List<List<SudokuCell>>): Pair<Int?, Int?> {
    for ((rowIndex, row) in grid.withIndex()) {
        for ((colIndex, cell) in row.withIndex()) {
            if (cell.isSelected) return Pair(rowIndex, colIndex)
        }
    }
    return Pair(null, null)
}*/
@Composable
fun TimerScreenContent(timerViewModel: TimerViewModel) {
    val timerValue by timerViewModel.timer.collectAsState()

    TimerScreen(
        timerValue = timerValue,
        onStartClick = { timerViewModel.startTimer() },
        onPauseClick = { timerViewModel.pauseTimer() },
        onStopClick = { timerViewModel.stopTimer() }
    )
}
fun Long.formatTime(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val remainingSeconds = this % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}
@Composable
fun TimerScreen(
    timerValue: Long,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Icon(Icons.Filled.CheckCircle, contentDescription = "Timer Icon", modifier = Modifier.padding(end = 8.dp))
            Text(text = timerValue.formatTime(), fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = onStartClick) {
                Text("Start")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onPauseClick) {
                Text("Pause")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onStopClick) {
                Text("Stop")
            }
        }
    }
}
class TimerViewModel : ViewModel() {
    private val _timer = MutableStateFlow(0L)
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _timer.value++
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
    }

    fun stopTimer() {
        _timer.value = 0
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}