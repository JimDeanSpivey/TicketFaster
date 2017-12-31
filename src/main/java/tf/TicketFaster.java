package tf;

import com.google.common.collect.ImmutableSet;
import tf.seats.Seat;
import tf.seats.SeatRange;
import tf.seats.Stadium;

import java.util.*;

/**
 * @author Jimmy Spivey
 */
public class TicketFaster implements TicketService {

    private Stadium stadium;
    private SeatHoldService seatHoldService;
    private ReservationService reservationService;
    private AdjacentSeatService adjacentSeatService;

    @Override
    public int numSeatsAvailable() { // better to just calculate on the fly instead of keep a counter and have to deal with syncing state
        return stadium.totalSeats() - reservationService.seatsReserved()
                - seatHoldService.seatsHeldCount();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        //first verify enough seats can be held.
        if (numSeats > numSeatsAvailable()) {
            throw new IllegalArgumentException(String.format(
                    "Unable to reserver %d seats. Only %d seats available.",
                    numSeats, numSeatsAvailable())
            );
        }
        //Search to find seats closest to stadium first. And search from middle col first.
        Seat cloest = findFirstClosest();
        //see if seats can be filled left or right.

        //keep searching until able to find consequtive, then fallback to first available
        //add seathold to queue and map and return it
    }

    private synchronized Set<SeatRange> allocateSeats(int numSeats) {
        //find needs to be synchronized and that method needs to also assign the seats too
        //keep finding seats. Fill up a list of seats found, but also keep looking for
        // adjacent seats (only if the group size is less than the row size). (actually
        // look for adjacent seats right there, if not found add that seat to the list).
        Seat[][] seats = stadium.asArrays();
        int seatsInRow = seats[0].length;
        if (numSeats <= seatsInRow && numSeats > 1) {
            for (int y = 0; y < seats.length; y++) {
                SeatRange seatRange = adjacentSeatService.find(y, numSeats, seatsInRow);
                if (seatRange != null) {
                    return ImmutableSet.of(seatRange);
                }
            }
        }
        //If unable to find adjacents, find first available
        int firstFound = 0;
        Set<SeatRange> firstAvailable = new HashSet<>();
        for (int y = 0; y < seats.length; y++) {
            List<SeatRange> available = adjacentSeatService.getAvailable(y, seatsInRow);
            //Fill each available seat range in.

        }

        return firstAvailable;
    }

    private boolean isAvailable(Seat seat) {
        return !seatHoldService.isHeld(seat) && !reservationService.isReserved(seat);
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        throw new UnsupportedOperationException("#reserveSeats()");
    }

}
