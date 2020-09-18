import java.io.FileReader;
import java.util.Scanner;
import java.io.FileNotFoundException;
import java.util.*;
import java.io.*;

public class CSDS291PA1 {
  
  /* Instance Variables */
  private static char[][] board = new char[3][3];
  private static int bLocation = 0;
  
  /*
   * Method to build the board (assuming 8-puzzle)
   * @param Scanner: the input file read by the scanner
   */
  public static void buildBoard(Scanner scan) { 
    
    for (int i = 0; i < 3; i++) {
      /* I need to isolate each line of my scanner to build my board */
      String nextLine = scan.nextLine();
      
      for (int j = 0; j < 3; j++) {
        board[i][j] = nextLine.charAt(j);
        /* Get the 0-index position of where the blank tile is for future use */
        if (board[i][j] == 'b') {
          bLocation = 3*i + j;
        }
      }
      
    }
  }
  
  /* Method to print the state of the CURRENT board */
  public static void printState() {
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        /* Only print to new line if I've reached the rightmost column to maintain structure */
        if (j == board[i].length - 1) {
          System.out.println(board[i][j]);
        }
        
        else {
          System.out.print(board[i][j]);
        }
      }
    }
  }
  
  
  /*
   * Helper Method to swap to tiles in the board
   * @param board, int, int, int(1), int(1): 
   *        The board we are currently on, the location of the blank tile, and the location of the tile we swap with
   */
  public static void swap(char[][] board, int x, int y, int x1, int y1) {
    char temp = board[x][y];
    board[x][y] = board[x1][y1];
    board[x1][y1] = temp;
  }
  
  /*
   * Method to move the blank tile.
   * @param String: The direction I want to move the blank tile.
   * Note that the blank swaps with the tile in the given direction
   * ex: If the blank is in the middle and direction is "up", blank swaps with the tile above it
   */
  public static void move(String direction) throws IllegalArgumentException {
    
    /* Avoid unnecessary capitalization issues */
    direction = direction.toLowerCase();
    
    String[] validDirections = {"up", "down", "left", "right"};
    
    /* If my input is not in the list of valid directions, throw an error */
    if (!Arrays.asList(validDirections).contains(direction)) {
      System.err.println("Please enter a valid direction");
      return;
    }
    
    /* Otherwise, I can try moving the blank tile in the given direction */
    try {
      
      /* Getting the row and column number of the blank tile */
      int row = bLocation / 3;
      int col = bLocation % 3;
      
      switch (direction) {
        case "up":
          if (row == 0) throw new IllegalArgumentException(); // Blank is in top row, can't move up
          
          swap(board, row, col, row - 1, col);
          bLocation -= 3; // The blank tile location has changed
          break;
        case "down":
          if (row == 2) throw new IllegalArgumentException(); // Blank is in bottom row, can't move down
          
          swap(board, row, col, row + 1, col);
          bLocation += 3; // The blank tile location has changed
          break;
        case "left":
          if (col == 0) throw new IllegalArgumentException(); // Blank is in leftmost column, can't move left
          
          swap(board, row, col, row, col - 1);
          bLocation--; // The blank tile location has changed
          break;
        case "right":
          if (col == 2) throw new IllegalArgumentException(); // Blank is in rightmost column, can't move right
          
          swap(board, row, col, row, col + 1);
          bLocation++; // The blank tile location has changed
          break;
      }
    }
    
    catch(IllegalArgumentException e) {
      System.err.println("Invalid Move!");
    }
  }
  
  /*
   * Method to randomize the board to get a valid start
   * @param int: the number of times I want to make a random move
   * Note that random moves that are invalid will not take place, so the result will be a board that can be solved
   */
  public static void randomizeState(int n) throws InterruptedException {
    System.out.println("Randomizing...");
    Thread.sleep(1500);
    String[] validMoves = {"up", "down", "left", "right"};
    Random rand = new Random();
    int val = 0;
    
    for (int i = 0; i < n; i++) {
      val = rand.nextInt(4);
      move(validMoves[val]);
    } 
  }
  
  
  /* First Heuristic function calculating the number of tiles not in place (excluding the blank tile) */
  public static int misplacedTiles() {
    
    int counter = 0;
    /* Solved board, which will be compared to the current board */
    char[][] solvedBoard = {
      {'b', '1', '2'},
      {'3', '4', '5'},
      {'6', '7', '8'}
    };
    
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (board[i][j] != solvedBoard[i][j]) {
          counter++;
        }
      }
    }
    
    // We don't want to count b as a misplaced tile
    return board[0][0] == 'b' ? counter : counter - 1; 
  }
  
  
  /*
   * Second Heuristic Function calculating the total distance of tiles from their correct spot
   * Idea: compare the current x,y indices to where the "number" should actually be by using a hashmap
   */ 
  
  public static int distanceFromManhattan() {
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
    
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (board[i][j] == 'b') continue;
        
        /* 
         * The number of moves for board[i][j] to go into its correct place is equal to
         * horizontal + vertical offsets from the current position
         */
        counter += Math.abs(map.get(board[i][j])[0] - i) + Math.abs(map.get(board[i][j])[1] - j);
      }
    }
    
    return counter;
  }
  
  /*
   * Main method
   */
  public static void main(String[] args) throws FileNotFoundException{
    try {
      int counter = 0;
      Set<Integer> set = new HashSet<>();
      FileReader file = new FileReader(args[0]);
      Scanner scan = new Scanner(file);
      buildBoard(scan);
      
      System.out.println("Misplaced tiles: " + misplacedTiles());
      randomizeState(5);
      System.out.println("Misplaced tiles: " + misplacedTiles());
      
      
      System.out.println("Total distance of tiles from solved state: " + distanceFromManhattan());
      
      printState();
    }
    catch(FileNotFoundException e) {
      System.err.println("Input file does not exist!");
    }
    
    catch(InterruptedException c) {
      System.err.println("Sleeping for too long lazyhead");
    }
    
  }
}