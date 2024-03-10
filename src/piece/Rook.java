package piece;

import main.GamePanel;
import main.Type;

public class Rook extends Piece{

    public Rook(int color, int col, int row) {
        super(color, col, row);

        type = Type.ROOK;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/white_rook");
        } else {
            image = getImage("/piece/black_rook");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            // Rook can move if square is in the same column or row
            if(targetCol == prevCol || targetRow == prevRow) {
                if(isValidSquare(targetCol, targetRow) && pieceOnStraightLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }
        return false;
    }
}
