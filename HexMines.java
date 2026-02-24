import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hexagonal Minesweeper GUI using HexMineManager as the model.
 *
 * Left-click = uncover cell.
 * Right-click = toggle flag.
 * Difficulty can be changed at the top (Easy / Hard).
 */
public class HexMines extends JFrame {

    private static final int EASY_RADIUS = 5;
    private static final int EASY_MINES  = 10;

    private static final int HARD_RADIUS = 7;
    private static final int HARD_MINES  = 25;

    private HexMineManager manager;

    private BoardPanel boardPanel;
    private JLabel minesLabel;
    private JLabel flagsLabel;
    private JLabel timeLabel;
    private JComboBox<String> difficultyBox;
    private JButton newGameButton;

    private Timer timer;
    private int elapsedSeconds = 0;
    private boolean gameRunning = false;  // timer running?
    private boolean gameFinished = false; // win OR loss

    private int currentRadius = EASY_RADIUS;
    private int currentMines  = EASY_MINES;

    public HexMines() {
        super("Hexagonal Minesweeper");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        difficultyBox = new JComboBox<>(new String[]{"Easy", "Hard"});
        difficultyBox.addActionListener(e -> {
            String s = (String) difficultyBox.getSelectedItem();
            if ("Easy".equals(s)) {
                currentRadius = EASY_RADIUS;
                currentMines  = EASY_MINES;
            } else {
                currentRadius = HARD_RADIUS;
                currentMines  = HARD_MINES;
            }
            startNewGame();
        });

        newGameButton = new JButton("New Game");
        newGameButton.addActionListener(e -> startNewGame());

        minesLabel = new JLabel("Mines: 0");
        flagsLabel = new JLabel("Flags: 0");
        timeLabel  = new JLabel("Time: 0");

        topPanel.add(new JLabel("Difficulty:"));
        topPanel.add(difficultyBox);
        topPanel.add(newGameButton);
        topPanel.add(new JSeparator(SwingConstants.VERTICAL));
        topPanel.add(minesLabel);
        topPanel.add(flagsLabel);
        topPanel.add(timeLabel);

        add(topPanel, BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        timer = new Timer(1000, e -> {
            if (gameRunning && !gameFinished) {
                elapsedSeconds++;
                updateLabels();
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        startNewGame();
    }

    /** Start a new game with currentRadius/currentMines. */
    private void startNewGame() {
        manager = new HexMineManager(currentRadius, currentMines);

        elapsedSeconds = 0;
        gameRunning = false;
        gameFinished = false;

        updateLabels();
        boardPanel.buildCells();
        boardPanel.repaint();
    }

    private void updateLabels() {
        if (manager != null) {
            minesLabel.setText("Mines: " + manager.getNumMines());
            flagsLabel.setText("Flags: " + manager.getFlagCount());
        } else {
            minesLabel.setText("Mines: 0");
            flagsLabel.setText("Flags: 0");
        }
        timeLabel.setText("Time: " + elapsedSeconds);
    }

    private void handleWin() {
        gameFinished = true;
        gameRunning  = false;
        timer.stop();

        boardPanel.repaint();
        JOptionPane.showMessageDialog(this,
                "You cleared all the safe cells! You win!",
                "Game Over",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleLoss() {
        gameFinished = true;
        gameRunning  = false;
        timer.stop();

        boardPanel.repaint();
        JOptionPane.showMessageDialog(this,
                "Boom! You hit a mine.",
                "Game Over",
                JOptionPane.ERROR_MESSAGE);
    }


    private class BoardPanel extends JPanel implements MouseListener {

        private static final int HEX_SIZE = 25;  // radius in pixels
        private static final int HEX_GAP  = 2;

        private List<Cell> cells = new ArrayList<>();

        BoardPanel() {
            setPreferredSize(new Dimension(800, 800));
            addMouseListener(this);
        }

        /** Build the hex cells geometry for the current board. */
        void buildCells() {
            cells.clear();
            if (manager == null) return;

            int R = manager.getRadius();

            int centerX = getPreferredSize().width / 2;
            int centerY = getPreferredSize().height / 2;

            for (int row = -R + 1; row <= R - 1; row++) {
                int colMin = Math.max(-R + 1, -row - (R - 1));
                int colMax = Math.min(R - 1, -row + (R - 1));

                for (int col = colMin; col <= colMax; col++) {

                    double x = HEX_SIZE * Math.sqrt(3) * (col + row / 2.0);
                    double y = HEX_SIZE * 1.5 * row;

                    int px = (int) Math.round(centerX + x);
                    int py = (int) Math.round(centerY + y);

                    Polygon poly = createHexPolygon(px, py, HEX_SIZE - HEX_GAP);
                    cells.add(new Cell(row, col, poly));
                }
            }

            revalidate();
            repaint();
        }

        private Polygon createHexPolygon(int cx, int cy, int radius) {
            Polygon p = new Polygon();
            for (int i = 0; i < 6; i++) {
                double angleRad = Math.toRadians(60 * i - 30); // pointy-top
                int x = cx + (int) Math.round(radius * Math.cos(angleRad));
                int y = cy + (int) Math.round(radius * Math.sin(angleRad));
                p.addPoint(x, y);
            }
            return p;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (manager == null) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            for (Cell c : cells) {
                int row = c.row;
                int col = c.col;

                if (!manager.inBounds(row, col)) {
                    continue;
                }

                boolean revealed = manager.isRevealedAt(row, col);
                boolean flagged  = manager.isFlaggedAt(row, col);
                boolean mined    = manager.hasMineAt(row, col);
                int neigh        = manager.getNeighborCountAt(row, col);

                if (!revealed) {
                    g2.setColor(Color.LIGHT_GRAY);
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillPolygon(c.poly);

                g2.setColor(Color.DARK_GRAY);
                g2.drawPolygon(c.poly);

                if (!revealed && flagged) {
                    g2.setColor(Color.RED);
                    drawCenteredString(g2, "F", c.poly.getBounds());
                } else if (revealed) {
                    if (mined) {
                        g2.setColor(Color.BLACK);
                        drawCenteredString(g2, "M", c.poly.getBounds());
                    } else if (neigh > 0) {
                        g2.setColor(numberColor(neigh));
                        drawCenteredString(g2, String.valueOf(neigh),
                                c.poly.getBounds());
                    } else {
                        g2.setColor(Color.GRAY);
                        drawCenteredString(g2, ".", c.poly.getBounds());
                    }
                }
            }
        }

        private Color numberColor(int n) {
            switch (n) {
                case 1: return Color.BLUE;
                case 2: return new Color(0, 128, 0);
                case 3: return Color.RED;
                case 4: return new Color(0, 0, 128);
                case 5: return new Color(128, 0, 0);
                case 6: return new Color(0, 128, 128);
                case 7: return Color.BLACK;
                case 8: return Color.GRAY;
                default: return Color.BLACK;
            }
        }

        private void drawCenteredString(Graphics2D g2, String text, Rectangle r) {
            FontMetrics fm = g2.getFontMetrics();
            int x = r.x + (r.width  - fm.stringWidth(text)) / 2;
            int y = r.y + (r.height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(text, x, y);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (manager == null || gameFinished) return;

            if (!gameRunning) {
                gameRunning = true;
                timer.start();
            }

            int x = e.getX();
            int y = e.getY();

            Cell clicked = null;
            for (Cell c : cells) {
                if (c.poly.contains(x, y)) {
                    clicked = c;
                    break;
                }
            }
            if (clicked == null) return;

            int row = clicked.row;
            int col = clicked.col;

            if (!manager.inBounds(row, col)) return;

            boolean left  = SwingUtilities.isLeftMouseButton(e);
            boolean right = SwingUtilities.isRightMouseButton(e);

            if (left) {
                if (!manager.isRevealedAt(row, col) &&
                        !manager.isFlaggedAt(row, col)) {

                    manager.uncover(row, col);

                    if (manager.isGameOver()) {
                        handleLoss();
                    } else if (manager.allSafeCellsRevealed()) {
                        handleWin();
                    }
                }
            } else if (right) {
                if (!manager.isRevealedAt(row, col)) {
                    manager.toggleFlag(row, col);
                }
            }

            updateLabels();
            repaint();
        }

        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseClicked(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }

    /** Simple container for one hex cell (geometry + row/col indices). */
    private static class Cell {
        final int row;
        final int col;
        final Polygon poly;

        Cell(int row, int col, Polygon poly) {
            this.row = row;
            this.col = col;
            this.poly = poly;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HexMines::new);
    }
}
