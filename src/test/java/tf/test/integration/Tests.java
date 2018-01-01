package tf.test.integration;

import org.junit.Assert;
import org.junit.Test;
import tf.seats.*;
import tf.services.*;
import tf.util.AlwaysLeft;

import java.security.SecureRandom;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Jimmy Spivey
 */
public class Tests {

    public static final int ROW_LENGTH = 100;
    public static final Stadium STADIUM = new RectangularSeatingStadium.Factory().getInstance(
            ROW_LENGTH, 20, 100, new RectangularSeatNaming()
    );
    public static final AdjacentlyAvailableSeatsService AVAILABILITY_SERVICE = new AdjacentlyAvailableSeatsService(STADIUM);
    public static final SeatHoldService SEAT_HOLD_SERVICE = new SeatHoldService(
            AVAILABILITY_SERVICE,
            new RandomStringService(
                    7,
                    new SecureRandom(),
                    RandomStringService.digits));
    public static final String EMAIL = "UNIT@TEST.com";

    @Test
    public void testBasics() {
        TicketService service = getService();
        assertEquals(STADIUM.totalSeats(), service.numSeatsAvailable());
        try {
            service.reserveSeats(9999, EMAIL);
            fail();
        } catch (IllegalArgumentException e) {
        }

        SeatHold hold;
        String code;
        hold = service.findAndHoldSeats(1, EMAIL);
        assertContains(hold, 1, 1, 1);
        code = service.reserveSeats(hold.getId(), hold.getEmail());
        assertEquals(5, code.length());
        try { // duplicate reservation
            code = service.reserveSeats(hold.getId(), hold.getEmail());
            fail();
        } catch (IllegalArgumentException e) {
        }
        hold = service.findAndHoldSeats(1, EMAIL);
        assertContains(hold,1, 2, 2);
        SEAT_HOLD_SERVICE.unholdSeats(); // Test unable to reserve expired seats
        try {
            code = service.reserveSeats(hold.getId(), hold.getEmail());
            fail();
        } catch (IllegalArgumentException e) {
        }

    }

    private TicketFaster getService() {
        return new TicketFaster(
                STADIUM,
                SEAT_HOLD_SERVICE,
                new ReservationService(new RandomStringService(5)),
                AVAILABILITY_SERVICE,
                -99999,
                new AlwaysLeft()
        );
    }

    private void assertContains(SeatHold hold, int row, int from, int to) {
        SeatRange range = new SeatRange(
                STADIUM.getSeat(row, from),
                STADIUM.getSeat(row, to)
        );
        Assert.assertTrue(hold.getHeld().contains(range));
    }

    private void assertOpen(SeatRange open, int from, int to) {
        assertEquals(from, open.getStart().getCol());
        assertEquals(to, open.getEnd().getCol());
    }

    private void assertRandomOpen(SeatRange open, int from1, int from2, int to1, int to2) {
        int start = open.getStart().getCol();
        int end = open.getEnd().getCol();
        System.out.println("Start: " + start);
        System.out.println("End: " + end);
        assertTrue(
                (from1 == start && to1 == end)
                        || (from2 == start && to2 == end)
        );
    }
}
