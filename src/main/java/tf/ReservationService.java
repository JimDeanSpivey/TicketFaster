package tf;

import tf.seats.Seat;

import java.util.Map;

/**
 * @author Jimmy Spivey
 */
public class ReservationService {

    private Map<String,Reservation> reservations;

    public int seatsReserved() {
        return 0;
    }

    public boolean isReserved(Seat seat) {
        return false;
    }
}
