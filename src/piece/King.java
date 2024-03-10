package piece;

import main.GamePanel;
import main.Type;

public class King extends Piece {

    public King(int color, int col, int row) {
        super(color, col, row);

        type = Type.KING;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/white_king");
        } else {
            image = getImage("/piece/black_king");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {
        if(isWithinBoard(targetCol, targetRow)) {

            //MOVEMENT
            if(Math.abs(targetCol - prevCol) + Math.abs(targetRow - prevRow) == 1 ||
            Math.abs(targetCol - prevCol) * Math.abs(targetRow - prevRow) == 1) {

                if(isValidSquare(targetCol, targetRow)) {
                    return true;
                }
            }

            //CASTLING
            if(moved == false) {

                //Smaller castling
                if(targetCol == prevCol+2 && targetRow == prevRow && pieceOnStraightLine(targetCol, targetRow) == false) {
                    for(Piece piece : GamePanel.pieces) {
                        if(piece.col == prevCol+3 && piece.row == prevRow && piece.moved == false) {
                            GamePanel.castlingPiece = piece;
                            return true;
                        }
                    }
                }

                //Bigger castling
                if(targetCol == prevCol-2 && targetRow == prevRow && pieceOnStraightLine(targetCol, targetRow) == false) {
                    Piece p[] = new Piece[2];
                    for(Piece piece : GamePanel.pieces) {
                        if(piece.col == prevCol-3 && piece.row == prevRow) {
                            p[0] = piece;
                        }
                        if(piece.col == prevCol-4 && piece.row == prevRow) {
                            p[1] = piece;
                        }

                        if(p[0] == null && p[1] != null && p[1].moved == false) {
                            GamePanel.castlingPiece = p[1];
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }
}
