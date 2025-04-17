import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import javax.swing.text.DefaultCaret;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.Instant;

public class SudokuUI extends JFrame {
    private final JTextField[][] cells = new JTextField[9][9];
    private final int[][] initialBoard = new int[9][9]; // Store the initial Sudoku grid
    private final SudokuSolver solver = new SudokuSolver();
    private final Color correctColor = new Color(0, 255, 0); // Dark green for correct
    private final Color incorrectColor = new Color(255, 0, 0); // Dark red for incorrect
    private final Color defaultColor = Color.WHITE;  // Default color for cells
    private final Color highlightColor = new Color(255, 229, 180);  // Peach color
    private final String PUZZLE_FILE = "sudoku_puzzles.txt"; // Puzzle file name
    private Instant startTime;
    private boolean gameStarted = false;

    public SudokuUI() {
        setTitle("Sudoku Solver");
        setSize(500, 580);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(3, 3)); // Changed to 3x3 grid for blocks
        Font futuraFont = new Font("Futura", Font.PLAIN, 18);

        // Loop through each 3x3 block
        for (int blockRow = 0; blockRow < 3; blockRow++) {
            for (int blockCol = 0; blockCol < 3; blockCol++) {
                JPanel blockPanel = new JPanel(new GridLayout(3, 3)); // Grid for cells inside the block
                blockPanel.setBorder(new LineBorder(Color.BLACK, 2)); // Border for the block

                // Loop through each cell in the block
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        int row = blockRow * 3 + i;
                        int col = blockCol * 3 + j;

                        JTextField cell = new JTextField();
                        cell.setHorizontalAlignment(JTextField.CENTER);
                        cell.setFont(futuraFont);
                        DefaultCaret caret = new DefaultCaret() {
                            @Override
                            public boolean isVisible() {
                                return false;
                            }
                        };
                        cell.setCaret(caret); // Set the invisible caret
                        cells[row][col] = cell; // Assign cell to the array
                        blockPanel.add(cell); // Add cell to the block

                        // Add focus listener for highlighting
                        cell.addFocusListener(new FocusAdapter() {
                            Color originalColor;

                            @Override
                            public void focusGained(FocusEvent e) {
                                originalColor = cell.getBackground(); // Save the original color
                                cell.setBackground(highlightColor);  // Set to peach on focus
                            }

                            @Override
                            public void focusLost(FocusEvent e) {
                                cell.setBackground(originalColor);  // Restore original color when focus is lost
                            }
                        });

                        cell.addKeyListener(new KeyAdapter() {
                            @Override
                            public void keyTyped(KeyEvent e) {
                                char c = e.getKeyChar();
                                if (!Character.isDigit(c) || c == '0') {
                                    e.consume();  // Ignore invalid input
                                } else {
                                    cell.setText("");
                                }
                                if (!gameStarted) {
                                    startTime = Instant.now();
                                    gameStarted = true;
                                }
                            }
                        });
                    }
                }
                gridPanel.add(blockPanel); // Add the block to the main grid
            }
        }

        // Initialize with a sample Sudoku puzzle
        int[][] samplePuzzle = {
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };
        setInitialPuzzle(samplePuzzle);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // FlowLayout for buttons

        // Modern button style
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Dimension buttonSize = new Dimension(100, 40);

        JButton solveBtn = createModernButton("Solve", buttonFont, buttonSize);
        JButton clearBtn = createModernButton("Clear", buttonFont, buttonSize);
        JButton checkBtn = createModernButton("Check", buttonFont, buttonSize);
        JButton newGameBtn = createModernButton("New Game", buttonFont, buttonSize); // New Game Button

        // Action listeners
        solveBtn.addActionListener(e -> solveSudoku());
        clearBtn.addActionListener(e -> clearBoard());
        checkBtn.addActionListener(e -> checkSolution());
        newGameBtn.addActionListener(e -> fillRandomPuzzle()); // New Game Action

        // Add buttons to panel
        buttonPanel.add(solveBtn);
        buttonPanel.add(clearBtn);
        buttonPanel.add(checkBtn);
        buttonPanel.add(newGameBtn); // Add New Game Button

        add(gridPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    // Method to create modern buttons
    private JButton createModernButton(String text, Font font, Dimension size) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setPreferredSize(size);
        button.setBackground(new Color(59, 89, 182)); // Example: Blue color
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return button;
    }

    private void setInitialPuzzle(int[][] puzzle) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                initialBoard[i][j] = puzzle[i][j];
                if (puzzle[i][j] != 0) {
                    cells[i][j].setText(Integer.toString(puzzle[i][j]));
                    cells[i][j].setEditable(false); // Make initial cells non-editable
                    cells[i][j].setBackground(Color.LIGHT_GRAY); // Indicate initial values
                } else {
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setBackground(defaultColor); // Editable cells
                }
            }
        }
    }

    private void solveSudoku() {
        int[][] board = new int[9][9];
        try {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    String text = cells[i][j].getText().trim();
                    board[i][j] = text.isEmpty() ? 0 : Integer.parseInt(text);
                }
            }

            if (solver.solve(board)) {
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        cells[i][j].setText(Integer.toString(board[i][j]));
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "No solution found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter numbers 1–9 only", "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].isEditable()) {
                    cells[i][j].setText("");
                    cells[i][j].setBackground(defaultColor); // Reset background for editable cells
                }
            }
        }
    }
    private void checkSolution() {
        boolean allCellsFilled = true;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = cells[i][j];
                if (cell.isEditable()) {
                    String text = cell.getText().trim();
                    if (text.isEmpty()) {
                        allCellsFilled = false;
                        break;
                    }
                }
            }
            if (!allCellsFilled) break;
        }

        if (!allCellsFilled) {
            highlightIncorrectCells(); // Only highlight incorrect cells
            return;
        }

        if (isValidSudoku()) {
            Duration timeTaken = Duration.between(startTime, Instant.now());
            long minutes = timeTaken.toMinutes();
            long seconds = timeTaken.minusMinutes(minutes).getSeconds();

            // Create congratulatory frame
            JFrame winFrame = new JFrame("Result");
            winFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only the win frame
            winFrame.setSize(250, 150);
            winFrame.setLocationRelativeTo(this); // Center relative to the main frame

            // Create panel and labels
            JPanel winPanel = new JPanel();
            winPanel.setLayout(new BoxLayout(winPanel, BoxLayout.Y_AXIS)); // Vertical layout
            winPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel winLabel = new JLabel("You Won!");
            winLabel.setFont(new Font("Arial", Font.BOLD, 20));
            winLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel timeLabel = new JLabel(String.format("Time: %02d:%02d", minutes, seconds));
            timeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
            timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            winPanel.add(winLabel);
            winPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Add some space
            winPanel.add(timeLabel);

            winFrame.add(winPanel);
            winFrame.setVisible(true);
        } else {
            highlightIncorrectCells();
            JOptionPane.showMessageDialog(this, "Try Again", "Message", JOptionPane.ERROR_MESSAGE);
        }
    }
     private void highlightIncorrectCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                JTextField cell = cells[i][j];
                if (cell.isEditable()) {
                    String text = cell.getText().trim();
                    if (!text.isEmpty()) {
                        int value = Integer.parseInt(text);
                        boolean isCorrect = false;
                        if (initialBoard[i][j] == 0) {
                            int[][] solvedBoard = new int[9][9];
                            for (int row = 0; row < 9; row++) {
                                for (int col = 0; col < 9; col++) {
                                    solvedBoard[row][col] = initialBoard[row][col];
                                }
                            }
                            SudokuSolver solver = new SudokuSolver();
                            solver.solve(solvedBoard);
                            if (solvedBoard[i][j] == value) {
                                isCorrect = true;
                            }
                        } else {
                            if (initialBoard[i][j] == value) {
                                isCorrect = true;
                            }
                        }
                        Color backgroundColor = isCorrect ? correctColor : incorrectColor;
                        cell.setBackground(backgroundColor);

                        // Reset color after 1 second
                        Timer timer = new Timer(1000, e -> {
                            cell.setBackground(defaultColor);
                            ((Timer) e.getSource()).stop(); // Stop the timer
                        });
                        timer.setRepeats(false); // Ensure it only runs once
                        timer.start();
                    }
                }
            }
        }
    }
      private boolean isValidSudoku() {
        int[][] board = new int[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String text = cells[i][j].getText().trim();
                board[i][j] = Integer.parseInt(text);
            }
        }

        // Check rows, columns, and 3x3 subgrids for validity
        for (int i = 0; i < 9; i++) {
            boolean[] rowCheck = new boolean[9];
            boolean[] colCheck = new boolean[9];
            for (int j = 0; j < 9; j++) {
                if (!isValidPlacement(board, i, j, rowCheck))
                    return false;
                if (!isValidPlacement(board, j, i, colCheck))
                    return false;
            }
        }

        // Check 3x3 subgrids
        for (int block = 0; block < 9; block++) {
            int startRow = (block / 3) * 3;
            int startCol = (block % 3) * 3;
            boolean[] blockCheck = new boolean[9];
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (!isValidPlacement(board, startRow + i, startCol + j, blockCheck))
                        return false;
                }
            }
        }

        return true;
    }

      private boolean isValidPlacement(int[][] board, int row, int col, boolean[] check) {
        int num = board[row][col] - 1; // Adjust to be index-based
        if (check[num]) {
            return false;
        }
        check[num] = true;
        return true;
    }
    private void fillRandomPuzzle() {
        try {
            List<String> puzzles = Files.readAllLines(Paths.get(PUZZLE_FILE));
            if (!puzzles.isEmpty()) {
                String puzzle = puzzles.get(new Random().nextInt(puzzles.size()));
                setPuzzle(puzzle); // Set new puzzle
            } else {
                JOptionPane.showMessageDialog(this, "Could not load puzzles file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Could not load puzzles file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setPuzzle(String puzzle) {
        clearBoard();
        gameStarted = false; // Reset game start
        if (puzzle != null && puzzle.length() == 81) {
            for (int i = 0; i < 81; i++) {
                int row = i / 9;
                int col = i % 9;
                char ch = puzzle.charAt(i);
                if (ch != '0') {
                    cells[row][col].setText(String.valueOf(ch));
                    cells[row][col].setEditable(false);
                    cells[row][col].setBackground(Color.LIGHT_GRAY);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                    cells[row][col].setBackground(defaultColor);
                }
                initialBoard[row][col] = (ch != '0') ? Character.getNumericValue(ch) : 0;
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid puzzle format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
