package com.example.sudoku3

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
