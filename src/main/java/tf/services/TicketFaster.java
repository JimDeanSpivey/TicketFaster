package tf.services;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import tf.util.RandomIterator;
import tf.seats.SeatHold;
import tf.seats.Seat;
import tf.seats.SeatRange;
import tf.seats.Stadium;

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

    public TicketFaster(Stadium stadium, SeatHoldService seatHoldService,
                        ReservationService reservationService,
                        SeatAvailabilityService seatAvailabilityService,
                        int expirySeconds) {
        this.stadium = stadium;
        this.seatHoldService = seatHoldService;
        this.reservationService = reservationService;
        this.seatAvailabilityService = seatAvailabilityService;
        this.expirySeconds = expirySeconds;
    }

    @Override
    public int numSeatsAvailable() {
        return stadium.totalSeats() - seatAvailabilityService.takenSeats();
    }

    @Override
    public synchronized SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        if (numSeats > numSeatsAvailable()) {
            throw new IllegalArgumentException(String.format(
                    "Unable to reserve %d seats. Only %d seats available.",
                    numSeats, numSeatsAvailable())
            );
        }
        Set<SeatRange> seats = findSeats(numSeats);
        seatAvailabilityService.reserveSeatRanges(seats);
        SeatHold seatHold = new SeatHold(
                seatHoldService.generateId(),
                seats,
                customerEmail,
                DateTime.now().plusSeconds(expirySeconds).toDate()
        );
        seatHoldService.hold(seatHold);
        return seatHold;
    }

    private synchronized Set<SeatRange> findSeats(int numSeats) {
        Seat[][] seats = stadium.asArrays(); //TODO: can move this to abstract scoring and this impl doesn't need to know about the rectangular array concern
        int seatsInRow = seats[0].length;
        //Try to hold a set of seats all adjacent first
        if (numSeats <= seatsInRow && numSeats > 1) {
            for (int y = 0; y < seats.length; y++) {
                SeatRange seatRange = seatAvailabilityService.find(y, numSeats, seatsInRow);
                if (seatRange != null) {
                    return ImmutableSet.of(seatRange);
                }
            }
        }
        //If unable to find adjacents, find first available
        int remaining = numSeats;
        Set<SeatRange> firstAvailable = new HashSet<>();
        for (int y = 0; y < seats.length; y++) {
            List<SeatRange> available = seatAvailabilityService.getAvailable(y, seatsInRow);
            //Fill each available seat range in with no preference to left or right.
            RandomIterator<SeatRange> randomIterator = new RandomIterator<>(available);
            while (randomIterator.hasNext()) {
                SeatRange range = randomIterator.next();
                Seat start = range.getStart();
                Seat end = range.getEnd();
                int size = end.getCol() - start.getCol() + 1;
                if (size <= remaining) {
                    firstAvailable.add(range);
                    remaining -= size;
                } else {
                    if (Math.random() < .5) { // Do not prefer left or right
                        firstAvailable.add(
                            new SeatRange(
                                start,
                                stadium.getSeat(start.getRow(), start.getCol()+remaining))
                        );
                    } else {
                        firstAvailable.add(
                            new SeatRange(
                                stadium.getSeat(end.getRow(), end.getCol()-remaining),
                                end)
                        );
                    }
                }
                if (remaining == 0) {
                    break;
                }
            }
        }
        return firstAvailable;
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
                    "Seathold id [%d] has expired by %s",
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

}
