package com.example.sudoku3

import android.annotation.SuppressLint
import android.os.Bundle
import android.system.Os.remove
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color



import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.sudoku3.ui.theme.Sudoku3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sudokuGrid = remember { mutableStateOf(generateSudokuGrid("Medium")) }
            Log.d("SudokuGridDebug", "Initial Grid: ${sudokuGrid.value}")

            val errorMessage = remember { mutableStateOf("") }
            val selectedDifficulty = remember { mutableStateOf("Easy") }
            val isNotesMode = remember { mutableStateOf(false) }
            val timerSeconds = remember { mutableStateOf(0) }
            val gameFinished = remember { mutableStateOf(false) }
            val gameWon = remember { mutableStateOf(false) }
            val errorCount = remember { mutableStateOf(0) }
            val isPaused = remember { mutableStateOf(false) }


            SudokuTimer(timerSeconds = timerSeconds, gameFinished = gameFinished, isPaused = isPaused)

            // Timer Coroutine
            LaunchedEffect(Unit) {
                while (!gameFinished.value) {
                    delay(1000L)
                    timerSeconds.value++
                }
            }
            Sudoku3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {



                    val finishedTime = remember { mutableStateOf(0) }
                    if (gameWon.value) {
                        if (finishedTime.value == 0) {
                            finishedTime.value = timerSeconds.value // Süreyi sabitle
                        }
                        isPaused.value = true
                        AlertDialog(
                            onDismissRequest = { gameWon.value = false },
                            title = { Text("Congratulations!") },
                            text = {
                                Column {
                                    Text("You have successfully completed the Sudoku puzzle!")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Difficulty: ${selectedDifficulty.value}", fontWeight = FontWeight.Bold)
                                    Text("Time Taken: ${formatTime(finishedTime.value)}", fontWeight = FontWeight.Bold)
                                }
                                   },
                            confirmButton = {
                                Button(onClick = {
                                    sudokuGrid.value = generateSudokuGrid(selectedDifficulty.value)
                                    Log.d("SudokuGridDebug", "Initial Grid: ${sudokuGrid.value}")

                                    timerSeconds.value = 0
                                    gameFinished.value = false
                                    errorMessage.value = ""
                                    errorCount.value = 0
                                    gameWon.value = false
                                    isPaused.value = false
                                }) {
                                    Text("New Game")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { gameWon.value = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    } else if (gameFinished.value && errorCount.value >= 3) {
                        AlertDialog(
                            onDismissRequest = { gameFinished.value = false },
                            title = { Text("Game Over") },
                            text = { Text("You have made 3 errors! Game over.") },
                            confirmButton = {
                                Button(onClick = {
                                    sudokuGrid.value = generateSudokuGrid(selectedDifficulty.value)
                                    timerSeconds.value = 0
                                    gameFinished.value = false
                                    errorMessage.value = ""
                                    errorCount.value = 0
                                }) {
                                    Text("New Game")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { gameFinished.value = false }) {
                                    Text("Close")
                                }
                            }
                        )
                    } else {
                        SudokuScreen(
                            sudokuGrid = sudokuGrid,
                            errorMessage = errorMessage,
                            selectedDifficulty = selectedDifficulty,
                            isNotesMode = isNotesMode,
                            gameFinished = gameFinished,
                            gameWon = gameWon,
                            errorCount = errorCount,
                            isPaused = remember { mutableStateOf(false)} ,
                            timerSeconds = timerSeconds, isLocked = false
                        )
                    }
                }
            }
        }
    }
}
fun timerFlow(): Flow<Int> = flow {
    var seconds = 0
    while (true) {
        emit(seconds++)
        delay(1000L)
    }
}

@Composable
fun SudokuTimer(timerSeconds: MutableState<Int>, gameFinished: MutableState<Boolean>, isPaused: MutableState<Boolean>) {
    LaunchedEffect(key1 = gameFinished.value, key2 = isPaused.value) {
        if (!gameFinished.value && !isPaused.value) {
            timerFlow()
                .onEach { time -> timerSeconds.value = time }
                .launchIn(this)
        }
    }
}



// Sudoku hücresini temsil eden veri sınıfı
data class SudokuCell(
    val row: Int,
    val col: Int,
    val value: Int = 0,
    val notes: Set<Int> = emptySet(), // Notlar için ek alan
    val isError: Boolean = false,
    var possibilities: MutableSet<Int> = mutableSetOf(), // Olası sayılar
    var isNoteMode: Boolean = false, // Not modu açık/kapalı
    var isSelected: Boolean = false, // Hücre seçili mi
    var isHighlighted: Boolean = false, // Satır, sütun ve 3x3 grid vurgulama
    var isMatching: Boolean = false, // Aynı değer vurgulama
    var isLocked: Boolean = false // Yeni alan eklendi.
)


