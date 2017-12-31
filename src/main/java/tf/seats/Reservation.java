package tf.seats;

import com.google.common.base.Objects;

/**
 * @author Jimmy Spivey
 */
public class Reservation {

    private String confirmationCode;
    private SeatHold seatHold;

    public Reservation(String confirmationCode, SeatHold seatHold) {
        this.confirmationCode = confirmationCode;
        this.seatHold = seatHold;
    }

    public String getConfirmationCode() {
        return this.confirmationCode;
    }

    public SeatHold getSeatHold() {
        return this.seatHold;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return Objects.equal(this.confirmationCode, that.confirmationCode) &&
                Objects.equal(this.seatHold, that.seatHold);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.confirmationCode, this.seatHold);
    }
}
