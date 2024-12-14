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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.sudoku3.ui.theme.Sudoku3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val sudokuGrid = remember { mutableStateOf(generateSudokuBoard("Easy")) }
            val errorMessage = remember { mutableStateOf("") }
            val selectedDifficulty = remember { mutableStateOf("Easy") }
            val isNotesMode = remember { mutableStateOf(false) }
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
@Composable
fun SudokuScreen(
    sudokuGrid: MutableState<List<List<SudokuCell>>>,
    errorMessage: MutableState<String>,
    selectedDifficulty: MutableState<String>,
    isNotesMode: MutableState<Boolean>
) {
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Başlık
        Text(
            text = "Sudoku",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Not Modu ve Zorluk Seviyesi Butonları
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { isNotesMode.value = !isNotesMode.value },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isNotesMode.value) Color.Green else Color.Blue
                )
            ) {
                Text(text = if (isNotesMode.value) "Notes Mode: ON" else "Notes Mode: OFF")
            }

            DifficultySelector(
                selectedDifficulty = selectedDifficulty.value,
                onDifficultySelected = { difficulty ->
                    selectedDifficulty.value = difficulty
                    sudokuGrid.value = generateSudokuBoard(difficulty)
                    errorMessage.value = ""
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sudoku Grid
        SudokuGrid(
            grid = sudokuGrid.value,
            errorMessage = errorMessage.value,
            onCellClick = { row, col ->
                selectedCell = Pair(row, col) // Seçilen hücre güncelleniyor
            },
            isNotesMode = isNotesMode.value
        )

        Spacer(modifier = Modifier.height(16.dp))

        // NumbersPanel (Sayı Girişi)
        NumbersPanel(onNumberSelected = { number ->
            selectedCell?.let { (row, col) ->
                if (isNotesMode.value) {
                    updateCellPossibilities(sudokuGrid, row, col, number)
                } else {
                    updateCellValue(sudokuGrid, row, col, number, errorMessage)
                }
            }
        })

        Spacer(modifier = Modifier.height(16.dp))

        // Reset Butonu
        Button(
            onClick = {
                sudokuGrid.value = generateSudokuBoard(selectedDifficulty.value)
                errorMessage.value = ""
                selectedCell = null
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Reset Game")
        }
    }
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

/*@Composable
fun NumbersPanel(
    onNumberSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween // Daha eşit aralık için
    ) {
        (1..9).forEach { number ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f) // Kutuların kare olmasını sağlar
                    .padding(4.dp)
                    .border(2.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                    .clickable { onNumberSelected(number) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}*/
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


@Composable
fun NumberSelectionPanel(onNumberSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        (1..9).forEach { number ->
            Button(onClick = { onNumberSelected(number) }) {
                Text(text = number.toString(), fontSize = 24.sp)
            }
        }
    }
}

fun getSelectedCell(grid: List<List<SudokuCell>>): Pair<Int?, Int?> {
    for ((rowIndex, row) in grid.withIndex()) {
        for ((colIndex, cell) in row.withIndex()) {
            if (cell.isSelected) return Pair(rowIndex, colIndex)
        }
    }
    return Pair(null, null)
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

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(onClick = { expanded = true }) {
            Text("Difficulty: $selectedDifficulty")
        }

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
    isNotesMode: Boolean,
    onCellClick: (Int, Int) -> Unit
) {
    val cellSize = calculateCellSize()

    Column {
        grid.forEach { row ->
            Row {
                row.forEach { cell ->
                    SudokuCellView(
                        cell = cell,
                        cellSize = cellSize,
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
}

@Composable
fun SudokuCellView(
    cell: SudokuCell,
    cellSize: Dp,
    onCellClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(cellSize)
            .border(1.dp, Color.Black)
            .clickable { onCellClick() }, // Hücre tıklanabilir
        contentAlignment = Alignment.Center
    ) {
        if (cell.value != 0) {
            Text(
                text = cell.value.toString(),
                fontSize = (cellSize.value * 0.5).sp,
                fontWeight = FontWeight.Bold
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

        SudokuScreen(
            sudokuGrid = sudokuGrid,
            errorMessage = errorMessage,
            selectedDifficulty = selectedDifficulty,
            isNotesMode = isNotesMode
        )
    }
}