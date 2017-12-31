package tf.seats;

/**
 * @author Jimmy Spivey
 */
public class SeatRange {

    private Seat start;
    private Seat end;

    public SeatRange(Seat single) {
        this.start = single;
        this.end = single;
    }

    public SeatRange(Seat start, Seat end) {
        this.start = start;
        this.end = end;
    }

    public Seat getStart() {
        return this.start;
    }

    public Seat getEnd() {
        return this.end;
    }
}
