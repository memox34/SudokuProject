package com.example.sudoku3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.concurrent.fixedRateTimer

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sudokuGrid = remember { mutableStateOf(generateSudokuBoard("Easy")) }
            val errorMessage = remember { mutableStateOf("") }
            val selectedDifficulty = remember { mutableStateOf("Easy") }
            val isNotesMode = remember { mutableStateOf(false) }
            val timerSeconds = remember { mutableStateOf(0) }
            val gameFinished = remember { mutableStateOf(false) }
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

                        SudokuScreen(
                            sudokuGrid = sudokuGrid,
                            errorMessage = errorMessage,
                            selectedDifficulty = selectedDifficulty,
                            isNotesMode = isNotesMode

                        )
                }
            }
        }
    }
}
data class SudokuCell(
    val row: Int,
    val col: Int,
    var value: Int = 0, // Hücredeki kesin sayı (0 ise boş)
    var possibilities: MutableSet<Int> = (1..9).toMutableSet(), // Olası sayılar
    var isNoteMode: Boolean = false, // Not modu açık/kapalı
    var isSelected: Boolean = false,
    var isError:Boolean = false
)
//data class SudokuCell(val row: Int, val col: Int, var value: Int = 0)


@Composable
fun SudokuScreen(
    sudokuGrid: MutableState<List<List<SudokuCell>>>,
    errorMessage: MutableState<String>,
    selectedDifficulty: MutableState<String>,
    isNotesMode: MutableState<Boolean>
) {
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val previousGrid = remember { mutableStateOf<List<List<SudokuCell>>?>(null) }
    val timerSeconds = remember { mutableStateOf(0) }
    val gameFinished = remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val titleFontSize = if (screenWidth < 600.dp) 32.sp else 48.sp
    val iconSize = if (screenWidth < 600.dp) 30.dp else 50.dp

    // Timer Coroutine
    LaunchedEffect(Unit) {
        while (!gameFinished.value) {
            delay(1000L)
            timerSeconds.value++
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        // Başlık
        Text(
            text = "Sudoku",
            fontSize = titleFontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Zorluk Seçim ve Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DifficultySelector(
                selectedDifficulty = selectedDifficulty.value,
                onDifficultySelected = { difficulty ->
                    selectedDifficulty.value = difficulty
                    sudokuGrid.value = generateSudokuBoard(difficulty)
                    errorMessage.value = ""
                    selectedCell = null
                }
            )

            Text("Time: ${formatTime(timerSeconds.value)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sudoku Grid
        SudokuGrid(
            grid = sudokuGrid.value,
            errorMessage = errorMessage.value,
            selectedCell = selectedCell,
            onCellClick = { row, col ->
                selectedCell = Pair(row, col)
            },
            isNotesMode = isNotesMode.value,
            gameFinished = gameFinished
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icon Row
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
                        sudokuGrid.value = generateSudokuBoard(selectedDifficulty.value)
                        timerSeconds.value = 0
                        gameFinished.value = false
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
                        }
                    },
                tint = Color.Blue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // NumbersPanel
        NumbersPanel { number ->
            selectedCell?.let { (row, col) ->
                previousGrid.value = sudokuGrid.value.map { it.map { cell -> cell.copy() } } // Önceki durumu kaydet
                if (isNotesMode.value) {
                    updateCellPossibilities(sudokuGrid, row, col, number)
                } else {
                    val isValid = updateCellValue(sudokuGrid, row, col, number, errorMessage)
                    if (!isValid) {
                        sudokuGrid.value = sudokuGrid.value.mapIndexed { r, rowList ->
                            rowList.mapIndexed { c, cell ->
                                if (r == row && c == col) cell.copy(isError = true) else cell
                            }
                        }
                    }
                }
            }
        }

        if (gameFinished.value) {
            Text(
                "Congratulations! You've completed the puzzle!",
                color = Color.Green,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

private operator fun Unit.not(): Boolean {
return false
}

fun selectCell(
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int
) {
    grid.value = grid.value.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            cell.copy(isSelected = (r == row && c == col)) // Sadece tıklanan hücre seçili olur
        }
    }
}

@Composable
fun NumbersPanel(onNumberSelected: (Int) -> Unit) {
    val numbers = (1..9).toList()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp // Ekran genişliği
    val cellWidth = screenWidth / numbers.size // Her kutunun eşit genişliği
    val panelFontSize = if (screenWidth < 600.dp)36.sp else 48.sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        numbers.forEach { number ->
            Box(
                modifier = Modifier
                    .width(cellWidth - 12.dp) // Genişlik ekranın bir parçasına eşit
                    .height(cellWidth * 2 - 6.dp) // Yükseklik sabit
                    .border(2.dp, Color.Transparent, RoundedCornerShape(4.dp))
                    .clickable { onNumberSelected(number) },
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
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int,
    errorMessage: MutableState<String>
) {
    // Hareketin geçerli olup olmadığını kontrol et
    if (!isValidMove(grid.value, row, col, value) { it.value }) {
        errorMessage.value = "Invalid Move"
        return
    }

    // Hücreye sayı yaz ve aynı 3x3 grid içerisindeki possibilities listesinden kaldır
    grid.value = grid.value.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            if (r == row && c == col) {
                // Hücreyi güncelle ve olasılıkları temizle
                cell.copy(value = value, possibilities = mutableSetOf())
            } else if (isInSameBlock(row, col, r, c) && cell.value == 0) {
                // Aynı 3x3 grid içindeki diğer hücrelerden bu değeri kaldır
                cell.copy(possibilities = cell.possibilities.toMutableSet().apply {
                    remove(value)
                })
            } else cell
        }
    }

    // Hata mesajını temizle
    errorMessage.value = ""
}

fun isInSameBlock(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
    return (row1 / 3 == row2 / 3) && (col1 / 3 == col2 / 3)
}


// DifficultySelector bileşeni
@Composable
fun DifficultySelector(selectedDifficulty: String, onDifficultySelected: (String) -> Unit) {
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
            .size(iconSize)
            .clickable {
                expanded = true
            },
        tint =  Color.Blue
    )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            difficulties.forEach { difficulty ->
                DropdownMenuItem(
                    onClick = {
                        onDifficultySelected(difficulty)
                        expanded = false
                    },
                    text = { Text(text = difficulty) }
                )
            }
        }
}

fun updateCellPossibilities(
    grid: MutableState<List<List<SudokuCell>>>,
    row: Int,
    col: Int,
    value: Int
) {
    grid.value = grid.value.mapIndexed { r, rowList ->
        rowList.mapIndexed { c, cell ->
            if (r == row && c == col) {
                if (cell.possibilities.contains(value)) {
                    // Sayıyı olasılıklardan kaldır
                    cell.copy(possibilities = cell.possibilities.toMutableSet().apply {
                        remove(value)
                    })
                } else {
                    // Sayıyı olasılıklara ekle
                    cell.copy(possibilities = cell.possibilities.toMutableSet().apply {
                        add(value)
                    })
                }
            } else cell
        }
    }
}

@Composable
fun calculateCellSize(): Dp {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val gridPadding = 16.dp // Grid'in çevresindeki padding
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

        // Oyun bitti kontrolü
        LaunchedEffect(grid) {
            if (grid.all { row -> row.all { it.value != 0 } }) {
                gameFinished.value = true
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
        else -> Color.White
    }

    val borderColorBlack = Color.Black
    val borderColorGray = Color.Gray
    Box(
        modifier = Modifier
            .size(cellSize)
            // .border(1.dp, Color.Black)
            .background(backgroundColor)
            .clickable { onCellClick() }
            .drawBehind {
                drawLine(
                    color = if (cell.row % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = if (cell.row % 3 == 0) 4f else 2f
                )
                // Alt çizgi
                drawLine(
                    color = if ((cell.row + 1) % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = if ((cell.row + 1) % 3 == 0) 4f else 2f
                )
                // Sol çizgi
                drawLine(
                    color = if (cell.col % 3 == 0) borderColorBlack else borderColorGray,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = if (cell.col % 3 == 0) 4f else 2f
                )
                // Sağ çizgi
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
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center) {
                cell.possibilities.chunked(3).forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly) {
                        row.forEach { possibility ->
                            Text(
                                text = possibility.toString(),
                                fontSize = (cellSize.value * 0.20).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Sudoku tahtası oluşturma fonksiyonu
fun generateSudokuBoard(difficulty: String): List<List<SudokuCell>> {
    val emptyCount = when (difficulty) {
        "Easy" -> 30 // Kolay seviyede 30 boş hücre
        "Medium" -> 40 // Orta seviyede 40 boş hücre
        "Hard" -> 50 // Zor seviyede 50 boş hücre
        else -> 40 // Varsayılan orta seviye
    }

    val board = MutableList(9) { row ->
        MutableList(9) { col -> SudokuCell(row, col, 0) }
    }

    // Sudoku doldurma algoritması
    fun fillBoard(row: Int, col: Int): Boolean {
        if (row == 9) return true
        val nextRow = if (col == 8) row + 1 else row
        val nextCol = if (col == 8) 0 else col + 1

        val numbers = (1..9).shuffled()
        for (number in numbers) {
            if (isValidMove(board, row, col, number) { it.value }) {
                board[row][col] = board[row][col].copy(value = number)
                if (fillBoard(nextRow, nextCol)) return true
                board[row][col] = board[row][col].copy(value = 0) // Geri dön
            }
        }
        return false
    }

    // Tahtayı doldur
    fillBoard(0, 0)

    // Hücreleri rastgele boşalt (zorluk seviyesine göre)
    val emptyCells = (0 until 81).shuffled().take(emptyCount)
    emptyCells.forEach { index ->
        val row = index / 9
        val col = index % 9
        board[row][col] = board[row][col].copy(value = 0)
    }

    return board
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
        val sudokuGrid = remember { mutableStateOf(generateSudokuBoard("Easy")) }
        val errorMessage = remember { mutableStateOf("") }
        val selectedDifficulty = remember { mutableStateOf("Easy") }
        val isNotesMode = remember { mutableStateOf(false) }
        val timerSeconds = remember { mutableStateOf(0) }
        val gameFinished = remember { mutableStateOf(false) }
        SudokuScreen(
            sudokuGrid = sudokuGrid,
            errorMessage = errorMessage,
            selectedDifficulty = selectedDifficulty,
            isNotesMode = isNotesMode

        )
    }
}