@SuppressLint("UnrememberedMutableState")
@Composable
fun SudokuScreen(
    sudokuGrid: MutableState<List<List<SudokuCell>>>,
    errorMessage: MutableState<String>,
    selectedDifficulty: MutableState<String>,
    isNotesMode: MutableState<Boolean>,
    gameFinished: MutableState<Boolean>,
    gameWon: MutableState<Boolean>,
    isPaused: MutableState<Boolean>,
    errorCount: MutableState<Int>,
    timerSeconds: MutableState<Int>,
    isLocked: Boolean
) {
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val previousGrid = remember { mutableStateOf<List<List<SudokuCell>>?>(null) }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val titleFontSize = if (screenWidth < 600.dp) 32.sp else 48.sp
    val iconSize = if (screenWidth < 600.dp) 25.dp else 40.dp

    val emptyNumbers = remember { mutableStateOf<List<Int>>(resetEmptyNumbers(difficulty = selectedDifficulty.value)) }
    val errorCountLog by derivedStateOf {
        Log.d("SudokuDebug3", "ErrorCount value: ${errorCount.value}")
        errorCount.value
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Sudoku",
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.weight(1.1f)) {
                Row {
                    Text(
                        text = "${selectedDifficulty.value} ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Hata: ${errorCount.value}/3",
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }
            }

            Box(modifier = Modifier.weight(0.8f),
                contentAlignment = Alignment.BottomCenter
            ) {
                DifficultySelector(
                    difficulty = selectedDifficulty.value,
                    onDifficultySelected = { difficulty ->
                        selectedDifficulty.value = difficulty
                        sudokuGrid.value = generateSudokuGrid(difficulty)
                        errorMessage.value = ""
                        selectedCell = null
                    }
                )
            }

            Box(modifier = Modifier.weight(1.1f), contentAlignment = Alignment.BottomEnd) {
                Row(  modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom) {
                    Text(" ${formatTime(timerSeconds.value)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (isPaused.value) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = "Pause/Resume",
                        modifier = Modifier
                            .size(iconSize * 2 / 3)
                            .clickable {
                                isPaused.value = !isPaused.value
                            },
                        tint = if (isPaused.value) Color.Green else Color.Blue)
                }

            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        if (isPaused.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                AlertDialog(
                    onDismissRequest = { isPaused.value = false },
                    title = { Text("Game Paused") },
                    text = { Text("Your game is paused. Press Resume to continue.") },
                    confirmButton = {
                        Button(onClick = { isPaused.value =false }) {
                            Text("Resume")
                        }
                    }
                )
            }
        } else {


            SudokuGrid(
                grid = sudokuGrid.value,
                errorMessage = errorMessage.value,
                selectedCell = selectedCell,
                onCellClick = { row, col ->
                    selectedCell = Pair(row, col)
                    updateGridHighlight(sudokuGrid, row, col)
                },
                isNotesMode = isNotesMode.value,
                gameFinished = gameFinished
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.Outlined.Edit,
                contentDescription = if (isNotesMode.value) "Not Modu Kapat" else "Not Modu Aç",
                modifier = Modifier
                    .size(iconSize)
                    .clickable { isNotesMode.value = !isNotesMode.value },
                tint = if (isNotesMode.value) Color.Green else Color.Blue
            )
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Reset",
                modifier = Modifier
                    .size(iconSize)
                    .clickable {
                        sudokuGrid.value = generateSudokuGrid(selectedDifficulty.value)
                        errorMessage.value = ""
                        selectedCell = null
                        errorCount.value = 0
                    },
                tint = Color.Blue
            )
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Undo",
                modifier = Modifier
                    .size(iconSize)
                    .clickable {
                        if (previousGrid.value != null) {
                            sudokuGrid.value = previousGrid.value!!
                            previousGrid.value = null
                            errorMessage.value = ""
                        }
                    },
                tint = Color.Blue
            ) }
            NumbersPanel(
                emptyNumbers = emptyNumbers
            ) { number ->
                selectedCell?.let { (row, col) ->
                    previousGrid.value = sudokuGrid.value.map { it.map { cell -> cell.copy() } }

                    if (isNotesMode.value) {
                        updateCell(sudokuGrid, row, col, number, true, errorMessage, emptyNumbers)
                    } else {
                        val isValid = updateCellValue(
                            sudokuGrid,
                            row,
                            col,
                            number,
                            errorMessage,
                            emptyNumbers,
                            isNotesMode = isNotesMode.value,errorCount
                        )

                        if (isValid && checkGameFinished(sudokuGrid.value)) {
                            gameWon.value = true
                        } else if (!isValid)  {
                         //   errorCount.value++
                            Log.d("errorCount3", "Attempt to modify locked or filled cell at (${errorCount.value})")
                            if (errorCount.value >= 3) {
                                gameFinished.value = true
                            }
                        }
                    }
                }
            }
    }
}


