package tf.seats;

/**
 * @author Jimmy Spivey
 */
public class RectangularSeatNaming implements SeatNaming {
    @Override
    public String getName(int row, int col) {
        String colName = "";
        int charValue = col % 26;
        if (charValue == 0) {
            charValue = 26; // Handle Z
        }
        charValue += 64; //add ASCII upper case start to offset
        String character = Character.toString((char)charValue);
        for (int i = 0; i < col; i += 26) {
             colName += character;
        }
        return row+colName;
    }
}
