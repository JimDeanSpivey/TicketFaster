package tf.services;

import tf.seats.SeatRange;

import java.util.List;
import java.util.Set;
/**
 * @author Jimmy Spivey
 */
public interface SeatAvailabilityService {
    int takenSeats();

    void reserveSeatRanges(Set<SeatRange> seats);

    SeatRange find(int y, int numSeats, int seatsInRow);

    List<SeatRange> getAvailable(int y, int seatsInRow);

    void freeSeatRanges(Set<SeatRange> held);
}
