public class SudokuSolver {
    private static final int SIZE = 9;

    public boolean solve(int[][] board) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= SIZE; num++) {
                        if (isValid(board, row, col, num)) {
                            board[row][col] = num;
                            if (solve(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num || board[i][col] == num ||
                (row - row % 3 + i / 3 >= 0 && row - row % 3 + i / 3 < SIZE &&
                 col - col % 3 + i % 3 >= 0 && col - col % 3 + i % 3 < SIZE &&
                 board[row - row % 3 + i / 3][col - col % 3 + i % 3] == num)) {
                return false;
            }
        }
        return true;
    }
}
