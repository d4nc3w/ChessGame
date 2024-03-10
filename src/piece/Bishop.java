package piece;

import main.GamePanel;
import main.Type;

public class Bishop extends Piece {

    public Bishop(int color, int col, int row) {
        super(color, col, row);

        type = Type.BISHOP;

        if(color == GamePanel.WHITE) {
            image = getImage("/piece/white_bishop");
        } else {
            image = getImage("/piece/black_bishop");
        }
    }

    public boolean canMove(int targetCol, int targetRow) {

        if(isWithinBoard(targetCol, targetRow) && isSameSquare(targetCol, targetRow) == false) {
            //Ratio must be 1:1
            if(Math.abs(targetCol - prevCol) == Math.abs(targetRow - prevRow)) {
                if(isValidSquare(targetCol, targetRow) && pieceOnDiagonalLine(targetCol, targetRow) == false) {
                    return true;
                }
            }
        }
        return false;
    }
}
