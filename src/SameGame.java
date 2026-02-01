import java.io.*;
import java.util.Random;
import java.util.Scanner;


public class SameGame {

    static final char EMPTY = ' ';
    static final char SELECTED = '*';
    static final char[] SYMBOLS = {EMPTY, '@', '=', '^', '+'};
    static final int MAX_ROW = 10;
    static final int MAX_COL = 26;
    static final String TOP_SCORE_FILE = "top_scores.txt";

    public static void main(String[] args) {
        new SameGame().startGame();
    }
    void startGame() {
        char[][] gameboard = new char[MAX_ROW][MAX_COL];
        randomizeBoard(gameboard);
        printHelp();

        Scanner scanner = new Scanner(System.in);
        int score = 0;
        char[][] selectedBoard;
        while (!isGameOver(gameboard)) {
            printBoard(gameboard);
            System.out.print("Enter your move in the format column-row, e.g. A-5, or press 'h' for help, 'q' to quit: ");
            String input = scanner.nextLine().trim();
            selectedBoard = null;
            switch (input) {
                case "h":
                    printHelp();
                    break;
                case "q":
                    System.out.println("Your score is: " + score);
                    System.out.println("Thank you for playing SameGame!");
                    topscorer(score);
                    return;
                case "r":
                    randomizeBoard(gameboard);
                    score = 0;
                    break;
                case "t":
                    System.out.println("Tips: selected the biggest segment of blocks for you..");
                    selectedBoard = selectBiggestSegment(gameboard);
                default:
                    if (selectedBoard == null) {
                        int dash = input.indexOf('-');
                        if (dash <= 0 || dash >= input.length() - 1) {
                            System.out.println("Invalid format. Use format such as 'A-5'.");
                            break;
                        }

                        char colChar = Character.toUpperCase(input.charAt(0));
                        int row;
                        try {
                            row = Integer.parseInt(input.substring(dash + 1));
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid row number.");
                            break;
                        }

                        if (!isValidSelection(gameboard, row, colChar)) {
                            System.out.println("Invalid selection (need 2+ vertically or horizontally connected).");
                            break;
                        }

                        selectedBoard = copyArray(gameboard);
                        int numSelected = select(selectedBoard, row, colChar);
                        System.out.println("Selected " + numSelected + " blocks.");
                    }

                    printBoard(selectedBoard);
                    System.out.print("Confirm removal? (y/n): ");
                    String ans = scanner.nextLine().trim().toLowerCase();
                    if (!ans.equals("y")) {
                        System.out.println("Cancelled.");
                        break;
                    }

                    int scoreCS = computeScore(selectedBoard);
                    score += scoreCS;
                    System.out.println("Gained " + scoreCS + " points. Total: " + score);

                    gameboard = removeSelected(selectedBoard);
                    break;

            }
        }
        System.out.println("\n\n");
        printBoard(gameboard);

        System.out.println("Game over! Your final score is: " + score);
        topscorer(score);
    }


    void topscorer(int score) {
        final String FILE = "top_scores.txt";
        final int MAX = 5;

        if (score < 0) {
            score = 0;
        }

        String[] names = new String[256];
        int[] scores = new int[256];
        boolean[] isCurrent = new boolean[256];
        int num = 0;

        try (Scanner sc = new Scanner(new File(FILE))) {
            while (sc.hasNextLine() && num < names.length) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;

                String name = parts[0].trim();

                boolean nameOk = (name != null && name.length() >= 1 && name.length() <= 3);
                if (nameOk) {
                    for (int i = 0; i < name.length(); i++) {
                        char ch = name.charAt(i);
                        if (ch < 'A' || ch > 'Z') { nameOk = false; break; }
                    }
                }
                if (!nameOk) continue;

                int scv;
                try {
                    scv = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    continue;
                }

                names[num] = name;
                scores[num] = scv;
                isCurrent[num] = false;
                num++;
            }
        } catch (FileNotFoundException ignore) {} // new file if it does not exist

        if (num < names.length) {
            names[num] = "";
            scores[num] = score;
            isCurrent[num] = true;
            num++;
        }

        boolean swapped;
        do {
            swapped = false;
            for (int i = 0; i + 1 < num; i++) {
                if (scores[i] < scores[i + 1]) {
                    int ts = scores[i];
                    scores[i] = scores[i + 1];
                    scores[i + 1] = ts;

                    String tn = names[i];
                    names[i] = names[i + 1];
                    names[i + 1] = tn;

                    boolean tc = isCurrent[i];
                    isCurrent[i] = isCurrent[i + 1];
                    isCurrent[i + 1] = tc;

                    swapped = true;
                }
            }
        } while (swapped);


        int limit;
        if  (num < MAX) {
            limit = num;
        } else {
            limit = MAX;
        }