/*object SudokuHelper {
    fun isValidMove(board: List<List<Int>>, row: Int, col: Int, value: Int): Boolean {
        // Satır kontrolü
        if (board[row].contains(value)) return false
        // Sütun kontrolü
        if (board.any { it[col] == value }) return false
        // Alt blok kontrolü
        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if (board[r][c] == value) return false
            }
        }
        return true
    }
}*/
fun isValidMove(board: List<List<Int>>, row: Int, col: Int, value: Int): Boolean {
    // Satır kontrolü
    if (board[row].contains(value)) return false
    // Sütun kontrolü
    if (board.any { it[col] == value }) return false
    // Alt blok kontrolü
    val startRow = (row / 3) * 3
    val startCol = (col / 3) * 3
    for (r in startRow until startRow + 3) {
        for (c in startCol until startCol + 3) {
            if (board[r][c] == value) return false
        }
    }
    return true
}

fun generateSudokuGrid(difficulty: String): List<List<SudokuCell>> {
    val emptyCount = when (difficulty) {
        "Easy" -> 30
        "Medium" -> 40
        "Hard" -> 50
        else -> 40
    }

    val board = MutableList(9) { row ->
        MutableList(9) { col -> SudokuCell(row, col, 0) }

    }

    fun fillBoard(row: Int, col: Int): Boolean {
        if (row == 9) return true
        val nextRow = if (col == 8) row + 1 else row
        val nextCol = if (col == 8) 0 else col + 1

        val numbers = (1..9).shuffled()
        for (number in numbers) {
            if (isValidMove(board.map { it.map { it.value } }, row, col, number)) {
                board[row][col] = board[row][col].copy(value = number)
                if (fillBoard(nextRow, nextCol)) return true
                board[row][col] = board[row][col].copy(value = 0) // Geri dön
                Log.d("board", "Attempt to modify locked or filled cell at (${board})")
            }
        }
        return false
    }

    fillBoard(0, 0)

    // Zorluk seviyesine göre hücreleri boş bırak
    val emptyCells = (0 until 81).shuffled().take(emptyCount)
    emptyCells.forEach { index ->
        val row = index / 9
        val col = index % 9
        board[row][col] = board[row][col].copy(value = 0)
    }

    return board
}

fun isInSameBlock(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
    val startRow1 = (row1 / 3) * 3
    val startCol1 = (col1 / 3) * 3
    val startRow2 = (row2 / 3) * 3
    val startCol2 = (col2 / 3) * 3

    // Eğer iki hücrenin başlangıç blok koordinatları aynıysa aynı bloktadırlar
    return startRow1 == startRow2 && startCol1 == startCol2
}

// Fonksiyon: updateGridHighlight
fun updateGridHighlight(
    grid: MutableState<List<List<SudokuCell>>>,
    selectedRow: Int,
    selectedCol: Int
) {
    val selectedValue = grid.value[selectedRow][selectedCol].value

    grid.value = grid.value.map { row ->
        row.map { cell ->
            cell.copy(
                isHighlighted = cell.row == selectedRow ||
                        cell.col == selectedCol ||
                        isInSameBlock(selectedRow, selectedCol, cell.row, cell.col),
                isMatching = selectedValue != 0 && cell.value == selectedValue
            )
        }
    }
}
fun resetEmptyNumbers(difficulty: String): MutableList<Int> {
    val emptyCount = when (difficulty) {
        "Easy" -> 35
        "Medium" -> 43
        "Hard" -> 51
        else -> 30
    }
    return MutableList(emptyCount) { (1..9).random() } // Belirli bir zorluk için rastgele sayılar oluştur

}


fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private operator fun Unit.not(): Boolean {
return false
}



