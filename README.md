# 8-puzzle
Programming Assignment 1 for CSDS 291

Solving the 8-puzzle with A* search and beam search.

To test, simply run "java CSDS291PA1 input.txt" in the command line and you will be prompted with certain questions to answer to build the board.

Methods include 
  setState (creation of board)
  
  printState (prints the board)
  
  move (moves the [blank] tile)
  
  randomizeState (makes n random valid moves)
  
  solveAStart (solves the puzzle using A*search with heuristic misplaced tiles and Manhattan distance [see next method])
  misplacedTiles (counts the number of tiles in the incorrect position [excluding blank tile])
  distanceToManhattan (calculates the sum of distances of the tiles from their goal positions)
  
  ... More to come
  