        int idxCurrent = -1;
        for (int i = 0; i < limit; i++) {
            if (isCurrent[i]) { idxCurrent = i; break; }
        }
        if (idxCurrent != -1) {
            Scanner in = new Scanner(System.in);
            while (true) {
                System.out.print("You made the top " + MAX + "! Enter your name (1–3 uppercase letters): ");
                String name = in.nextLine().trim();

                boolean ok = (name != null && name.length() >= 1 && name.length() <= 3);
                if (ok) {
                    for (int i = 0; i < name.length(); i++) {
                        char ch = name.charAt(i);
                        if (ch < 'A' || ch > 'Z') { ok = false; break; }
                    }
                }
                if (ok) { names[idxCurrent] = name; break; }
                System.out.println("Invalid name. Use 1–3 uppercase letters (A–Z).");
            }
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(FILE, false))) {
            for (int i = 0; i < limit; i++) {
                String name = names[i];
                boolean ok = (name != null && name.length() >= 1 && name.length() <= 3);
                if (ok) {
                    for (int k = 0; k < name.length(); k++) {
                        char ch = name.charAt(k);
                        if (ch < 'A' || ch > 'Z') { ok = false; break; }
                    }
                }
                if (!ok) name = "NA";
                out.println(name + " " + scores[i]);
            }
        } catch (IOException e) {
            System.out.println("Warning: couldn't write top scores: " + e.getMessage());
        }

        System.out.println("=== Top Scores ===");
        for (int i = 0; i < limit; i++) {
            String name = names[i];
            boolean ok = (name != null && name.length() >= 1 && name.length() <= 3);
            if (ok) {
                for (int k = 0; k < name.length(); k++) {
                    char ch = name.charAt(k);
                    if (ch < 'A' || ch > 'Z') { ok = false; break; }
                }
            }
            if (!ok) name = "NA";
            System.out.println((i + 1) + ". " + name + " " + scores[i]);
        }
    }


    int computeScore(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return 0;

        int n = 0;
        for (int i = 0; i < rowsGB; i++) {
            for (int j = 0; j < colsGB; j++) {
                if (gameboard[i][j] == SELECTED) {
                    n++;
                }
            }
        }

        if (n == 0) return 0;

        int oldColsGB = colsGB;
        char[][] copy = copyArray(gameboard);
        char[][] afterRemoveSelected = removeSelected(copy);

        int newColsGB;
        if (afterRemoveSelected.length == 0) {
            newColsGB = 0;
        }  else {
            newColsGB = afterRemoveSelected[0].length;
        }

        int colsRemoved = oldColsGB - newColsGB;
        return n * (n + 1) + 10 * colsRemoved;
    }


    char[][] selectBiggestSegment(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if  (colsGB == 0) return gameboard;

        int mostCount = 0;
        char[][] mostSelected = null;

        for (int i = 0; i < rowsGB; i++) {
            for (int j = 0; j < colsGB; j++) {
                char block = gameboard[i][j];
                if (block == EMPTY ||  block == SELECTED) continue;

                char colChar = (char)('A' + j);
                if (!isValidSelection(gameboard, i, colChar)) continue;

                char[][] copy = copyArray(gameboard);
                int count = select(copy, i, colChar);
                if (count > mostCount) {
                    mostCount = count;
                    mostSelected = copy;
                }
            }
        }

        if (mostSelected != null) return mostSelected;
        return copyArray(gameboard);
    }


    char[][] copyArray(char[][] src) {
        int rowsGB = src.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = src[0].length;
        }

        char[][] copy = new char[rowsGB][colsGB];
        for (int i = 0; i < rowsGB; i++) {
            for (int j = 0; j < colsGB; j++) {
                copy[i][j] = src[i][j];
            }
        }

        return copy;
    }


    void printHelp() {
        System.out.println("=== SameGame Help ===");
        System.out.println("Goal: remove groups of 2 or more matching blocks (no diagonals).");
        System.out.println();
        System.out.println("Input format:");
        System.out.println("  <Column>-<Row>  e.g.,  A-5");
        System.out.println("  (Column is a letter A.., Row is a number 0..)");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  h  : show this help");
        System.out.println("  q  : quit the game");
        System.out.println("  r  : randomize the board (ensures at least one valid move)");
        System.out.println("  t  : tip/auto-pick the largest segment (preview, then confirm)");
        System.out.println();
        System.out.println("Rules:");
        System.out.println("  - Only 4-neighbor connections count (up, down, left, right).");
        System.out.println("  - Single tiles with no equal neighbor cannot be selected.");
        System.out.println("  - After removal, blocks fall UP; empty columns drop to the LEFT.");
        System.out.println();
        System.out.println("Scoring:");
        System.out.println("  n*(n+1) + 10*(number of columns removed this move)");
        System.out.println();
        System.out.println("Turn flow:");
        System.out.println("  select -> preview -> confirm (y/n) -> score -> remove -> continue");
    }


    void printBoard(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if  (colsGB == 0) {
            System.out.println("Gameboard is empty.");
            return;
        }

        System.out.print(" ");
        for (int i = 0; i < colsGB; i++) {
            System.out.print((char)('A' + i));
        }
        System.out.println();

        for (int i = 0; i < rowsGB; i++) {
            System.out.print(i);

            for (int j = 0; j < colsGB; j++) {
                System.out.print(gameboard[i][j]);
            }

            System.out.println(i);
        }

        System.out.print(" ");
        for (int i = 0; i < colsGB; i++) {
            System.out.print((char)('A' + i));
        }
        System.out.println();
    }


    boolean isValidSelection(char[][] gameboard, int row, char column) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return false;

        char upperChar = Character.toUpperCase(column);
        if (upperChar < 'A' || upperChar > 'Z') return false;

        int colIndex = upperChar - 'A';
        if (row < 0 || row >= rowsGB || colIndex < 0 || colIndex >= colsGB) return false;

        char target = gameboard[row][colIndex];
        if (target == EMPTY || target == SELECTED) return false;
        if (row > 0 && target == gameboard[row - 1][colIndex]) return true; // up
        if (row + 1 < rowsGB && target == gameboard[row + 1][colIndex]) return true; // down
        if (colIndex > 0 && target == gameboard[row][colIndex - 1]) return true; // left
        if (colIndex + 1 < colsGB && target == gameboard[row][colIndex + 1]) return true; // right

        return false;
    }


    int select(char[][] gameboard, int row, char column) {
        char upperChar = Character.toUpperCase(column);
        int colIndex = upperChar - 'A';

        int rowsGB = gameboard.length;
        int colsGB;

        if (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return 0;
        if (colIndex < 0 || colIndex >= colsGB) return 0;
        if (row < 0 || row >= rowsGB) return 0;
        if (!isValidSelection(gameboard, row, column)) return 0;

        char target = gameboard[row][colIndex];
        gameboard[row][colIndex] = SELECTED;

        int count = 1;
        boolean changed = true;
        while (changed) {
            changed = false;

            for (int i = 0; i < rowsGB; i++) {
                for (int j = 0; j < colsGB; j++) {
                    if (target == gameboard[i][j]) {
                        if ((i > 0 && gameboard[i - 1][j] == SELECTED) ||
                            (i + 1 < rowsGB && gameboard[i + 1][j] == SELECTED) ||
                            (j > 0 && gameboard[i][j - 1] == SELECTED) ||
                            (j + 1 < colsGB && gameboard[i][j + 1] == SELECTED))
                        {
                            gameboard[i][j] = SELECTED;
                            count++;
                            changed = true;
                        }
                    }
                }
            }
        }

        return count;
    }


    char[][] removeSelected(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return gameboard;

        char[][] newGB = new char[rowsGB][colsGB];
        for (int i = 0; i < colsGB; i++) {
            int changedCol = 0;

            for (int j = 0; j < rowsGB; j++) {
                if (gameboard[j][i] != SELECTED) {
                    newGB[changedCol][i] = gameboard[j][i];
                    changedCol++;
                }
            }

            while (changedCol < rowsGB) {
                newGB[changedCol][i] = EMPTY;
                changedCol++;
            }
        }

        boolean[] toBeKept = new boolean[colsGB];
        int countCols = 0;
        for (int i = 0; i < colsGB; i++) {
            boolean empty = true;
            for (int j = 0; j < rowsGB; j++) {
                if (newGB[j][i] != EMPTY) {
                    empty = false;
                    break;
                }
            }

            if (!empty) {
                toBeKept[i] = true;
                countCols++;
            }

        }

        char[][] result = new char[rowsGB][countCols];
        int newCols = 0;
        for (int i = 0; i < colsGB; i++) {
            if  (toBeKept[i]) {
                for (int j = 0; j < rowsGB; j++) {
                    result[j][newCols] = newGB[j][i];
                }
                newCols++;
            }
        }

        return result;
    }


    boolean isGameOver(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return true;

        for (int i = 0; i < rowsGB; i++) {
            for (int c = 0; c < colsGB; c++) {
                if (gameboard[i][c] == SELECTED) return false;
            }
        }

        for (int i = 0; i < rowsGB; i++) {
            for (int j = 0; j < colsGB; j++) {
                char block  = gameboard[i][j];

                if (block == EMPTY || block == SELECTED) continue;
                if (isValidSelection(gameboard, i, (char)('A' + j))) return false;
            }
        }

        return true;
    }


    void randomizeBoard(char[][] gameboard) {
        int rowsGB = gameboard.length;
        int colsGB;

        if  (rowsGB == 0) {
            colsGB = 0;
        } else {
            colsGB = gameboard[0].length;
        }

        if (colsGB == 0) return;

        char[] allowed = new char[SYMBOLS.length];
        int m = 0;
        for (int i = 0; i < SYMBOLS.length; i++) {
            char s = SYMBOLS[i];

            if (s != EMPTY && s != SELECTED) {
                allowed[m] = s;
                m++;
            }
        }

        if (m == 0) return;

        Random rand = new Random();
        int n = 0;
        do {
            for (int i = 0; i < rowsGB; i++) {
                for (int j = 0; j < colsGB; j++) {
                    gameboard[i][j] = allowed[rand.nextInt(m)];
                }
            }
            n++;
        } while (isGameOver(gameboard) && n < 10);
    }
}