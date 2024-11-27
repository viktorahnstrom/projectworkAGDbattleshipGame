package com.example.projectworkagd_battleshipgame.ui.components


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Cell


@Composable
fun BoardGrid(
    board: Board,
    onCellClick: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
    ) {
        for (y in 0 until board.size) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                for (x in 0 until board.size) {
                    BoardCell(
                        cell = board.cells[y][x],
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
    cell: Cell,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(2.dp)
            .aspectRatio(1f)
            .background(
                color = when {
                    cell.shipId != null -> Color(0XFF535353)
                    else -> Color.White.copy(alpha = 0.6f)
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onClick)
    )
}