# SameGame: Java Grid-Based Puzzle

This​‍​‌‍​‍‌ project is essentially a command-line based implementation of the classic SameGame puzzle. It largely involves efficient manipulation of 2D arrays, game state management, and algorithm ​‍​‌‍​‍‌optimization.

## Technical Highlights

* **Complex Grid Logic**: Implemented a $10 \times 26$ game board by using 2D arrays, also handled real-time updates for block selection and removal.
* **Advanced Physics & Gravity**: Developed logic for blocks to "fall" upwards and empty columns to shift left, ensuring a dynamic and challenging play area.
* **Intelligent Hint Engine**: Includes a `selectBiggestSegment` method that algorithmically parses the board to suggest the mathematically optimal move.
* **Data Persistence**: Integrated a file-based leaderboard system (`top_scores.txt`) that tracks, sorts, and stores the top 5 player scores.


## Scoring System
The game utilizes a non-linear scoring algorithm to reward larger segment clears:
$$Score = n(n + 1) + 10 \times (\text{columns removed})$$
*(where $n$ is the number of blocks removed in a single turn)*.

## Features
* **Randomized Board Generation**: Ensures a playable state with at least one valid move using a randomized symbol distribution.
* **Input Validation**: Securely parses coordinate-based user input (e.g., `A-5`) with error handling for invalid formats.
* **Interactive UI**: A clear, double-axis coordinate system for easy navigation in the terminal.

## Tech Stack
* **Language**: Java
* **Core Concepts**: 2D Arrays, File I/O, Scanner/PrintWriter, Algorithmic State Management.
