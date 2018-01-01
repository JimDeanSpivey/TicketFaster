package tf.services;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import tf.seats.Seat;
import tf.seats.SeatHold;
import tf.seats.SeatRange;
import tf.seats.Stadium;
import tf.services.helpers.LeftOrRight;
import tf.util.RandomIterator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jimmy Spivey
 */
public class TicketFaster implements TicketService {

    private Stadium stadium;
    private SeatHoldService seatHoldService;
    private ReservationService reservationService;
    private SeatAvailabilityService seatAvailabilityService;
    private int expirySeconds;
    private LeftOrRight leftOrRight;

    public TicketFaster(Stadium stadium, SeatHoldService seatHoldService,
                        ReservationService reservationService,
                        SeatAvailabilityService seatAvailabilityService,
                        int expirySeconds, LeftOrRight leftOrRight) {
        this.stadium = stadium;
        this.seatHoldService = seatHoldService;
        this.reservationService = reservationService;
        this.seatAvailabilityService = seatAvailabilityService;
        this.expirySeconds = expirySeconds;
        this.leftOrRight = leftOrRight;
    }

    @Override
    public int numSeatsAvailable() {
        return stadium.totalSeats() - seatAvailabilityService.takenSeats();
    }

    @Override
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        if (numSeats > numSeatsAvailable()) {
            throw new IllegalStateException(String.format(
                    "Unable to reserve %d seats. Only %d seats available.",
                    numSeats, numSeatsAvailable())
            );
        }
        Set<SeatRange> seats = findSeats(numSeats);
        seatAvailabilityService.holdSeats(seats);
        SeatHold seatHold = new SeatHold(
                seatHoldService.generateId(),
                seats,
                customerEmail,
                DateTime.now().plusSeconds(expirySeconds).toDate()
        );
        seatHoldService.hold(seatHold);
        return seatHold;
    }

    /**
     * Finds the most preferred seats.
     * Seats are seen as most desirable in this order: how close the row is to
     * the stage, if this is the seat in the middle of the row or closer to it
     * than the others.
     * Seatholds are seen as most desirable if they can accommodate the entire
     * party adjacently in a single row, searching for the most preferable
     * seats first. If not, first come first serve of just the seats closest to
     * the stage. If those seats have any adjacent seats those are included so
     * the seat hold could be a combination of multiple adjacent seats and
     * non-adjacently reserved seats.
     * There is no preference to middle seats if it a single seat hold, or if
     * it isn't possible to find an entirely adjacent set of seats.
     *
     * @param numSeats
     * @return
     */
    private Set<SeatRange> findSeats(int numSeats) {
        Seat[][] seats = stadium.asArrays(); //TODO: can move this to abstract scoring and this impl doesn't need to know about the rectangular array concern
        int seatsInRow = seats[0].length;
        //Try to hold a set of seats all adjacent first
        if (numSeats <= seatsInRow && numSeats > 1) { // groups of 2 or more get priority to middle seats
            for (int y = 1; y <= seats.length; y++) {
                SeatRange seatRange = seatAvailabilityService.find(y, numSeats, seatsInRow);
                if (seatRange != null) {
                    Set<SeatRange> result = new HashSet<>();
                    partionRange(numSeats, result, seatRange.getStart(), seatRange.getEnd());
                    return result;
//                    return ImmutableSet.of(seatRange);
                }
            }
        }
        //If unable to find adjacents, find first available
        int remaining = numSeats;
        Set<SeatRange> firstAvailable = new HashSet<>();
        for (int y = 1; y <= seats.length; y++) {
            List<SeatRange> available = seatAvailabilityService.getAvailable(y, seatsInRow);
            //Fill each available seat range randomly (as opposed to iterating left to right)
            RandomIterator<SeatRange> randomIterator = new RandomIterator<>(available);
            while (randomIterator.hasNext()) {
                SeatRange range = randomIterator.next();
                Seat start = range.getStart();
                Seat end = range.getEnd();
                int size = end.getCol() - start.getCol() + 1;
                if (size <= remaining) { // Just get entire range
                    firstAvailable.add(range);
                    remaining -= size;
                } else { // Partition range
                    // Do not prefer leftside or rightside seats, fill evenly
                    partionRange(remaining, firstAvailable, start, end);
                    return firstAvailable;
                }
                if (remaining == 0) {
                    return  firstAvailable;
                }
            }
        }
        return firstAvailable;
    }

    private void partionRange(int remaining, Set<SeatRange> ranges, Seat start, Seat end) {
        if (leftOrRight.choose()) {
            ranges.add(
                new SeatRange(
                    start,
                    stadium.getSeat(start.getRow(), start.getCol()+remaining-1))
            );
        } else {
            ranges.add(
                new SeatRange(
                    stadium.getSeat(end.getRow(), end.getCol()-remaining+1),
                    end)
            );
        }
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        validateSeatHoldId(seatHoldId);
        SeatHold hold = seatHoldService.getSeatHold(seatHoldId);
        return reservationService.reserve(hold);
    }

    private void validateSeatHoldId(int seatHoldId) {
        SeatHold hold = seatHoldService.getSeatHold(seatHoldId);
        if (hold == null) {
            throw new IllegalArgumentException(
                    String.format("Seat hold id [%d] not found", seatHoldId)
            );
        }
        if (seatHoldService.hasExpired(seatHoldId)) {
            String duration = DurationFormatUtils.formatDurationHMS(
                    new Date().getTime() - hold.getExpiration().getTime());
            String formated = String.format(
                    "Seat hold id [%d] has expired by %s",
                    seatHoldId, duration

            );
            throw new IllegalArgumentException(formated);
        }
        if (reservationService.isReserved(hold)) {
            throw new IllegalArgumentException(String.format(
                    "Seat hold id [%d] is already reserved", seatHoldId
            ));
        }
    }

    /**
     * Added for unit testing ease.
     * @return
     */
    public SeatHoldService getSeatHoldService() {
        return this.seatHoldService;
    }
}
