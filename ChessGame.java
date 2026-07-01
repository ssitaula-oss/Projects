import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class ChessGame extends JFrame {
    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG            = new Color(22, 21, 18);
    private static final Color LIGHT_SQ      = new Color(240, 217, 181);
    private static final Color DARK_SQ       = new Color(181, 136, 99);
    private static final Color SELECTED_SQ   = new Color(246, 246, 105);
    private static final Color HINT_SQ       = new Color(130, 190, 90);
    private static final Color LAST_FROM_SQ  = new Color(205, 210, 106);
    private static final Color LAST_TO_SQ    = new Color(170, 162, 58);
    private static final Color CHECK_SQ      = new Color(220, 60, 60);
    private static final Color PANEL_BG      = new Color(38, 36, 33);
    private static final Color TEXT_COLOR    = new Color(220, 210, 190);
    private static final Color ACCENT        = new Color(189, 140, 80);
    private static final Color BTN_BG        = new Color(60, 55, 48);
    private static final Color BTN_HOVER     = new Color(80, 74, 64);
    private static final Color TIMER_ACTIVE  = new Color(240, 220, 160);
    private static final Color TIMER_LOW     = new Color(220, 70, 70);

    // ── UI ────────────────────────────────────────────────────────────────────
    private final JButton[][] buttons = new JButton[8][8];
    private final JLabel[] fileLabels = new JLabel[8]; // a-h along bottom
    private final JLabel[] rankLabels = new JLabel[8]; // 1-8 along side

    // Status bar
    private final JLabel statusLabel   = new JLabel("White to move", SwingConstants.CENTER);
    // Timers (clock displays)
    private final JLabel whiteTimerLabel = makeTimerLabel();
    private final JLabel blackTimerLabel = makeTimerLabel();
    // Captured pieces
    private final JLabel whiteCaptured  = new JLabel("", SwingConstants.LEFT);
    private final JLabel blackCaptured  = new JLabel("", SwingConstants.LEFT);
    // Move counter
    private final JLabel moveCountLabel = new JLabel("Move 1", SwingConstants.CENTER);

    // ── Game state ────────────────────────────────────────────────────────────
    private final Board board = new Board();
    private boolean whiteTurn = true;
    private boolean vsAI = true;
    private int selectedRow = -1, selectedCol = -1;
    private Set<String> legalTargets = new HashSet<>();
    private int lastFromRow = -1, lastFromCol = -1, lastToRow = -1, lastToCol = -1;
    private int moveCount = 1;
    private final List<Piece> whiteLost = new ArrayList<>();
    private final List<Piece> blackLost = new ArrayList<>();
    private boolean gameOver = false;

    // ── Clock ─────────────────────────────────────────────────────────────────
    private static final int INITIAL_SECONDS = 10 * 60; // 10 minutes each
    private int whiteSecondsLeft = INITIAL_SECONDS;
    private int blackSecondsLeft = INITIAL_SECONDS;
    private javax.swing.Timer clockTimer;

    private final ChessAI ai = new ChessAI(3);

    // ─────────────────────────────────────────────────────────────────────────
    public ChessGame() {
        setTitle("Chess");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenterArea(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        setupClock();
        updateUIBoard();
        pack();
        setMinimumSize(new Dimension(700, 780));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ── Build top bar (black player info + timer) ─────────────────────────────
    private JPanel buildTopBar() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        // Left: black captured
        JPanel leftSide = new JPanel(new BorderLayout(4, 0));
        leftSide.setBackground(PANEL_BG);
        JLabel blackIcon = new JLabel("♟ Black");
        blackIcon.setForeground(TEXT_COLOR);
        blackIcon.setFont(new Font("SansSerif", Font.BOLD, 14));
        blackCaptured.setForeground(new Color(160, 150, 135));
        blackCaptured.setFont(new Font("SansSerif", Font.PLAIN, 15));
        leftSide.add(blackIcon, BorderLayout.WEST);
        leftSide.add(blackCaptured, BorderLayout.CENTER);

        panel.add(leftSide, BorderLayout.CENTER);
        panel.add(blackTimerLabel, BorderLayout.EAST);
        return panel;
    }

    // ── Build center (board + rank labels) ───────────────────────────────────
    private JPanel buildCenterArea() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 12));

        // Rank labels on the left
        JPanel rankPanel = new JPanel(new GridLayout(8, 1));
        rankPanel.setBackground(BG);
        rankPanel.setPreferredSize(new Dimension(20, 0));
        for (int r = 0; r < 8; r++) {
            JLabel lbl = new JLabel(String.valueOf(8 - r), SwingConstants.CENTER);
            lbl.setForeground(ACCENT);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            rankLabels[r] = lbl;
            rankPanel.add(lbl);
        }

        // Board grid
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setBorder(BorderFactory.createLineBorder(ACCENT, 2));
        Font pieceFont = new Font("SansSerif", Font.PLAIN, 44);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = new JButton();
                b.setFont(pieceFont);
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setOpaque(true);
                b.setMargin(new Insets(0, 0, 0, 0));
                final int row = r, col = c;
                b.addActionListener(e -> handleClick(row, col));
                // Hover effect
                b.addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        if (!b.getBackground().equals(SELECTED_SQ) &&
                            !b.getBackground().equals(CHECK_SQ))
                            b.setBackground(b.getBackground().brighter());
                    }
                    @Override public void mouseExited(MouseEvent e) { updateUIBoard(); }
                });
                buttons[r][c] = b;
                boardPanel.add(b);
            }
        }

        // File labels along the bottom
        JPanel filePanel = new JPanel(new GridLayout(1, 8));
        filePanel.setBackground(BG);
        filePanel.setPreferredSize(new Dimension(0, 18));
        // left offset to align under rank label column
        JPanel filePanelWrapper = new JPanel(new BorderLayout());
        filePanelWrapper.setBackground(BG);
        JPanel fileOffset = new JPanel();
        fileOffset.setBackground(BG);
        fileOffset.setPreferredSize(new Dimension(20, 18));
        filePanelWrapper.add(fileOffset, BorderLayout.WEST);
        filePanelWrapper.add(filePanel, BorderLayout.CENTER);
        for (int c = 0; c < 8; c++) {
            JLabel lbl = new JLabel(String.valueOf((char)('a' + c)), SwingConstants.CENTER);
            lbl.setForeground(ACCENT);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 11));
            fileLabels[c] = lbl;
            filePanel.add(lbl);
        }

        wrapper.add(rankPanel, BorderLayout.WEST);
        wrapper.add(boardPanel, BorderLayout.CENTER);
        wrapper.add(filePanelWrapper, BorderLayout.SOUTH);
        return wrapper;
    }

    // ── Build bottom bar (white player info, status, controls) ────────────────
    private JPanel buildBottomBar() {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(PANEL_BG);

        // White player row
        JPanel whiteRow = new JPanel(new BorderLayout(12, 0));
        whiteRow.setBackground(PANEL_BG);
        whiteRow.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JPanel leftSide = new JPanel(new BorderLayout(4, 0));
        leftSide.setBackground(PANEL_BG);
        JLabel whiteIcon = new JLabel("♙ White");
        whiteIcon.setForeground(TEXT_COLOR);
        whiteIcon.setFont(new Font("SansSerif", Font.BOLD, 14));
        whiteCaptured.setForeground(new Color(160, 150, 135));
        whiteCaptured.setFont(new Font("SansSerif", Font.PLAIN, 15));
        leftSide.add(whiteIcon, BorderLayout.WEST);
        leftSide.add(whiteCaptured, BorderLayout.CENTER);

        whiteRow.add(leftSide, BorderLayout.CENTER);
        whiteRow.add(whiteTimerLabel, BorderLayout.EAST);

        // Controls row
        JPanel ctrlRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
        ctrlRow.setBackground(new Color(28, 27, 24));

        statusLabel.setForeground(TEXT_COLOR);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        moveCountLabel.setForeground(ACCENT);
        moveCountLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JButton newGameBtn  = makeButton("New Game");
        JButton modeBtn     = makeModeBtn();
        JButton resignBtn   = makeButton("Resign");

        newGameBtn.addActionListener(e -> resetGame());
        modeBtn.addActionListener(e -> {
            vsAI = !vsAI;
            modeBtn.setText(vsAI ? "vs AI" : "vs Friend");
            resetGame();
        });
        resignBtn.addActionListener(e -> {
            if (!gameOver) {
                gameOver = true;
                stopClock();
                statusLabel.setText((whiteTurn ? "White" : "Black") + " resigned!");
            }
        });

        ctrlRow.add(newGameBtn);
        ctrlRow.add(modeBtn);
        ctrlRow.add(resignBtn);
        ctrlRow.add(Box.createHorizontalStrut(8));
        ctrlRow.add(statusLabel);
        ctrlRow.add(Box.createHorizontalStrut(8));
        ctrlRow.add(moveCountLabel);

        outer.add(whiteRow, BorderLayout.NORTH);
        outer.add(ctrlRow, BorderLayout.SOUTH);
        return outer;
    }

    // ── Clock setup ───────────────────────────────────────────────────────────
    private void setupClock() {
        clockTimer = new javax.swing.Timer(1000, e -> tickClock());
        clockTimer.start();
    }

    private void tickClock() {
        if (gameOver) return;
        if (whiteTurn) {
            whiteSecondsLeft--;
            if (whiteSecondsLeft <= 0) {
                whiteSecondsLeft = 0;
                gameOver = true;
                stopClock();
                statusLabel.setText("White's time is up — Black wins!");
            }
        } else {
            blackSecondsLeft--;
            if (blackSecondsLeft <= 0) {
                blackSecondsLeft = 0;
                gameOver = true;
                stopClock();
                statusLabel.setText("Black's time is up — White wins!");
            }
        }
        updateTimerLabels();
    }

    private void stopClock() {
        if (clockTimer != null) clockTimer.stop();
    }

    private void updateTimerLabels() {
        whiteTimerLabel.setText(formatTime(whiteSecondsLeft));
        blackTimerLabel.setText(formatTime(blackSecondsLeft));

        // Dim the inactive clock, highlight the active one
        boolean wActive = whiteTurn && !gameOver;
        boolean bActive = !whiteTurn && !gameOver;

        whiteTimerLabel.setForeground(wActive
            ? (whiteSecondsLeft <= 30 ? TIMER_LOW : TIMER_ACTIVE)
            : new Color(100, 95, 85));
        blackTimerLabel.setForeground(bActive
            ? (blackSecondsLeft <= 30 ? TIMER_LOW : TIMER_ACTIVE)
            : new Color(100, 95, 85));

        // Pulsing border on active timer
        whiteTimerLabel.setBorder(wActive
            ? BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT)
            : BorderFactory.createEmptyBorder(0, 0, 2, 0));
        blackTimerLabel.setBorder(bActive
            ? BorderFactory.createMatteBorder(0, 0, 2, 0, ACCENT)
            : BorderFactory.createEmptyBorder(0, 0, 2, 0));
    }

    private String formatTime(int seconds) {
        int m = seconds / 60, s = seconds % 60;
        return String.format("%d:%02d", m, s);
    }

    // ── Game logic ────────────────────────────────────────────────────────────
    private void resetGame() {
        board.setupStartPosition();
        whiteTurn = true;
        selectedRow = -1; selectedCol = -1;
        legalTargets.clear();
        lastFromRow = -1; lastFromCol = -1; lastToRow = -1; lastToCol = -1;
        moveCount = 1;
        whiteLost.clear(); blackLost.clear();
        whiteSecondsLeft = INITIAL_SECONDS;
        blackSecondsLeft = INITIAL_SECONDS;
        gameOver = false;
        updateTimerLabels();
        updateCapturedLabels();
        moveCountLabel.setText("Move 1");
        stopClock();
        setupClock();
        updateUIBoard();
    }

    private void handleClick(int row, int col) {
        if (gameOver) return;
        if (vsAI && !whiteTurn) return;

        Piece clicked = board.get(row, col);

        if (selectedRow == -1) {
            if (clicked != null && clicked.white == whiteTurn) {
                selectedRow = row; selectedCol = col;
                // Compute legal targets for hint dots
                legalTargets.clear();
                for (Move m : board.getLegalMoves(whiteTurn)) {
                    if (m.fromRow == row && m.fromCol == col) {
                        legalTargets.add(m.toRow + "," + m.toCol);
                    }
                }
            }
        } else {
            Move move = new Move(selectedRow, selectedCol, row, col);
            List<Move> legalMoves = board.getLegalMoves(whiteTurn);

            if (legalMoves.contains(move)) {
                // Track captured piece before the move
                Piece target = board.get(row, col);
                if (target != null) {
                    if (whiteTurn) blackLost.add(target); else whiteLost.add(target);
                }

                board.makeMove(move);
                lastFromRow = selectedRow; lastFromCol = selectedCol;
                lastToRow = row; lastToCol = col;

                if (!whiteTurn) moveCount++; // increment after black moves
                moveCountLabel.setText("Move " + moveCount);

                whiteTurn = !whiteTurn;
                selectedRow = -1; selectedCol = -1;
                legalTargets.clear();
                updateCapturedLabels();
                updateUIBoard();

                if (gameEnded()) return;

                if (vsAI && !whiteTurn) {
                    javax.swing.Timer t = new javax.swing.Timer(300, e -> makeAIMove());
                    t.setRepeats(false);
                    t.start();
                }
            } else {
                if (clicked != null && clicked.white == whiteTurn) {
                    selectedRow = row; selectedCol = col;
                    legalTargets.clear();
                    for (Move m : board.getLegalMoves(whiteTurn)) {
                        if (m.fromRow == row && m.fromCol == col) {
                            legalTargets.add(m.toRow + "," + m.toCol);
                        }
                    }
                } else {
                    selectedRow = -1; selectedCol = -1;
                    legalTargets.clear();
                }
            }
        }
        updateUIBoard();
    }

    private void makeAIMove() {
        if (gameOver) return;
        Move aiMove = ai.findBestMove(board, false);
        if (aiMove != null) {
            Piece target = board.get(aiMove.toRow, aiMove.toCol);
            if (target != null) whiteLost.add(target);

            board.makeMove(aiMove);
            lastFromRow = aiMove.fromRow; lastFromCol = aiMove.fromCol;
            lastToRow = aiMove.toRow; lastToCol = aiMove.toCol;
            moveCount++;
            moveCountLabel.setText("Move " + moveCount);
            whiteTurn = true;
            updateCapturedLabels();
        }
        updateUIBoard();
        gameEnded();
    }

    private boolean gameEnded() {
        List<Move> moves = board.getLegalMoves(whiteTurn);
        if (moves.isEmpty()) {
            gameOver = true;
            stopClock();
            if (board.isKingInCheck(whiteTurn)) {
                String winner = whiteTurn ? "Black" : "White";
                statusLabel.setText("Checkmate — " + winner + " wins!");
            } else {
                statusLabel.setText("Stalemate — it's a draw!");
            }
            return true;
        }
        return false;
    }

    private void updateUIBoard() {
        boolean kingInCheck = board.isKingInCheck(whiteTurn);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                JButton b = buttons[r][c];
                Piece p = board.get(r, c);
                b.setText(p == null ? "" : p.symbol());

                // Determine foreground color for piece
                if (p != null) {
                    b.setForeground(p.white ? Color.WHITE : new Color(30, 20, 10));
                }

                Color bg = squareColor(r, c, kingInCheck, true, false, false);
                b.setBackground(bg);
            }
        }

        if (!gameOver) {
            String turn = whiteTurn ? "White" : "Black";
            String check = kingInCheck ? " — Check!" : "";
            statusLabel.setText(turn + " to move" + check);
        }
        updateTimerLabels();
    }

    private Color squareColor(int r, int c, boolean kingInCheck, boolean doHints,
                              boolean dummy1, boolean dummy2) {
        Piece p = board.get(r, c);

        // Check highlight: king's square
        if (kingInCheck && p != null && p.white == whiteTurn && p.type.equals("KING")) {
            return CHECK_SQ;
        }
        // Selected square
        if (r == selectedRow && c == selectedCol) return SELECTED_SQ;
        // Legal move hint dots
        if (doHints && legalTargets.contains(r + "," + c)) return HINT_SQ;
        // Last move highlight
        if ((r == lastFromRow && c == lastFromCol) || (r == lastToRow && c == lastToCol)) {
            return (r == lastToRow && c == lastToCol) ? LAST_TO_SQ : LAST_FROM_SQ;
        }
        // Normal board color
        return ((r + c) % 2 == 0) ? LIGHT_SQ : DARK_SQ;
    }

    private void updateCapturedLabels() {
        whiteCaptured.setText(buildCapturedString(blackLost)); // pieces white took
        blackCaptured.setText(buildCapturedString(whiteLost)); // pieces black took
    }

    private String buildCapturedString(List<Piece> pieces) {
        if (pieces.isEmpty()) return "";
        Map<String, Integer> counts = new LinkedHashMap<>();
        String[] order = {"QUEEN","ROOK","BISHOP","KNIGHT","PAWN"};
        for (String t : order) counts.put(t, 0);
        for (Piece p : pieces) counts.merge(p.type, 1, Integer::sum);

        StringBuilder sb = new StringBuilder();
        for (String t : order) {
            int n = counts.get(t);
            if (n == 0) continue;
            // Use one representative symbol (color of captured piece matters not; show white symbols)
            String sym = symbolFor(t);
            sb.append(sym.repeat(n)).append(" ");
        }
        return sb.toString().trim();
    }

    private String symbolFor(String type) {
        return switch (type) {
            case "QUEEN"  -> "♛";
            case "ROOK"   -> "♜";
            case "BISHOP" -> "♝";
            case "KNIGHT" -> "♞";
            case "PAWN"   -> "♟";
            default       -> "";
        };
    }

    // ── UI helpers ────────────────────────────────────────────────────────────
    private JLabel makeTimerLabel() {
        JLabel lbl = new JLabel("10:00", SwingConstants.CENTER);
        lbl.setForeground(new Color(100, 95, 85));
        lbl.setFont(new Font("Monospaced", Font.BOLD, 22));
        lbl.setPreferredSize(new Dimension(80, 36));
        return lbl;
    }

    private JButton makeButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBackground(BTN_BG);
        btn.setForeground(TEXT_COLOR);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1, true),
            BorderFactory.createEmptyBorder(5, 14, 5, 14)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(BTN_HOVER); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(BTN_BG); }
        });
        return btn;
    }

    private JButton makeModeBtn() {
        JButton btn = makeButton(vsAI ? "vs AI" : "vs Friend");
        return btn;
    }

    public static void main(String[] args) {
        // Enable antialiasing for text on some JVMs
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(ChessGame::new);
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Board
// ═════════════════════════════════════════════════════════════════════════════
class Board {
    Piece[][] board = new Piece[8][8];

    Board() { setupStartPosition(); }

    Piece get(int r, int c) {
        if (!inside(r, c)) return null;
        return board[r][c];
    }

    boolean inside(int r, int c) { return r >= 0 && r < 8 && c >= 0 && c < 8; }

    void setupStartPosition() {
        board = new Piece[8][8];
        String[] order = {"ROOK","KNIGHT","BISHOP","QUEEN","KING","BISHOP","KNIGHT","ROOK"};
        for (int c = 0; c < 8; c++) {
            board[0][c] = new Piece(order[c], false);
            board[1][c] = new Piece("PAWN", false);
            board[6][c] = new Piece("PAWN", true);
            board[7][c] = new Piece(order[c], true);
        }
    }

    void makeMove(Move m) {
        Piece moving = board[m.fromRow][m.fromCol];
        m.captured = board[m.toRow][m.toCol];
        board[m.toRow][m.toCol] = moving;
        board[m.fromRow][m.fromCol] = null;
        if (moving != null && moving.type.equals("PAWN")) {
            if ((moving.white && m.toRow == 0) || (!moving.white && m.toRow == 7)) {
                m.promotedFromPawn = true;
                moving.type = "QUEEN";
            }
        }
    }

    void undoMove(Move m) {
        Piece moving = board[m.toRow][m.toCol];
        if (m.promotedFromPawn && moving != null) moving.type = "PAWN";
        board[m.fromRow][m.fromCol] = moving;
        board[m.toRow][m.toCol] = m.captured;
        m.promotedFromPawn = false;
    }

    List<Move> getLegalMoves(boolean white) {
        List<Move> moves = new ArrayList<>();
        for (Move m : getPseudoMoves(white)) {
            makeMove(m);
            boolean safe = !isKingInCheck(white);
            undoMove(m);
            if (safe) moves.add(m);
        }
        return moves;
    }

    List<Move> getPseudoMoves(boolean white) {
        List<Move> moves = new ArrayList<>();
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.white == white) addPieceMoves(moves, r, c, p);
            }
        return moves;
    }

    void addPieceMoves(List<Move> moves, int r, int c, Piece p) {
        switch (p.type) {
            case "PAWN"   -> addPawnMoves(moves, r, c, p.white);
            case "KNIGHT" -> addKnightMoves(moves, r, c, p.white);
            case "BISHOP" -> addSlidingMoves(moves, r, c, p.white, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1}});
            case "ROOK"   -> addSlidingMoves(moves, r, c, p.white, new int[][]{{1,0},{-1,0},{0,1},{0,-1}});
            case "QUEEN"  -> addSlidingMoves(moves, r, c, p.white, new int[][]{{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{-1,0},{0,1},{0,-1}});
            case "KING"   -> addKingMoves(moves, r, c, p.white);
        }
    }

    void addPawnMoves(List<Move> moves, int r, int c, boolean white) {
        int dir = white ? -1 : 1, startRow = white ? 6 : 1;
        if (inside(r+dir,c) && board[r+dir][c] == null) {
            moves.add(new Move(r,c,r+dir,c));
            if (r == startRow && board[r+2*dir][c] == null) moves.add(new Move(r,c,r+2*dir,c));
        }
        for (int dc : new int[]{-1,1}) {
            int nr=r+dir, nc=c+dc;
            if (inside(nr,nc) && board[nr][nc] != null && board[nr][nc].white != white)
                moves.add(new Move(r,c,nr,nc));
        }
    }

    void addKnightMoves(List<Move> moves, int r, int c, boolean white) {
        for (int[] j : new int[][]{{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{1,-2},{-1,2},{-1,-2}})
            addIfValid(moves, r, c, r+j[0], c+j[1], white);
    }

    void addKingMoves(List<Move> moves, int r, int c, boolean white) {
        for (int dr=-1; dr<=1; dr++)
            for (int dc=-1; dc<=1; dc++)
                if (dr!=0||dc!=0) addIfValid(moves,r,c,r+dr,c+dc,white);
    }

    void addSlidingMoves(List<Move> moves, int r, int c, boolean white, int[][] dirs) {
        for (int[] d : dirs) {
            int nr=r+d[0], nc=c+d[1];
            while (inside(nr,nc)) {
                if (board[nr][nc] == null) moves.add(new Move(r,c,nr,nc));
                else { if (board[nr][nc].white!=white) moves.add(new Move(r,c,nr,nc)); break; }
                nr+=d[0]; nc+=d[1];
            }
        }
    }

    void addIfValid(List<Move> moves, int fr, int fc, int tr, int tc, boolean white) {
        if (!inside(tr,tc)) return;
        if (board[tr][tc]==null || board[tr][tc].white!=white) moves.add(new Move(fr,fc,tr,tc));
    }

    boolean isKingInCheck(boolean whiteKing) {
        int kr=-1, kc=-1;
        for (int r=0; r<8; r++) for (int c=0; c<8; c++) {
            Piece p=board[r][c];
            if (p!=null && p.white==whiteKing && p.type.equals("KING")) { kr=r; kc=c; }
        }
        if (kr==-1) return true;
        for (Move m : getPseudoMoves(!whiteKing))
            if (m.toRow==kr && m.toCol==kc) return true;
        return false;
    }

    int evaluate() {
        int score=0;
        for (int r=0; r<8; r++) for (int c=0; c<8; c++) {
            Piece p=board[r][c];
            if (p!=null) score += p.white ? p.value() : -p.value();
        }
        return score;
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  ChessAI
// ═════════════════════════════════════════════════════════════════════════════
class ChessAI {
    private final int depth;
    ChessAI(int depth) { this.depth = depth; }

    Move findBestMove(Board board, boolean aiWhite) {
        List<Move> moves = board.getLegalMoves(aiWhite);
        if (moves.isEmpty()) return null;
        Move best = null;
        int bestScore = aiWhite ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move m : moves) {
            board.makeMove(m);
            int score = minimax(board, depth-1, !aiWhite, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board.undoMove(m);
            if ((aiWhite && score > bestScore) || (!aiWhite && score < bestScore)) {
                bestScore = score; best = m;
            }
        }
        return best;
    }

    int minimax(Board board, int depth, boolean whiteTurn, int alpha, int beta) {
        List<Move> moves = board.getLegalMoves(whiteTurn);
        if (depth==0 || moves.isEmpty()) {
            if (moves.isEmpty()) return board.isKingInCheck(whiteTurn) ? (whiteTurn ? -100000 : 100000) : 0;
            return board.evaluate();
        }
        if (whiteTurn) {
            int max = Integer.MIN_VALUE;
            for (Move m : moves) {
                board.makeMove(m);
                int s = minimax(board, depth-1, false, alpha, beta);
                board.undoMove(m);
                max = Math.max(max,s); alpha = Math.max(alpha,s);
                if (beta<=alpha) break;
            }
            return max;
        } else {
            int min = Integer.MAX_VALUE;
            for (Move m : moves) {
                board.makeMove(m);
                int s = minimax(board, depth-1, true, alpha, beta);
                board.undoMove(m);
                min = Math.min(min,s); beta = Math.min(beta,s);
                if (beta<=alpha) break;
            }
            return min;
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Piece
// ═════════════════════════════════════════════════════════════════════════════
class Piece {
    String type; boolean white;
    Piece(String type, boolean white) { this.type=type; this.white=white; }

    int value() {
        return switch (type) {
            case "PAWN" -> 100; case "KNIGHT" -> 320; case "BISHOP" -> 330;
            case "ROOK" -> 500; case "QUEEN"  -> 900; case "KING"   -> 20000;
            default -> 0;
        };
    }

    String symbol() {
        if (white) return switch (type) {
            case "KING"->"♔"; case "QUEEN"->"♕"; case "ROOK"->"♖";
            case "BISHOP"->"♗"; case "KNIGHT"->"♘"; case "PAWN"->"♙"; default->"";
        };
        return switch (type) {
            case "KING"->"♚"; case "QUEEN"->"♛"; case "ROOK"->"♜";
            case "BISHOP"->"♝"; case "KNIGHT"->"♞"; case "PAWN"->"♟"; default->"";
        };
    }
}

// ═════════════════════════════════════════════════════════════════════════════
//  Move
// ═════════════════════════════════════════════════════════════════════════════
class Move {
    int fromRow, fromCol, toRow, toCol;
    Piece captured; boolean promotedFromPawn = false;

    Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow=fromRow; this.fromCol=fromCol; this.toRow=toRow; this.toCol=toCol;
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Move o)) return false;
        return fromRow==o.fromRow && fromCol==o.fromCol && toRow==o.toRow && toCol==o.toCol;
    }
    @Override public int hashCode() { return Objects.hash(fromRow,fromCol,toRow,toCol); }
}