package piece;

import main.GamePanel;
import main.Type;

public class Pawn extends Piece {

    public Pawn(int color, int col, int row) {
        super(color, col, row);

        type = Type.PAWN;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/white_pawn");
        } else {
            image = getImage("/piece/black_pawn");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            //Define move value based on its color
            int moveValue;
            if(color == GamePanel.WHITE) {
                moveValue = -1;
            } else {
                moveValue = 1;
            }

            //Check hitting piece
            hittingPiece = getHittingPiece(targetCol, targetRow);

            //1 square movement
            if(targetCol == prevCol && targetRow == prevRow + moveValue && hittingPiece == null) {
                return true;
            }

            //2 square movement
            if(targetCol == prevCol && targetRow == prevRow + moveValue*2 && hittingPiece == null &&
                    moved == false && pieceOnStraightLine(targetCol, targetRow) == false) {
                return true;
            }

            //Diagonal movement & capture
            if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + moveValue && hittingPiece != null
                    && hittingPiece.color != color) {
                return true;
            }

            //En Passant
            if(Math.abs(targetCol - prevCol) == 1 && targetRow == prevRow + moveValue) {
                for(Piece piece : GamePanel.pieces) {
                    if(piece.col == targetCol && piece.row == prevRow && piece.twoStepped == true) {
                        hittingPiece = piece;
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
