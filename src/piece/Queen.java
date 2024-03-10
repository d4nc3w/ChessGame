package piece;

import main.GamePanel;
import main.Type;

public class Queen extends Piece {

    public Queen(int color, int col, int row) {
        super(color, col, row);

        type = Type.QUEEN;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/white_queen");
        } else {
            image = getImage("/piece/black_queen");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {

            //Vertical & Horizontal
            if(targetCol == prevCol || targetRow == prevRow) {
                if(isValidSquare(targetCol, targetRow) && pieceOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }

            //Diagonal
            if(Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow)) {
                if(isValidSquare(targetCol, targetRow) && pieceOnDiagonalLine(targetCol, targetRow) == false) {
                    return true;
                }
            }

        }

        return false;
    }
}
