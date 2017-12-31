package tf.seats;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Jimmy Spivey
 */
public class RectangularSeatingStadium implements Stadium {

    private Seat[][] seats;
    private int width;
    private int length;

    private RectangularSeatingStadium(int width, int length, Seat[][] seats) {
        this.width = width;
        this.length = length;
        this.seats = seats;
    }

    public static class Factory {
        public Stadium getInstance(int width, int length, int seatLength, SeatNaming seatNaming) {
            Seat[][] seats = IntStream.range(1, length+1)
                    .mapToObj(x -> IntStream.range(1, width+1)
                    .mapToObj(y ->
                        new Seat(seatNaming.getName(x, y), seatLength*x, x, y)
                    ).toArray(Seat[]::new)).toArray(Seat[][]::new);
//            Seat[] seats = IntStream.range(1, size+1).boxed().map(i -> {
//                int row = (int) Math.ceil((double)i / width);
//                int col = i % width;
//                if (col == 0) {
//                    col = width;
//                }
//                int cmFromStage = seatLength * row;
//                String name = seatNaming.getName(row, col);
//                return new Seat(name, cmFromStage, row, col);
//            }).toArray(s -> new Seat[s]);
            return new RectangularSeatingStadium(width, length, seats);
        }
    }

    private static final Pattern COL_NAME_PATTERN = Pattern.compile("([0-9])+([A-Z]+)");
    @Override
    public Seat getSeat(String id) {
        Matcher m = COL_NAME_PATTERN.matcher(id);
        m.find();
        String colName = m.group(2);
        int row = Integer.parseInt(m.group(1)) -1;
        int charValue = (int)colName.charAt(0) - 65;
        int col = (26*(colName.length()-1)) + charValue;
        return seats[row][col];
//        return seats[((row * width)-width)+col];
    }

}