fun checkGameFinished(grid: List<List<SudokuCell>>): Boolean {
    return grid.all { row -> row.all { it.value != 0 } } // Tüm hücreler doluysa oyun bitti
}
// DifficultySelector bileşeni
@Composable
fun DifficultySelector(difficulty: String, onDifficultySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val difficulties = listOf("Easy", "Medium", "Hard")
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val iconSize = if (screenWidth < 600.dp) 30.dp else 50.dp

    Icon(
        Icons.Default.Menu,
        contentDescription = "Diffuculty Selector",

        modifier = Modifier
            .padding(6.dp)
            .size(iconSize * 2 / 3)
            .clickable {
                expanded = true
            },
        tint =  Color.Blue
    )
        DropdownMenu(modifier = Modifier.size(200.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            difficulties.forEach { difficulty ->
                DropdownMenuItem(
                    onClick = {
                        onDifficultySelected(difficulty)
                        expanded = false
                    },
                    text = { Text(text = difficulty)
                         }
                )
            }
        }
}
@Composable
fun NumbersPanel(emptyNumbers: MutableState<List<Int>>, onNumberSelected: (Int) -> Unit) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cellWidth = screenWidth / 9
    val panelFontSize = if (screenWidth < 600.dp) 28.sp else 40.sp
    val distinctNumbers = emptyNumbers.value.distinct().sorted()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        distinctNumbers.forEach { number ->
            Box(
                modifier = Modifier
                    .width(cellWidth - 15.dp)
                    .height(cellWidth * 2 - 6.dp)
                    .clickable {
                        onNumberSelected(number) // Burada tetiklenmeli
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = panelFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue
                )
            }
        }
    }
}

fun updateCellValue(
    sudokuGrid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int,
    errorMessage: MutableState<String>,
    emptyNumbers: MutableState<List<Int>>,
    isNotesMode: Boolean,
    errorCount: MutableState<Int>
): Boolean {
    val grid = sudokuGrid.value
    val cell = grid[row][col]

    // Eğer hücre kilitliyse işlem yapılmaz.
    if (cell.isLocked) {
        errorMessage.value = "This cell is already locked!"
        Log.d("SudokuDebug", "Attempt to modify locked cell at ($row, $col)")
        return false
    }

    // Eğer hücre doluysa işlem yapılmaz ve hata sayısı artırılmaz.
    if (cell.value != 0) {
        errorMessage.value = "This cell is already filled!"
        Log.d("SudokuDebug", "Attempt to modify filled cell at ($row, $col)")
        return false
    }

    // Hareket geçerli mi kontrol et.
    if (isValidMove(grid.map { it.map { c -> c.value } }, row, col, value)) {
        val newGrid = grid.map { it.map { c -> c.copy() }.toMutableList() }.toMutableList()
        newGrid[row][col] = cell.copy(value = value, isLocked = true) // Hücre güncelleniyor ve kilitleniyor.
        sudokuGrid.value = newGrid
        errorMessage.value = ""
        return true
    } else {
        errorMessage.value = "Invalid move!"
        errorCount.value += 1
        Log.d("SudokuDebug", "Invalid move at ($row, $col). Error count: ${errorCount.value}")
        return false
    }
}


@Composable
fun calculateCellSize(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridPadding = 18.dp // Grid'in çevresindeki padding
    val totalGridSize = screenWidth.coerceAtMost(screenHeight) - (2 * gridPadding)
    return totalGridSize / 9 // 9x9 Sudoku için hücre boyutu

}

