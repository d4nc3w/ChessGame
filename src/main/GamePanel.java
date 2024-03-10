package main;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import piece.*;

public class GamePanel extends JPanel
        implements Runnable {

    public static final int WIDTH = 1100;
    public static final int HEIGHT = 800;
    final int FPS = 60;
    Thread GameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    //PIECES
    public static ArrayList<Piece> backupPieces = new ArrayList<>();
    public static ArrayList<Piece> pieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activePiece, checkingPiece;
    public static Piece castlingPiece;

    //COLOR
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    //BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(backupPieces, pieces);
        LaunchGame();
    }

    public void LaunchGame() {
        GameThread = new Thread(this);
        GameThread.start();
    }

    public void setPieces() {
        //WHITE PIECES
        backupPieces.add(new Pawn(WHITE, 0, 6));
        backupPieces.add(new Pawn(WHITE, 1, 6));
        backupPieces.add(new Pawn(WHITE, 2, 6));
        backupPieces.add(new Pawn(WHITE, 3, 6));
        backupPieces.add(new Pawn(WHITE, 4, 6));
        backupPieces.add(new Pawn(WHITE, 5, 6));
        backupPieces.add(new Pawn(WHITE, 6, 6));
        backupPieces.add(new Pawn(WHITE, 7, 6));
        backupPieces.add(new Rook(WHITE, 0, 7));
        backupPieces.add(new Rook(WHITE, 7, 7));
        backupPieces.add(new Knight(WHITE, 1, 7));
        backupPieces.add(new Knight(WHITE, 6, 7));
        backupPieces.add(new Bishop(WHITE, 2, 7));
        backupPieces.add(new Bishop(WHITE, 5, 7));
        backupPieces.add(new Queen(WHITE, 3, 7));
        backupPieces.add(new King(WHITE, 4, 7));
        //BLACK PIECES
        backupPieces.add(new Pawn(BLACK, 0, 1));
        backupPieces.add(new Pawn(BLACK, 1, 1));
        backupPieces.add(new Pawn(BLACK, 2, 1));
        backupPieces.add(new Pawn(BLACK, 3, 1));
        backupPieces.add(new Pawn(BLACK, 4, 1));
        backupPieces.add(new Pawn(BLACK, 5, 1));
        backupPieces.add(new Pawn(BLACK, 6, 1));
        backupPieces.add(new Pawn(BLACK, 7, 1));
        backupPieces.add(new Rook(BLACK, 0, 0));
        backupPieces.add(new Rook(BLACK, 7, 0));
        backupPieces.add(new Knight(BLACK, 1, 0));
        backupPieces.add(new Knight(BLACK, 6, 0));
        backupPieces.add(new Bishop(BLACK, 2, 0));
        backupPieces.add(new Bishop(BLACK, 5, 0));
        backupPieces.add(new Queen(BLACK, 3, 0));
        backupPieces.add(new King(BLACK, 4, 0));

        //TEST Castling
        /*   backupPieces.add(new King(WHITE, 4, 4));
        backupPieces.add(new Rook(WHITE, 0, 4));
        backupPieces.add(new Rook(WHITE, 7, 4));
        backupPieces.add(new King(BLACK, 4, 3));
        backupPieces.add(new Rook(BLACK, 0, 3));
        backupPieces.add(new Rook(BLACK, 7, 3));*/
    }

    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target) {
        target.clear();
        target.addAll(source);
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (GameThread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {

        if (promotion) {
            promoting();
        } else if(gameOver == false && stalemate == false) {
            //MOUSE PRESSED
            if (mouse.pressed) {
                if (activePiece == null) {
                    //Check if you can select a piece
                    for (Piece piece : pieces) {
                        //Check if the piece is the same color as the current player
                        if (piece.color == currentColor &&
                                piece.col == mouse.x / Board.SQUARE_SIZE &&
                                piece.row == mouse.y / Board.SQUARE_SIZE) {
                            activePiece = piece;
                        }
                    }
                } else {
                    //If player holds piece simulate movement
                    simulate();
                }
            }

            //MOUSE RELEASED
            if (mouse.pressed == false) {

                if (activePiece != null) {

                    if (validSquare) {

                        //MOVE CONFIRMED

                        //Update Piece list in case a piece has been captured and removed during simulation
                        copyPieces(pieces, backupPieces);
                        activePiece.updatePosition();
                        if (castlingPiece != null) {
                            castlingPiece.updatePosition();
                        }

                        if (isKingInCheck() && isCheckMate()) {
                            gameOver = true;

                        } else if (isStalemate() && isKingInCheck() == false) {
                            stalemate = true;

                        } else {
                             if (canPromote()) {
                                promotion = true;
                            } else {
                                changePlayer();
                            }
                        }
                    } else {
                        //The piece was not moved to a valid square
                        copyPieces(backupPieces, pieces);
                        activePiece.resetPosition();
                        activePiece = null;
                    }
                }
            }
        }
    }

    private void simulate() {

        canMove = false;
        validSquare = false;

        //Reset the piece list in every loop
        copyPieces(backupPieces, pieces);

        //Reset the castling piece's position
        if (castlingPiece != null) {
            castlingPiece.col = castlingPiece.prevCol;
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
            castlingPiece = null;
        }

        activePiece.x = mouse.x - Board.HALF_SQUARE_SIZE;
        activePiece.y = mouse.y - Board.HALF_SQUARE_SIZE;
        activePiece.col = activePiece.getCol(activePiece.x);
        activePiece.row = activePiece.getRow(activePiece.y);

        //Check if the piece can move to the square
        if (activePiece.canMove(activePiece.col, activePiece.row)) {
            canMove = true;

            //If hitting piece, remove it from the list
            if (activePiece.hittingPiece != null) {
                pieces.remove(activePiece.hittingPiece.getIndex());

            }
            checkCastling();

            if (isIllegal(activePiece) == false && opponentCanCaptureKing() == false) {
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king) {
        if (king.type == Type.KING) {
            for (Piece piece : pieces) {
                if (piece != king && piece.color != king.color && piece.canMove(king.col, king.row)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isKingInCheck() {

        Piece king = getKing(true);

        if (activePiece.canMove(king.col, king.row)) {
            checkingPiece = activePiece;
            return true;
        } else {
            checkingPiece = null;
        }
        return false;
    }

    private boolean opponentCanCaptureKing() {

        Piece king = getKing(false);

        for (Piece piece : pieces) {
            if (piece.color != king.color && piece.canMove(king.col, king.row)) {
                return true;
            }
        }
        return false;
    }

    private Piece getKing(boolean opponent) {
        Piece king = null;

        for (Piece piece : pieces) {
            if (opponent) {
                if (piece.type == Type.KING && piece.color != currentColor) {
                    king = piece;
                }
            } else {
                if (piece.type == Type.KING && piece.color == currentColor) {
                    king = piece;
                }
            }
        }
        return king;
    }

    private boolean isCheckMate() {
        Piece king = getKing(true);

        if (kingCanMove(king)) {
            return false;
        } else {
            //Check if you can block the attack

            //Check positions of the checking piece and the king in check
            int colDiff = Math.abs(checkingPiece.col - king.col);
            int rowDiff = Math.abs(checkingPiece.row - king.row);

            if (colDiff == 0) {
                //The checking piece is attacking vertically
                if (checkingPiece.row > king.row) {
                    //The checking piece is above the king
                    for (int row = checkingPiece.row; row < king.row; row++) {
                        for (Piece piece : pieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)) {
                                return false;
                            }
                        }
                    }
                }
                if (checkingPiece.row < king.row) {
                    //The checking piece is below the king
                    for (int row = checkingPiece.row; row > king.row; row--) {
                        for (Piece piece : pieces) {
                            if (piece != king && piece.color != currentColor && piece.canMove(checkingPiece.col, row)) {
                                return false;
                            }
                        }
                    }
                }


                if (rowDiff == 0) {
                    //The checking piece is attacking horizontally
                    if (checkingPiece.col < king.col) {
                        //The checking piece is on the left side of the king
                        for (int col = checkingPiece.col; col < king.col; col++) {
                            for (Piece piece : pieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)) {
                                    return false;
                                }
                            }
                        }
                    }
                    if (checkingPiece.col > king.col) {
                        //The checking piece is on the right side of the king
                        for (int col = checkingPiece.col; col > king.col; col--) {
                            for (Piece piece : pieces) {
                                if (piece != king && piece.color != currentColor && piece.canMove(col, checkingPiece.row)) {
                                    return false;
                                }
                            }
                        }
                    }
                } else if (colDiff == rowDiff) {
                    //The checking piece is attacking diagonally
                    if (checkingPiece.row < king.row) {
                        //The checking piece is above the king
                        if (checkingPiece.col < king.col) {
                            //The checking piece is in the upper left
                            for (int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row++) {
                                for (Piece piece : pieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        if (checkingPiece.col > king.col) {
                            //The checking piece is in the upper right
                            for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row++) {
                                for (Piece piece : pieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    if (checkingPiece.row > king.row) {
                        //The checking piece is below the king
                        if (checkingPiece.col < king.col) {
                            //The checking piece is in the lower left
                            for (int col = checkingPiece.col, row = checkingPiece.row; col < king.col; col++, row--) {
                                for (Piece piece : pieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        if (checkingPiece.col > king.col) {
                            //The checking piece is in the lower right
                            for (int col = checkingPiece.col, row = checkingPiece.row; col > king.col; col--, row--) {
                                for (Piece piece : pieces) {
                                    if (piece != king && piece.color != currentColor && piece.canMove(col, row)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
    }

    private boolean kingCanMove(Piece king) {

        //Simulate if there is any square where king can move to
        if(isValidMove(king, -1, -1)) { return true;}
        if(isValidMove(king, 0, -1)) { return true;}
        if(isValidMove(king, 1, -1)) { return true;}
        if(isValidMove(king, -1, 0)) { return true;}
        if(isValidMove(king, 1, 0)) { return true;}
        if(isValidMove(king, -1, 1)) { return true;}
        if(isValidMove(king, 0, 1)) { return true;}
        if(isValidMove(king, 1, 1)) { return true;}
        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus) {
        boolean isValidMove = false;

        //update the king's position for a second
        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)) {
            if(king.hittingPiece != null) {
                pieces.remove(king.hittingPiece.getIndex());
            }
            if(isIllegal(king) == false) {
                isValidMove = true;
            }
        }
        //Reset the king's position
        king.resetPosition();
        copyPieces(backupPieces, pieces);
        return isValidMove;
    }

    private boolean isStalemate() {

        int count = 0;
        //Count the number of pieces
        for(Piece piece : pieces) {
            if(piece.color != currentColor) {
                count++;
            }
        }

        //If there are only piece left it is a king
        if(count == 1) {
            if(kingCanMove(getKing(true)) == false) {
                return true;
            }
        }


        return false;
    }

    private void checkCastling() {

        if (castlingPiece != null) {
            if (castlingPiece.col == 0) {
                castlingPiece.col += 3;
            } else if (castlingPiece.col == 7) {
                castlingPiece.col -= 2;
            }
            castlingPiece.x = castlingPiece.getX(castlingPiece.col);
        }
    }

    private void changePlayer() {
        if (currentColor == WHITE) {
            currentColor = BLACK;
            //Reset Two stepped status
            for (Piece piece : backupPieces) {
                if (piece.color == BLACK) {
                    piece.twoStepped = false;
                }
            }
        } else {
            currentColor = WHITE;
            //Reset Two stepped status
            for (Piece piece : backupPieces) {
                if (piece.color == WHITE) {
                    piece.twoStepped = false;
                }
            }
        }
        activePiece = null;
    }

    private boolean canPromote() {
        if (activePiece.type == Type.PAWN) {
            if (currentColor == WHITE && activePiece.row == 0 || currentColor == BLACK && activePiece.row == 7) {
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor, 9, 2));
                promoPieces.add(new Knight(currentColor, 9, 3));
                promoPieces.add(new Bishop(currentColor, 9, 4));
                promoPieces.add(new Queen(currentColor, 9, 5));
                return true;
            }
        }
        return false;
    }

    private void promoting() {
        if (mouse.pressed) {
            for (Piece piece : promoPieces) {
                if (piece.col == mouse.x / Board.SQUARE_SIZE && piece.row == mouse.y / Board.SQUARE_SIZE) {
                    switch (piece.type) {
                        case ROOK:
                            pieces.add(new Rook(currentColor, activePiece.col, activePiece.row));
                            break;
                        case KNIGHT:
                            pieces.add(new Knight(currentColor, activePiece.col, activePiece.row));
                            break;
                        case BISHOP:
                            pieces.add(new Bishop(currentColor, activePiece.col, activePiece.row));
                            break;
                        case QUEEN:
                            pieces.add(new Queen(currentColor, activePiece.col, activePiece.row));
                        default:
                            break;
                    }
                    pieces.remove(activePiece.getIndex());
                    copyPieces(pieces, backupPieces);
                    activePiece = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        board.drawBoard(g2);

        for (Piece piece : backupPieces) {
            piece.drawPiece(g2);
        }

        if (activePiece != null) {
            if (canMove) {
                if (isIllegal(activePiece) || opponentCanCaptureKing()) {
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                } else {
                    g2.setColor(Color.green);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
                    g2.fillRect(activePiece.col * Board.SQUARE_SIZE, activePiece.row * Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
            }

            activePiece.drawPiece(g2);
        }

        //STATUS BAR
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Italic", Font.PLAIN, 40));
        g2.setColor(Color.white);

        if (promotion) {
            g2.drawString("Promote to:", 840, 150);
            for (Piece piece : promoPieces) {
                g2.drawImage(piece.image, piece.getX(piece.col), piece.getY(piece.row),
                        Board.SQUARE_SIZE, Board.SQUARE_SIZE, null);
            }
        } else {
            if (currentColor == WHITE) {
                g2.drawString("White's turn", 840, 550);
                if (checkingPiece != null && checkingPiece.color == BLACK) {
                    g2.setColor(Color.red);
                    g2.drawString("The king", 840, 650);
                    g2.drawString("Is in check!", 840, 700);
                }
            } else {
                g2.drawString("Black's turn", 840, 250);
                if (checkingPiece != null && checkingPiece.color == WHITE) {
                    g2.setColor(Color.red);
                    g2.drawString("The king", 840, 100);
                    g2.drawString("Is in check!", 840, 150);
                }
            }
        }
        if(gameOver) {
            String winner = "";
            if(currentColor == WHITE) {
                winner = "White wins";
            } else {
                winner = "Black wins";
            }
            g2.setFont(new Font("Italic", Font.PLAIN, 90));
            g2.setColor(Color.green);
            g2.drawString(winner, 200, 420);
        }

        if(stalemate) {
            g2.setFont(new Font("Italic", Font.PLAIN, 90));
            g2.setColor(Color.orange);
            g2.drawString("Stalemate", 200, 420);
        }
    }
}
