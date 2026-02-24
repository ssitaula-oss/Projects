import java.util.*;

/**
 * @author siddhartha sitaula
 *
 * project :HexMineManager
 *this project takes the input inthe form of radius and
 * num mines in order to give the hexagonal shaped figure
 *
 *
 * Symbols used in toString():
 *   c : covered cell
 *   F : flagged cell
 *   M : uncovered mine
 *   . : uncovered cell with no neighboring mines
 *   q is diagonal axis direction
 *   r is row like axis
 *   <number n> : uncovered cell with n neighboring mines
 **/
public class HexMineManager {

    private static class Coord {
        final int q;
        final int r;

        Coord(int q, int r) {
            this.q = q;
            this.r = r;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Coord)) return false;
            Coord other = (Coord) o;
            return q == other.q && r == other.r;
        }

        @Override
        public int hashCode() {
            return Objects.hash(q, r);
        }
    }


    private static class Cell {
        boolean hasMine;
        boolean revealed;
        boolean flagged;
        int neighborMines;
    }

    private static final int[][] DIRECTIONS = {
            { 1,  0},
            { 1, -1},
            { 0, -1},
            {-1,  0},
            {-1,  1},
            { 0,  1}
    };

    private final int radius;
    private final int numMines;
    private final Map<Coord, Cell> cells = new HashMap<>();
    private final List<Coord> allCoords = new ArrayList<>();
    private boolean gameOver;

    /**
     * Creates a hexagonal board of given
     * radius with the give number mines
     * Radius r means hex distance
     */
    public HexMineManager(int radius, int numMines, long seed) {
        if (radius <= 0) {
            throw new IllegalArgumentException("radius must be positive");
        }

        this.radius = radius;

        for (int r = -radius + 1; r <= radius - 1; r++) {
            int qMin = Math.max(-radius + 1, -r - (radius - 1));
            int qMax = Math.min(radius - 1, -r + (radius - 1));
            for (int q = qMin; q <= qMax; q++) {
                Coord c = new Coord(q, r);
                cells.put(c, new Cell());
                allCoords.add(c);
            }
        }

        int maxCells = allCoords.size();
        if (numMines < 0 || numMines > maxCells) {
            throw new IllegalArgumentException(
                    "numMines must be in [0, " + maxCells + "]");
        }
        this.numMines = numMines;

        placeMines(seed);
        computeNeighborCounts();
        gameOver = false;
    }

    public HexMineManager(int radius, int numMines) {
        this(radius, numMines, 123456789L);
    }


    public int getRadius() {
        return radius;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    /** it returns true if it is a valid coordinate*/
    public boolean inBounds(int row, int col) {
        Coord c = new Coord(col, row); // (row,col) -> (r,q) mapping
        return cells.containsKey(c);
    }

    /**it tests only if the mines is there or not */
    public boolean hasMineAt(int row, int col) {
        Coord c = new Coord(col, row);
        Cell cell = cells.get(c);
        if (cell == null) {
            throw new IndexOutOfBoundsException("Not in hex region:" +
                    " (" + row + "," + col + ")");
        }
        return cell.hasMine;
    }

    /** it tests only for neihbor count */
    public int getNeighborCountAt(int row, int col) {
        Coord c = new Coord(col, row);
        Cell cell = cells.get(c);
        if (cell == null) {
            throw new IndexOutOfBoundsException
                    ("Not in hex region: (" + row + "," + col + ")");
        }
        return cell.neighborMines;
    }

    public boolean isRevealedAt(int row, int col) {
        Coord c = new Coord(col, row);
        Cell cell = cells.get(c);
        if (cell == null) return false;
        return cell.revealed;
    }

    public boolean isFlaggedAt(int row, int col) {
        Coord c = new Coord(col, row);
        Cell cell = cells.get(c);
        if (cell == null) return false;
        return cell.flagged;
    }

    /**
     * Toggle flag on the cell covered and if the cell
     * is out of bounds
     *row is r and col is c
     * */
    public void toggleFlag(int row, int col) {
        Coord c = new Coord(col, row);
        Cell cell = cells.get(c);
        if (cell == null) return;
        if (cell.revealed) return;
        cell.flagged = !cell.flagged;
    }

    /**this uncovers cell like row col
     *  - this does nothing if out side the bound orflagged
     *  if it contains mines then the game is over
     *
     *
     */
    public void uncover(int row, int col) {
        Coord start = new Coord(col, row);
        Cell startCell = cells.get(start);
        if (startCell == null || startCell.flagged || startCell.revealed) {
            return;
        }

        if (startCell.hasMine) {
            revealAll();
            gameOver = true;
            return;
        }

        Queue<Coord> q = new ArrayDeque<>();
        q.add(start);

        while (!q.isEmpty()) {
            Coord curr = q.remove();
            Cell cell = cells.get(curr);
            if (cell == null || cell.revealed || cell.flagged || cell.hasMine) {
                continue;
            }

            cell.revealed = true;

            if (cell.neighborMines == 0) {
                for (int[] dir : DIRECTIONS) {
                    Coord nb = new Coord(curr.q + dir[0], curr.r + dir[1]);
                    Cell nbCell = cells.get(nb);
                    if
                    (nbCell != null && !nbCell.revealed &&
                            !nbCell.flagged && !nbCell.hasMine) {
                        q.add(nb);
                    }
                }
            }
        }
    }


    private void placeMines(long seed) {
        Random rng = new Random(seed);
        List<Coord> shuffled = new ArrayList<>(allCoords);
        Collections.shuffle(shuffled, rng);
        for (int i = 0; i < numMines; i++) {
            Cell c = cells.get(shuffled.get(i));
            c.hasMine = true;
        }
    }

    private void computeNeighborCounts() {
        for (Coord c : allCoords) {
            Cell cell = cells.get(c);
            int count = 0;
            for (int[] d : DIRECTIONS) {
                Coord nb = new Coord(c.q + d[0], c.r + d[1]);
                Cell nbCell = cells.get(nb);
                if (nbCell != null && nbCell.hasMine) {
                    count++;
                }
            }
            cell.neighborMines = count;
        }
    }

    private void revealAll() {
        for (Cell cell : cells.values()) {
            cell.revealed = true;
        }
    }

    /**
     * it prints row in the form of radiu +1 or radisu-1
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int maxRowCells = 2 * radius - 1;

        for (int r = -radius + 1; r <= radius - 1; r++) {
            int qMin = Math.max(-radius + 1, -r - (radius - 1));
            int qMax = Math.min(radius - 1, -r + (radius - 1));
            int cellsInRow = qMax - qMin + 1;

            int leadingSpaces = maxRowCells - cellsInRow;
            for (int i = 0; i < leadingSpaces; i++) {
                sb.append(' ');
            }

            for (int q = qMin; q <= qMax; q++) {
                Coord c = new Coord(q, r);
                Cell cell = cells.get(c);
                char ch;
                if (!cell.revealed) {
                    ch = cell.flagged ? 'F' : 'c';
                } else if (cell.hasMine) {
                    ch = 'M';
                } else if (cell.neighborMines == 0) {
                    ch = '.';
                } else {
                    ch = (char) ('0' + cell.neighborMines);
                }
                sb.append(ch);
                if (q < qMax) sb.append(' ');
            }
            sb.append('\n');
        }

        return sb.toString();
    }
    /** Total number of mines on the board. */
    public int getNumMines() {
        return numMines;
    }

    /** How many cells are currently flagged. */
    public int getFlagCount() {
        int flags = 0;
        for (Cell cell : cells.values()) {
            if (cell.flagged) {
                flags++;
            }
        }
        return flags;
    }

    /**
     * Returns true if all non-mine cells are revealed.
     * Used to detect a win.
     */
    public boolean allSafeCellsRevealed() {
        for (Cell cell : cells.values()) {
            if (!cell.hasMine && !cell.revealed) {
                return false;
            }
        }
        return true;
    }
}



