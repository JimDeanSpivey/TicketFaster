package tf.test.util;

import com.google.common.collect.ImmutableSet;
import tf.seats.Reservation;
import tf.seats.Seat;
import tf.seats.SeatHold;
import tf.seats.SeatRange;

import java.util.Date;
import java.util.Set;

/**
 * @author Jimmy Spivey
 */
public class Generation {

    public static Reservation reservation(int id, String code) {
        Seat seat = seat(99, 99); //reservation doesn't reference these
        Set<SeatRange> range = ImmutableSet.of(new SeatRange(seat, seat));
        SeatHold hold = hold(id, range);
        return new Reservation(code, hold);
    }

    private static Seat seat(int row, int col) {
        return new Seat("UNITTEST", 100, row, col);
    }

    public static Set<SeatRange> range(int row, int from, int to) {
        Seat start = seat(row, from);
        Seat end = seat(row, to);
        return ImmutableSet.of(new SeatRange(start, end));
    }

    public static SeatHold hold(int id, Set<SeatRange> range) {
        return new SeatHold(id, range, "UNIT@TEST.com", new Date());
    }

}