@Composable
fun SudokuGrid(
    grid: List<List<SudokuCell>>,
    errorMessage: String,
    selectedCell: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    isNotesMode: Boolean,
    gameFinished: MutableState<Boolean>
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val cellSize = calculateCellSize()
    val gridFontSize = if (screenWidth < 600.dp) 16.sp else 24.sp
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

        // Oyun bitti kontrolü
        LaunchedEffect(grid) {
            if (grid.all { row -> row.all { it.value != 0 } }) {
                gameFinished.value = true
            }
        }

        if (errorMessage.isNotEmpty()) {
            ////winAlertDiolog
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = gridFontSize,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
fun updateCell(
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int,
    isNotesMode: Boolean,
    errorMessage: MutableState<String>,
    emptyNumbers: MutableState<List<Int>>
): Boolean {
    return if (isNotesMode) {
        // Not modu: Hücrenin olasılıklarını güncelle
        grid.value = grid.value.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, cell ->
                if (r == row && c == col) {
                    val newPossibilities = cell.possibilities.toMutableSet().apply {
                        if (contains(value)) remove(value) else add(value)
                    }.filter { possibleValue ->
                      isValidMove(grid.value.map { it.map { it.value } }, row, col, possibleValue)
                    }.toMutableSet()
                    cell.copy(possibilities = newPossibilities)
                } else cell
            }
        }
        true
    } else {
        // Normal mod: Hücrenin değerini güncelle
        val isValid =isValidMove(grid.value.map { it.map { it.value } }, row, col, value)
        if (isValid) {
            grid.value = grid.value.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, cell ->
                    if (r == row && c == col) {
                        cell.copy(value = value, isError = false)
                    } else cell
                }
            }
            emptyNumbers.value = emptyNumbers.value.toMutableList().apply {
                remove(value)
            }

            true
        } else {
            errorMessage.value = "Invalid move!"
            grid.value = grid.value.mapIndexed { r, rowList ->
                rowList.mapIndexed { c, cell ->
                    if (r == row && c == col) cell.copy(isError = true) else cell
                }
            }
            false
        }
    }
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    cellSize: Dp,
    isSelected: Boolean,
    onCellClick: () -> Unit
) {
    val backgroundColor = when {
        cell.isError -> Color.Red.copy(alpha = 0.3f) // Yanlış giriş
        isSelected -> Color.Cyan.copy(alpha = 0.3f) // Seçili hücre
        cell.isHighlighted -> Color.LightGray
        cell.isMatching -> Color.Yellow
        else -> Color.White
    }

    val borderColorBlack = Color.Black
    val borderColorGray = Color.Gray
    Box(
        modifier = Modifier
            .size(cellSize)
            .background(backgroundColor)
            .clickable { onCellClick() }
            .drawBehind {
                drawLine(
                    color = if (cell.row % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = if (cell.row % 3 == 0) 4f else 2f
                )
                drawLine(
                    color = if ((cell.row + 1) % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = if ((cell.row + 1) % 3 == 0) 4f else 2f
                )
                drawLine(
                    color = if (cell.col % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = if (cell.col % 3 == 0) 4f else 2f
                )
                drawLine(
                    color = if ((cell.col + 1) % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = if ((cell.col + 1) % 3 == 0) 4f else 2f
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                fontSize = (cellSize.value * 0.5).sp,
                fontWeight = FontWeight.Bold,
                color = if (cell.isError) Color.Red else Color.Black
            )
        } else if (cell.possibilities.isNotEmpty()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                cell.possibilities.sorted().chunked(3).forEach { row ->
                    Row(modifier= Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { possibility ->
                            Text(
                                text = possibility.toString(),
                                fontSize = (cellSize.value * 0.2).sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
    }
// Geçerli hareket kontrolü
fun <T> isValidMove(
    board: List<List<T>>,
    row: Int,
    col: Int,
    value: Int,
    getValue: (T) -> Int
): Boolean {
    // Eğer hücrede hali hazırda bir değer varsa değiştirilemez
    if (getValue(board[row][col]) != 0) return false

    // Satır kontrolü
    if (board[row].any { getValue(it) == value }) return false

    // Sütun kontrolü
    if (board.any { getValue(it[col]) == value }) return false

    // 3x3 blok kontrolü
    val startRow = (row / 3) * 3
    val startCol = (col / 3) * 3
    for (r in startRow until startRow + 3) {
        for (c in startCol until startCol + 3) {
            if (getValue(board[r][c]) == value) return false
        }
    }

    return true
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Sudoku3Theme {
        val sudokuGrid = remember { mutableStateOf(generateSudokuGrid("Easy")) }
        val errorMessage = remember { mutableStateOf("") }
        val selectedDifficulty = remember { mutableStateOf("Easy") }
        val isNotesMode = remember { mutableStateOf(false) }
        val timerSeconds = remember { mutableStateOf(0) }
        val gameFinished = remember { mutableStateOf(false) }
        val gameWon = remember { mutableStateOf(false) }
        val errorCount = remember { mutableStateOf(0) }
        SudokuScreen(
            sudokuGrid = sudokuGrid,
            errorMessage = errorMessage,
            selectedDifficulty = selectedDifficulty,
            isNotesMode = isNotesMode,
                    gameFinished = gameFinished,
            gameWon = gameWon,
            errorCount = errorCount,
            isPaused = remember { mutableStateOf(false)
            },
            timerSeconds = timerSeconds, isLocked = false
        )
    }
}