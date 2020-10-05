import java.util.*;

public class Board {
    /* Instance Variables */

    // Current state of board represented as 1D array, as suggested by TA
    private final char[] board;
    // Evaluation function value, the "f" in f = g + h
    private int evaluation = 0;
    // The parent of my current Board, used to backtrack to print out answer
    private Board parent;
    // The current Depth of my node of my tree, or the "g"
    private int currentLevel = 0;
    // The previous Move I took to get to my current Board, used to backtrack to print out sequence of moves
    private String previousMove;

    /* Constructor to initialize a new Board with 8 numerical tiles and blank tile*/
    public Board() {
        board = new char[9];
    }

    /* Get the current board's parent */
    public Board getParent() {
        return parent;
    }

    /* Get the currentLevel + heuristic */
    public int getEvaluation() {
        return evaluation;
    }

    /* Get current level */
    public int getCurrentLevel() {
        return currentLevel;
    }

    /* Get last move */
    public String getPreviousMove() {
        return previousMove == null ? "" : previousMove;
    }

    /* Method to check if a board is a root or was created by a move */
    public boolean hasPrev() {
        return getPreviousMove().length() != 0;
    }

    /* Method to clone the current state of my board */
    public Board cloneBoard() {
        Board output = new Board();
        output.setState(getBoard());
        return output;
    }

    /* Method to reset the board to the initial state */
    public void resetBoard() {
        setState("b12 345 678");
    }

    /* Return the state of the board as a String in 1D array form */
    public String getBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < board.length; i++) {

            // If I am at a new row, add a space
            if (i % 3 == 0) {
                sb.append(" ");
            }

            sb.append(board[i]);
        }

        // Delete the initial space added to my sequence
        sb.deleteCharAt(0);
        return sb.toString();
    }

    /* Set the previous move for when I change states */
    public void setPreviousMove(String previousMove) {
        this.previousMove = previousMove;
    }

    /* Set parent for when I create children */
    public void setParent(Board parent) {
        this.parent = parent;
    }

    /* Method to set the Evaluation value */
    public void setEvaluation(int level, String heuristic) {
        currentLevel = level;
        evaluation = currentLevel + getHeuristic(heuristic);
    }

    /* Gets either the hamming distance or the manhattan distance */
    public int getHeuristic(String heuristic) {
        return heuristic.equals("h1") ? misplacedTiles() : manhattanDistance();
    }

    /* Set the state of a board for when I initialize Boards */
    public void setState(String sequence) {
        int counter = 0;
        for (int i = 0; i < sequence.length(); i++) {
            // If I have a space (to make the design prettier), ignore it
            if (sequence.charAt(i) == ' ') continue;
            board[counter++] = sequence.charAt(i);
        }
    }

    /* Prints the current state of my board */
    public void printState() {
        int counter = 0;
        for (int i = 0; i < 3; i++) {
            if (i != 0) { // Looks sexy in console
                /* I've reached the end of a line */
                System.out.println();
            }
            for (int j = 0; j < 3; j++) {
                /* Print out the current value and increment counter */
                System.out.print(board[counter++]);
            }
        }

        /* Looks sexy if I add two new lines */
        System.out.println("\n");
    }

    /* Method to check if a given move is valid or not */
    public boolean validMove(String direction) {
        /* Avoid capitalization issues */
        direction = direction.toLowerCase();
        /* Set of valid moves */
        String[] validDirections = {"up", "down", "left", "right"};

        /* If a direction is not in my valid moves, return false immediately */
        if (!Arrays.asList(validDirections).contains(direction)) return false;

        /* Get the current row and column locations of my blank tile */
        int row = getBLocation() / 3;
        int col = getBLocation() % 3;

        /* There are four valid cases */
        switch (direction) {
            case "up" -> {
                // If the direction is "up" and my blank tile is in the top row, return false;
                if (row == 0) return false;
            }
            case "down" -> {
                // If the direction is "down" and my blank tile is in the bottom row, return false;
                if (row == 2) return false;
            }
            case "left" -> {
                // If the direction is "left" and my blank tile is in the leftmost column, return false;
                if (col == 0) return false;
            }
            case "right" -> {
                // If the direction is "right" and my blank tile is in the rightmost column, return false;
                if (col == 2) return false;
            }
        }

        // Else this move is valid
        return true;
    }

    /*
     *  Method to move a tile in one of the four directions
     *  @Param String: The direction that the user wishes to move the blank tile to
     *  @Return Board: A new board (representing the child) with the move made
     */
    public Board move(String direction) {
        /* If this move is invalid, don't do anything */
        if (!validMove(direction)) {
            System.out.println("Invalid Move");
            return null;
        }

        /* I want to create a new board to represent the current Boards child (after making one move) */
        Board child = cloneBoard();
        int bLoc = getBLocation();

        /* There are four valid cases */
        switch (direction) {
            case "up" ->
                    // Swap the blank tile with the one above it (minus 3 b/c it's 3 spaces to the left in a 1D array)
                    swap(child, bLoc, bLoc - 3);

            case "down" ->
                    // Swap the blank tile with the one below it (plus 3 b/c it's 3 spaces to the right in a 1D array)
                    swap(child, bLoc, bLoc + 3);

            case "left" ->
                    // Swap the blank tile with the one to the left (minus 1 because it's the column to the left)
                    swap(child, bLoc, bLoc - 1);

            case "right" ->
                    // Swap the blank tile with the one to the right (plus 1 because it's the column to the right)
                    swap(child, bLoc, bLoc + 1);
        }

        return child;
    }

    /* Helper method to swap the blank tile with the neighbor tile in the direction of "move" */
    public void swap(Board current, int index1, int index2) {
        char temp = current.board[index1];
        current.board[index1] = current.board[index2];
        current.board[index2] = temp;
    }

    /* Method to get the location of the blank tile */
    private int getBLocation() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                // The current index of the blank tile is the row of the 2D array times three, plus the column
                if (board[3*i + j] == 'b') {
                    return 3*i + j;
                }
            }
        }

        // Will never be reached or if blank tile doesn't exist initially
        return -1;
    }

    /* Method to calculate the hamming distance of the board, accomplished by comparing with solved board */
    public int misplacedTiles() {
        int counter = 0;
        /* Solved board, which will be compared to the current board */
        char[] solved = {'b', '1', '2', '3', '4', '5', '6', '7', '8'};

        for (int i = 0; i < board.length; i++) {
            if (solved[i] != board[i]) {
                counter++;
            }
        }

        // We don't want to count b as a misplaced tile
        return board[0] == 'b' ? counter : counter - 1;
    }

    /* Method to get the Manhattan Distance, done by creating a map */
    public int manhattanDistance() {
        int counter = 0;
        /* Creating the map */
        Map<Character, Integer[]> map = new HashMap<>();
        map.put('1', new Integer[]{0, 1});
        map.put('2', new Integer[]{0, 2});
        map.put('3', new Integer[]{1, 0});
        map.put('4', new Integer[]{1, 1});
        map.put('5', new Integer[]{1, 2});
        map.put('6', new Integer[]{2, 0});
        map.put('7', new Integer[]{2, 1});
        map.put('8', new Integer[]{2, 2});

        /*
         * The number of moves for board[i][j] to go into its correct place is equal to
         * horizontal + vertical offsets from the current position
         */
        for (int i = 0; i < 9; i++) {
            if (board[i] == 'b') continue;
            counter += Math.abs(map.get(board[i])[0] - i / 3) + Math.abs(map.get(board[i])[1] - i % 3);
        }

        return counter;
    }
}