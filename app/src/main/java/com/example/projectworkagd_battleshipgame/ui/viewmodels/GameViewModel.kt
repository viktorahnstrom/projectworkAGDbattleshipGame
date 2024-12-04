package com.example.projectworkagd_battleshipgame.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectworkagd_battleshipgame.data.models.Board
import com.example.projectworkagd_battleshipgame.data.models.Game
import com.example.projectworkagd_battleshipgame.data.models.GameState
import com.example.projectworkagd_battleshipgame.data.models.GameStatus
import com.example.projectworkagd_battleshipgame.data.remote.FirebaseService
import com.example.projectworkagd_battleshipgame.data.repositories.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val playerId: String,
    private val gameId: String,
    private val gameRepository: GameRepository = GameRepository(FirebaseService()),
    private val firebaseService: FirebaseService = FirebaseService()
) : ViewModel() {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _board = MutableStateFlow(Board())
    val board: StateFlow<Board> = _board.asStateFlow()

    private val _opponentBoard = MutableStateFlow(Board())
    val opponentBoard: StateFlow<Board> = _opponentBoard.asStateFlow()

    init {
        observeGame()
    }

    fun getCurrentPlayerId(): String = playerId

    private fun observeGame() {
        firebaseService.observeGame(gameId) { game ->
            Log.d("GameViewModel", "Game update received: ${game.status}, currentTurn: ${game.currentTurn}")

            _gameState.value = GameState(
                isLoading = false,
                status = game.status,
                currentPlayerId = game.currentTurn,
                player1Id = game.player1Id,
                player2Id = game.player2Id,
                player1Ready = game.player1Ready,
                player2Ready = game.player2Ready,
                error = null,
                winner = game.winner
            )

            if (playerId == game.player1Id) {
                game.board1String?.let { boardString ->
                    _board.value = convertStringToBoard(boardString)
                }
            } else {
                game.board2String?.let { boardString ->
                    _board.value = convertStringToBoard(boardString)
                }
            }

            if (playerId == game.player1Id) {
                game.board2String?.let { boardString ->
                    _opponentBoard.value = convertStringToBoardHidingShips(boardString)
                }
            } else {
                game.board1String?.let { boardString ->
                    _opponentBoard.value = convertStringToBoardHidingShips(boardString)
                }
            }

            if (game.board1String != null && game.board2String != null && game.status == GameStatus.IN_PROGRESS) {
                checkWinCondition(game)
            }
        }
    }

    private fun convertStringToBoard(boardString: String): Board {
        val newBoard = Board()
        val rows = boardString.split(",")
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                newBoard.cells[y][x].state = when (cell) {
                    'E' -> Board.CellState.EMPTY
                    'S' -> Board.CellState.SHIP
                    'H' -> Board.CellState.HIT
                    'M' -> Board.CellState.MISS
                    else -> Board.CellState.EMPTY
                }
            }
        }
        return newBoard
    }

    private fun convertStringToBoardHidingShips(boardString: String): Board {
        val newBoard = Board()
        val rows = boardString.split(",")
        rows.forEachIndexed { y, row ->
            row.forEachIndexed { x, cell ->
                newBoard.cells[y][x].state = when (cell) {
                    'S' -> Board.CellState.EMPTY
                    'H' -> Board.CellState.HIT
                    'M' -> Board.CellState.MISS
                    else -> Board.CellState.EMPTY
                }
            }
        }
        return newBoard
    }

    private fun checkWinCondition(game: Game) {
        if (game.status != GameStatus.IN_PROGRESS) {
            return
        }

        val board1Ships = game.board1String?.count { it == 'S' } ?: 0
        val board2Ships = game.board2String?.count { it == 'S' } ?: 0

        Log.d("GameViewModel", "Checking win condition - Board1 ships: $board1Ships, Board2 ships: $board2Ships")

        when {
            board1Ships == 0 -> handleGameOver(game.player2Id)
            board2Ships == 0 -> handleGameOver(game.player1Id)
        }
    }

    private fun handleGameOver(winnerId: String) {
        Log.d("GameViewModel", "Game Over - Winner: $winnerId")
        if (_gameState.value.status != GameStatus.FINISHED) {
            firebaseService.handleGameOver(gameId, winnerId)
        }
    }

    fun markPlayerReady(gameId: String, board: List<List<Board.Cell>>) {
        val playerId = this.playerId
        viewModelScope.launch {
            try {
                gameRepository.markPlayerReady(gameId, playerId, board)
                Log.d("GameViewModel", "Current game state: ${_gameState.value}")
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error marking player ready", e)
            }
        }
    }

    fun observeGameReadiness(onBothReady: () -> Unit) {
        firebaseService.observeGameReadiness(gameId, onBothReady)
    }

    fun makeMove(x: Int, y: Int) {
        if (gameState.value.currentPlayerId != playerId) {
            Log.d("GameViewModel", "Not your turn")
            return
        }

        val targetBoard = opponentBoard.value
        if (targetBoard.cells[y][x].state == Board.CellState.HIT ||
            targetBoard.cells[y][x].state == Board.CellState.MISS) {
            Log.d("GameViewModel", "Cell already hit")
            return
        }

        Log.d("GameViewModel", "Making move at ($x, $y)")
        viewModelScope.launch {
            try {
                gameRepository.makeMove(gameId, x, y, playerId)
            } catch (e: Exception) {
                Log.e("GameViewModel", "Error making move", e)
            }
        }
    }
}