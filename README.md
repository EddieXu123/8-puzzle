# 8-puzzle
Programming Assignment 1 for CSDS 291 (Intro To AI)

Solving the 8-puzzle with A* search and beam search.

To test, simply run "java Main TestFile.txt" in the command line. You may also replace the text file with your own desired commands.

# Methods include 
  setState (creation of board)
  
  printState (prints the board)
  
  move (moves the [blank] tile)
  
  randomizeState (makes n random valid moves)
  
  solveAStart (solves the puzzle using A*search with heuristics - Hamming and Manhattan distance [see next method])
  
  misplacedTiles (counts the number of tiles in the incorrect position [excluding blank tile])
  
  manhattanDistance (calculates the sum of distances of the tiles from their goal positions)
   
  solveBeam (solves the puzzle with local beam search)
  
  maxNodes(n) -> Sets the maximum number of nodes to consider per search
