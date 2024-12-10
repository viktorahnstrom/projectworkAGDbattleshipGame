package com.example.projectworkagd_battleshipgame.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Board

@Composable
fun BoardGrid(
    board: Board,
    onCellClick: (Int, Int) -> Unit,
    selectedCell: Pair<Int, Int>? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        board.cells.forEachIndexed { y, row ->
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                row.forEachIndexed { x, cell ->
                    val isSelected = selectedCell?.let { it.first == x && it.second == y } ?: false
                    BoardCell(
                        cellItem = cell,
                        isSelected = isSelected,
                        onClick = { onCellClick(x, y) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardCell(
    cellItem: Board.Cell,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> Color(0x4400FF00)
        cellItem.state == Board.CellState.HIT -> Color.Red
        cellItem.state == Board.CellState.MISS -> Color.Blue
        cellItem.state == Board.CellState.SHIP -> Color(0xFF2B2B2B)
        else -> Color.White
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(1.dp)
            .background(backgroundColor)
            .clickable(onClick = onClick)
    )
}