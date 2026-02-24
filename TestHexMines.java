/**
 * this is the tester that tests the HexMineManager by making the board
 * and helps find the all the mines and lyaout of the board.
 * */
public class TestHexMines {

    public static void main(String[] args) {

        System.out.println("Summary of symbols in my string representations");
        System.out.println("c : covered cell");
        System.out.println("F : flagged cell");
        System.out.println("M : uncovered mine");
        System.out.println(". : uncovered cell with no neighboring mines");
        System.out.println("<number n> : uncovered cell with n neighboring mines");
        System.out.println();

        // First manager
        System.out.println("Test first manager");
        HexMineManager manager1 = new HexMineManager(5, 10, 42L);
        System.out.println("initializing hexagon with radius 5 and 10 mines");
        runDemo(manager1);

        System.out.println();

        // Second manager
        System.out.println("Test second manager");
        HexMineManager manager2 = new HexMineManager(7, 30, 99L);
        System.out.println("initializing hexagon with radius 7 and 30 mines");
        runDemo(manager2);

        System.out.println();
        System.out.println("Go back and test first manager some more");
        runExtraTestsOnFirst(manager1);
    }

    private static void runDemo(HexMineManager manager) {
        System.out.println(manager);

        int[] nCell = findSafeWithNeighbors(manager);
        if (nCell != null) {
            System.out.printf("uncover (%d, %d)%n", nCell[0], nCell[1]);
            manager.uncover(nCell[0], nCell[1]);
            System.out.println(manager);
        }

        int[] zCell = findSafeZeroNeighbors(manager);
        if (zCell != null) {
            System.out.printf("uncover (%d, %d)%n", zCell[0], zCell[1]);
            manager.uncover(zCell[0], zCell[1]);
            System.out.println(manager);
        }

        int[] flagCell = findCoveredCell(manager);
        if (flagCell != null) {
            System.out.printf("toggle flag at (%d, %d)%n", flagCell[0], flagCell[1]);
            manager.toggleFlag(flagCell[0], flagCell[1]);
            System.out.println(manager);

            System.out.printf("toggle flag at (%d, %d)%n", flagCell[0], flagCell[1]);
            manager.toggleFlag(flagCell[0], flagCell[1]);
            System.out.println(manager);
        }

        int[] mineCell = findCoveredMine(manager);
        if (mineCell != null) {
            System.out.printf("uncover (%d, %d)%n", mineCell[0], mineCell[1]);
            manager.uncover(mineCell[0], mineCell[1]);
            System.out.println(manager);
            System.out.println("Oops! Uncovered a mine!");
        }
    }

    private static void runExtraTestsOnFirst(HexMineManager manager) {
        System.out.println(manager);

        int[] safe = findAnyCoveredSafe(manager);
        if (safe != null) {
            System.out.printf("uncover (%d, %d)%n", safe[0], safe[1]);
            manager.uncover(safe[0], safe[1]);
            System.out.println(manager);
        }

        int[] flag = findCoveredCell(manager);
        if (flag != null) {
            System.out.printf("toggle flag at (%d, %d)%n", flag[0], flag[1]);
            manager.toggleFlag(flag[0], flag[1]);
            System.out.println(manager);
        }
    }


    private static int[] findSafeWithNeighbors(HexMineManager m) {
        int rMax = m.getRadius();
        for (int r = -rMax + 1; r <= rMax - 1; r++) {
            for (int c = -rMax + 1; c <= rMax - 1; c++) {
                if (!m.inBounds(r, c)) continue;
                if (!m.hasMineAt(r, c)
                        && m.getNeighborCountAt(r, c) > 0
                        && !m.isRevealedAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private static int[] findSafeZeroNeighbors(HexMineManager m) {
        int rMax = m.getRadius();
        for (int r = -rMax + 1; r <= rMax - 1; r++) {
            for (int c = -rMax + 1; c <= rMax - 1; c++) {
                if (!m.inBounds(r, c)) continue;
                if (!m.hasMineAt(r, c)
                        && m.getNeighborCountAt(r, c) == 0
                        && !m.isRevealedAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private static int[] findCoveredCell(HexMineManager m) {
        int rMax = m.getRadius();
        for (int r = -rMax + 1; r <= rMax - 1; r++) {
            for (int c = -rMax + 1; c <= rMax - 1; c++) {
                if (!m.inBounds(r, c)) continue;
                if (!m.isRevealedAt(r, c) && !m.isFlaggedAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private static int[] findCoveredMine(HexMineManager m) {
        int rMax = m.getRadius();
        for (int r = -rMax + 1; r <= rMax - 1; r++) {
            for (int c = -rMax + 1; c <= rMax - 1; c++) {
                if (!m.inBounds(r, c)) continue;
                if (m.hasMineAt(r, c)
                        && !m.isRevealedAt(r, c)
                        && !m.isFlaggedAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }

    private static int[] findAnyCoveredSafe(HexMineManager m) {
        int rMax = m.getRadius();
        for (int r = -rMax + 1; r <= rMax - 1; r++) {
            for (int c = -rMax + 1; c <= rMax - 1; c++) {
                if (!m.inBounds(r, c)) continue;
                if (!m.hasMineAt(r, c)
                        && !m.isRevealedAt(r, c)
                        && !m.isFlaggedAt(r, c)) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }
}
