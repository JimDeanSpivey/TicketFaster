package tf.seats;

import com.google.common.base.Objects;

/**
 * @author Jimmy Spivey
 */
public class SeatRange implements Comparable<SeatRange> {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SeatRange seatRange = (SeatRange) o;
        return Objects.equal(this.start, seatRange.start) &&
                Objects.equal(this.end, seatRange.end);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.start, this.end);
    }

    @Override
    public int compareTo(SeatRange o) {
        int thisStart = this.getStart().getCol();
        int incEnd = o.getEnd().getCol();
        return thisStart - incEnd;
    }
}
