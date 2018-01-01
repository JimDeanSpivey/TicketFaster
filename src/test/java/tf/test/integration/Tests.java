package tf.test.integration;

import org.junit.Assert;
import org.junit.Test;
import tf.seats.*;
import tf.services.*;
import tf.services.helpers.LeftOrRight;
import tf.services.helpers.RandomLeftOrRight;
import tf.test.util.AlwaysLeft;

import java.security.SecureRandom;
import java.util.concurrent.ThreadLocalRandom;

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
    public static final String EMAIL = "UNIT@TEST.com";

    @Test
    public void testBasics() {
        TicketFaster service = getService();
        assertEquals(STADIUM.totalSeats(), service.numSeatsAvailable());
        try {
            service.reserveSeats(9999, EMAIL);
            fail();
        } catch (IllegalArgumentException e) {
        }

        SeatHold hold;
        String code;
        hold = service.findAndHoldSeats(1, EMAIL);
        assertEquals(STADIUM.totalSeats() -1, service.numSeatsAvailable());
        assertContains(hold, 1, 1, 1);
        code = service.reserveSeats(hold.getId(), hold.getEmail());
        assertEquals(STADIUM.totalSeats() -1, service.numSeatsAvailable());
        assertEquals(5, code.length());
        try { // duplicate reservation
            service.reserveSeats(hold.getId(), hold.getEmail());
            fail();
        } catch (IllegalArgumentException e) {
        }
        hold = service.findAndHoldSeats(1, EMAIL);
        assertContains(hold,1, 2, 2);
        assertEquals(STADIUM.totalSeats() - 2, service.numSeatsAvailable());
        service.getSeatHoldService().unholdSeats(); // Test unable to reserve expired seats
        assertEquals(STADIUM.totalSeats() -1, service.numSeatsAvailable());
        try {
            service.reserveSeats(hold.getId(), hold.getEmail());
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testGroups() {
        TicketService service = getService();
        SeatHold hold;
        String code;
        //test full row
        hold = service.findAndHoldSeats(100, EMAIL);
        assertContains(hold, 1, 1, 100);
        hold = service.findAndHoldSeats(20, EMAIL);
        assertContains(hold, 2, 41, 60);

        //test a group that is not able to fit in a single row (size 120)
        hold = service.findAndHoldSeats(120, EMAIL);
        assertContains(hold, 2, 1, 40);
        assertContains(hold, 2, 61, 100);
        assertContains(hold, 3, 1, 40);

    }

    @Test
    public void testMaxGroupSize() {
        TicketService service = getService();
        SeatHold hold;
        hold = service.findAndHoldSeats(STADIUM.totalSeats(), EMAIL);
        for (int i = 1; i <= 20; i++) {
            assertContains(hold, i, 1, 100);
        }
        service.reserveSeats(hold.getId(), hold.getEmail());
        assertEquals(0, service.numSeatsAvailable());
        try {
            service.findAndHoldSeats(1, EMAIL);
            fail();
        } catch (IllegalStateException e) {
        }

    }

    /**
     * Mimic real usage.
     */
    @Test
    public void testRandomly() {
        TicketFaster service = getService(new RandomLeftOrRight());
        SeatHold hold;
        for (int i = 1; i < 4000; ++i) {
            int numSeats = maybe(.7) ? 1 : getRandom(2, 20);
            if (numSeats > service.numSeatsAvailable()) {
                break;
            }
            hold = service.findAndHoldSeats(numSeats, EMAIL);
            //TODO: assert range is of size
            int actual = sumRanges(hold);
            assertEquals(numSeats, actual);
            if (maybe(.5)) {
                service.reserveSeats(hold.getId(), hold.getEmail());
            }

            if (i % 20 == 0) {
                service.getSeatHoldService().unholdSeats();
            }
        }
    }

    private int sumRanges(SeatHold hold) {
        return hold.getHeld().stream().mapToInt(r ->
                r.getEnd().getCol() - r.getStart().getCol() +1).sum();
    }

    private boolean maybe(double chance) {
        return Math.random() < chance;
    }

    private int getRandom(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private TicketFaster getService() {
        return getService(new AlwaysLeft());
    }

    private TicketFaster getService(LeftOrRight leftOrRight) {
        AdjacentlyAvailableSeatsService availablityService = getAvailablityService();
        ReservationService reservationService = getReservationService();
        return new TicketFaster(
                STADIUM,
                getSeatHoldService(availablityService, reservationService),
                reservationService,
                availablityService,
                -99999,
                leftOrRight
        );
    }

    private static SeatHoldService getSeatHoldService(SeatAvailabilityService availabilityService,
                                                      ReservationService reservationService) {
        return new SeatHoldService(
                availabilityService,
                new RandomStringService(
                        7,
                        new SecureRandom(),
                        RandomStringService.digits),
                reservationService);
    }

    private static AdjacentlyAvailableSeatsService getAvailablityService() {
        return new AdjacentlyAvailableSeatsService(STADIUM);
    }

    private static ReservationService getReservationService() {
        return new ReservationService(new RandomStringService(5));
    }

    private void assertContains(SeatHold hold, int row, int from, int to) {
        SeatRange range = new SeatRange(
                STADIUM.getSeat(row, from),
                STADIUM.getSeat(row, to)
        );
        Assert.assertTrue(hold.getHeld().contains(range));
    }

}
