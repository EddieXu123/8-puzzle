import java.io.*;
import java.util.*;

public class Main {

    /* Instance variables */

    // Variable to check if the solution has been found
    private static boolean found = false;
    // Variable that initially sets the max number of nodes, may be changed with maxNodes(n) command
    private static int maxNodes = 69420;
    // Variable to keep track of the total number of nodes visited in one search
    private static int nodesVisited = 0;
    // Variable to keep track of the number of commands entered to test program
    private static int numCommands = 1;

    /*
     *  Method to check if the current board is solvable
     *  Source: https://math.stackexchange.com/questions/293527/how-to-check-if-a-8-puzzle-is-solvable
     */
    public static boolean isSolvable(Board current) {
        String s = current.getBoard();
        int counter = 0;
        int index = 0;
        // Represent my current board state as an array
        int[] flatArr = new int[9];

        /* Adding the board elements to my array */
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == 'b') {
                flatArr[index++] = 0;
            }
            else if (Character.isDigit(s.charAt(i))) {
                flatArr[index++] = Character.getNumericValue(s.charAt(i));
            }
        }

        /* If there is an inversion, add one to my counter */
        for (int i = 0; i < flatArr.length - 1; i++) {
            if (flatArr[i] > flatArr[i + 1]) {
                counter++;
            }
        }

        // Even inversions mean that the puzzle is solvable
        return counter % 2 == 0;
    }

    /*
     * Method to randomize the board to get a valid start
     * @param int: the number of times I want to make a random move
     * @return Board: Returns a new Board that has been randomized from the initial board (per TA instructions)
     * Note that random moves that are invalid will not take place, so the result will be a board that can be solved
     */
    public static Board randomizeState(int n) {
        // Array of valid Directions
        String[] validDirections = {"up", "down", "right", "left"};
        Board board = new Board();
        board.resetBoard();
        // Set a seed so that the same numbers are generated randomly (as per instructions)
        Random rand = new Random(69);

        for (int i = 0; i < n; i++) {
            int randomNum = rand.nextInt(4);
            // Check if the move is valid to avoid spam of ("Invalid Move") everytime random move is generated
            if (board.validMove(validDirections[randomNum])) {
                board = board.move(validDirections[randomNum]);
            }
        }

        return board;
    }

    /*
     *  Method to solve A-Star using either hamming or manhattan distances as heuristic
     *  @Param String: h1 || h2, the heuristic function being used
     *  @Param Board: The current state of the board when trying to solve the puzzle
     *  @Return Board: The solved Board if the puzzle is solvable, used to backtrack sequence of moves
     *  Sources referred: https://gist.github.com/raymondchua/8064159
     */
    public static Board solveAStar(String heuristic, Board current) {
        if (current.misplacedTiles() == 0) {
            found = true;
            return current;
        }

        String[] validDirections = {"up", "down", "right", "left"};

        // Using the heuristic, set the current board
        current.setEvaluation(current.getCurrentLevel(), heuristic);

        // HashSet and PriorityQueue used in A-Star search, with Evaluation function as priority
        Set<String> visitedNodes = new HashSet<>();
        Queue<Board> pq = new PriorityQueue<>(Comparator.comparingInt(Board::getEvaluation));

        pq.add(current);

        // Start of BFS-like A* search. This will continue until I found a solution,
        // the queue is empty, or until max nodes has been reached
        while(!pq.isEmpty()) {
            Board parent = pq.poll();
            // Add the current state to my set
            visitedNodes.add(parent.getBoard());
            nodesVisited++; // Every board I visit increases the nodes visited by one

            /* The current node of the tree is equal to the goal state */
            if (parent.misplacedTiles() == 0) {
                return parent;
            }

            /* If my maxNodes has been reached */
            if (visitedNodes.size() > maxNodes) {
                break;
            }

            // For all the children of my current Board
            for (String direction : validDirections) {

                // Get the successor by moving the current board
                if (parent.validMove(direction)) {
                    Board child = parent.move(direction);

                    // Avoid loops
                    if (visitedNodes.contains(child.getBoard())) continue;

                    /* Assign the values of the children */
                    int currentLevel = parent.getCurrentLevel();
                    child.setPreviousMove(direction);
                    child.setParent(parent);
                    child.setEvaluation(currentLevel + 1, heuristic);

                    // Add the successor back into priority queue, which automatically sorts it based on evaluation
                    pq.add(child);
                }
            }
        }

        return current;
    }

    /* Backtrack method, per TA suggestion, to avoid infinite loops */
    public static boolean backtrack(String direction, Board board) {
        Map<String, Integer> map = new HashMap<>();
        map.put("up", 1);
        map.put("down", 1);
        map.put("left", 2);
        map.put("right", 2);

        // If the current move isn't the same as the previous move and the two moves aren't opposite
        return !direction.equals(board.getPreviousMove()) &&
                (map.get(direction).equals(map.get(board.getPreviousMove())));
    }

    /*
     *  Helper method a list of the successors of the current board
     *  @Param Board: The current board that I want to get the successors of
     *  @Return List<Board>: The children of the board in a list form
     */
    public static List<Board> getChildren(Board current) {
        String[] directions = {"up", "down", "left", "right"};
        List<Board> children = new ArrayList<>();
        for (String direction : directions) {
            // I want to have both a child that was produced with a valid move,
            // and it can't be the opposite of the parent or else I will have an infinite loop
            if (current.validMove(direction) && !backtrack(direction, current)) {
                nodesVisited++; // For every child I visit, increase the nodes visited

                Board child = current.move(direction);
                child.setEvaluation(current.getCurrentLevel() + 1, "h2");
                child.setParent(current);
                child.setPreviousMove(direction);

                // If one of my children is the solved board, then I can set my found variable to true
                if (child.misplacedTiles() == 0) found = true;

                // Add the child to my list of children
                children.add(child);
            }
        }

        return children;
    }

    /*
     *  Method to solve the 8-puzzle with Local Beam Search
     *  @Param: int: The number of successors considered
     *  @Param Board: The current board
     *  @Return Board: The solved Board if the puzzle is solvable, used to backtrack sequence of moves
     *  Sources: https://www.cs.umd.edu/~nau/cmsc421/chapter04b.pdf
     *  Sources: https://github.com/xiejuncs/Search/blob/master/BeamSearch/BeamSearch.java
     *  Sources: Textbook - Intro to AI 3rd Edition
     */
    public static Board solveBeam(int k, Board current) {
        if (current.misplacedTiles() == 0) {
            found = true;
            return current;
        }

        // Need two priorityQueues, one for the current set of children and one to get next K best
        Queue<Board> pq = new PriorityQueue<>(Comparator.comparingInt(Board::getEvaluation));
        Queue<Board> pq1 = new PriorityQueue<>(Comparator.comparingInt(Board::getEvaluation));

        List<Board> children = getChildren(current);

        // Using streams, add each board to my first queue
        children.stream().forEach(kid -> pq.add(kid));

        while (current.misplacedTiles() != 0) {
            /* If I still have room to add boards to my queue (haven't reached k best yet), add! */
            if (k >= pq.size()) {
                for (Board b : pq) {
                    pq1.add(b);
                }
            }

            // Else get the best K out of all the children I have and can find the top k
            else {
                for (int i = 0; i < k; i++) {
                    pq1.add(pq.poll());
                }
            }

            // Reset the temp queue for next iteration
            pq.clear();

            while (!pq1.isEmpty()) {
                // Similar approach, getting the best of the best
                children = getChildren(pq1.poll());

                // If one of my children were the correct board, then look for that child
                if (found) {
                    for (Board b : children) {
                        if (b.misplacedTiles() == 0) return b;
                    }
                }

                // Otherwise, add my children to my queue and restart the process
                for (Board c : children) {
                    pq.add(c);
                }
            }
        }

        // Will never be reached because the algorithm will break if the board is solved
        return null;
    }

    /* Helper method to print the output of my board after attempting to solve it */
    public static void printOutput(Board current) {
        if (current == null) System.out.println("Unsolved Board");
        int counter = 0;
        String currentBoard = current.getBoard();

        // Trying to see if the current board is solved (The first and second elements will not be equal)
        int[] flatState = new int[9];
        for (int i = 1; i < currentBoard.length(); i++) {
            if (currentBoard.charAt(i) != ' ') {
                flatState[counter++] = Character.getNumericValue(currentBoard.charAt(i));
            }
        }

        // The inversion is odd, meaning that the blank tiles fill the board
        if (flatState[0] == flatState[1]) {
            System.out.println("This board is unsolvable because the inversion number is odd");
        }

        // If the number of nodes I have visited in total is greater than my threshold
        else if (nodesVisited > maxNodes) {
            System.out.println("Number of nodes has been exceeded :(");
        }

        // No errors emerged in solving the puzzle
        else {
            System.out.println("Congrats! You've solved the puzzle! Here's the output information:");
            System.out.println("The solved board state: ");
            current.printState();
            List<String> output = new ArrayList<>();

            /* Like a linked list, iterate up the tree but in reverse order to print out sequence */
            while (current.hasPrev()) {
                output.add(0, current.getPreviousMove());
                current = current.getParent();
            }

            /* If there are moves in my list */
            if (output.size() != 0) {
                // Return the number of moves the puzzle was solved in (with correct grammar)
                if (output.size() == 1) {
                    System.out.println("This Was Solved In 1 Move! Nice!");
                } else {
                    System.out.println("This Was Solved In " + output.size() + " Moves");
                }

                System.out.print("Sequence of Moves: ");

                /* Printing out the sequence of moves */
                for (int i = 0; i < output.size() - 1; i++) {
                    System.out.print(output.get(i).toUpperCase() + ", ");
                }

                // Print out the last line, with a space for sexiness
                System.out.println(output.get(output.size() - 1).toUpperCase());
            }

            // If there are no moves in my output, then the board is already solved
            else {
                System.out.println("The board was already solved! Lucky you!");
            }
        }
    }

    /* Main method to run code, reading command file through command line */
    public static void main(String[] args) {
        try {
            // Read the input scanner
            FileReader file = new FileReader(args[0]);
            Scanner scanner = new Scanner(file);

            // Array of commands that will be run in my input Text file
            String[] command;
            // The board we will be working with
            Board currentState = new Board();


            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // Use delimiters to split each command and their inputs, per TA suggestion
                command = line.split(" ");
                switch(command[0]) {
                    case "randomizeState" -> {
                        System.out.println("Command #" + numCommands++ + " -> Randomize State");
                        System.out.println("RANDOMIZING..." + "\n");
                        int n = Integer.parseInt(command[1]);
                        currentState = randomizeState(n);
                        break;
                    }
                    case "setState" -> {
                        System.out.println("Command #" + numCommands++ + " -> Set State");
                        currentState.setState(line.substring(9));

                        if (!isSolvable(currentState)) {
                            System.out.println("WARNING... THIS STATE IS NOT SOLVABLE!!");
                        }
                        else {
                            System.out.println("GENERATING STATE...");
                            currentState.printState();
                        }
                        break;
                    }
                    case "printState" -> {
                        System.out.println("Command #" + numCommands++ + " -> Print State ");
                        System.out.println("PRINTING STATE...");
                        currentState.printState();
                        break;
                    }
                    case "move" -> {
                        System.out.println("Command #" + numCommands++ + " -> Move");
                        System.out.println("MOVING BLANK TILE: " + command[1].toUpperCase());
                        currentState = currentState.move(command[1]);
                        System.out.println("");
                        break;
                    }
                    case "solve" -> {
                        System.out.println("Command #" + numCommands++ + " -> Solve");
                        // Determine which heuristic I want to use (If I use A*)
                        String heuristic = command[2].equals("h1") ? "HAMMING DISTANCE HEURISTIC" : "MANHATTAN DISTANCE HEURISTIC";
                        // Determine whether I want to solve the puzzle with A* or Beam Search
                        String s = command[1].equals("A-star") ? "A-STAR USING " + heuristic : "LOCAL BEAM SEARCH";
                        System.out.println("SOLVING WITH " + s + "...");
                        found = false;
                        nodesVisited = 0;
                        currentState = command[1].equals("A-star") ?
                                solveAStar(command[2], currentState) : solveBeam(Integer.parseInt(command[2]), currentState);

                        printOutput(currentState);
                        System.out.println("");
                    }
                    case "maxNodes" -> {
                        System.out.println("Command #" + numCommands++ + " -> " + command[0] + " " + command[1]);
                        System.out.println("MAX NODES SET TO: " + command[1] + "\n");
                        maxNodes = Integer.parseInt(command[1]);
                    }
                    default ->
                            System.out.println("\n" + "You've entered an incorrect command, try again!");
                }

            }
        }

        // Catching if file does not exist
        catch (FileNotFoundException e) {
            System.err.println("Please Input a valid file name");
        }

        // Catching if user tries running the program instead of via command line
        catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("PLEASE RUN METHOD USING COMMAND LINE. EX: 'java Main TestFile.txt'");
        }
    }
}