package tf.services;

import org.junit.Test;
import tf.seats.RectangularSeatNaming;
import tf.seats.RectangularSeatingStadium;
import tf.seats.SeatRange;
import tf.testutil.Generation;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Jimmy Spivey
 */
public class AdjacentlyAvailableSeatsServiceTest {


    public static final int ROW_LENGTH = 10;

    private AdjacentlyAvailableSeatsService getService() {
        return new AdjacentlyAvailableSeatsService(
                new RectangularSeatingStadium.Factory().getInstance(
                        ROW_LENGTH, 20, 100, new RectangularSeatNaming()
                )
        );
    }

    @Test
    public void testFind() {
        AdjacentlyAvailableSeatsService service = getService();

    }

    @Test
    public void testGetAvailable() {
        AdjacentlyAvailableSeatsService service = getService();
        List<SeatRange> available;
//        service.holdSeats(Generation.range(1, 1, 10));
//        available = service.getAvailable(1, ROW_LENGTH);
//        assertTrue(available.isEmpty());
//
        service.holdSeats(Generation.range(2, 10, 10));
        available = service.getAvailable(2, ROW_LENGTH);
        assertEquals(1, available.size());
        assertOpen(available.get(0), 1, 9);

        service.holdSeats(Generation.range(3, 1, 1));
        available = service.getAvailable(3, ROW_LENGTH);
        assertOpen(available.get(0), 2, 10);

        service.holdSeats(Generation.range(4, 5, 5));
        available = service.getAvailable(4, ROW_LENGTH);
        assertOpen(available.get(0), 1, 4);
        assertOpen(available.get(1), 6, 10);

        service.holdSeats(Generation.range(5, 5, 10));
        available = service.getAvailable(5, ROW_LENGTH);
        assertOpen(available.get(0), 1, 4);

        service.holdSeats(Generation.range(6, 1, 5));
        available = service.getAvailable(6, ROW_LENGTH);
        assertOpen(available.get(0), 6, 10);

        service.holdSeats(Generation.range(7, 1, 5));
        service.holdSeats(Generation.range(7, 6, 10));
        available = service.getAvailable(7, ROW_LENGTH);
        assertEquals(0, available.size());

        service.holdSeats(Generation.range(8, 9, 9));
        service.holdSeats(Generation.range(8, 1, 2));
        service.holdSeats(Generation.range(8, 5, 6));
        available = service.getAvailable(8, ROW_LENGTH);
        assertOpen(available.get(1), 7, 8);
        assertOpen(available.get(2), 10, 10);

        service.holdSeats(Generation.range(9, 1, 5));
        service.holdSeats(Generation.range(9, 6, 9));
        available = service.getAvailable(9, ROW_LENGTH);
        assertOpen(available.get(0), 10, 10);

        service.holdSeats(Generation.range(10, 2, 5));
        service.holdSeats(Generation.range(10, 6, 10));
        available = service.getAvailable(10, ROW_LENGTH);
        assertOpen(available.get(0), 1, 1);

        service.holdSeats(Generation.range(11, 1, 4));
        service.holdSeats(Generation.range(11, 6, 10));
        available = service.getAvailable(11, ROW_LENGTH);
        assertOpen(available.get(0), 5, 5);

        service.holdSeats(Generation.range(12, 1, 1));
        service.holdSeats(Generation.range(12, 2, 3));
        service.holdSeats(Generation.range(12, 7, 7));
        service.holdSeats(Generation.range(12, 8, 10));
        available = service.getAvailable(12, ROW_LENGTH);
        assertOpen(available.get(0), 4, 6);

        available = service.getAvailable(15, ROW_LENGTH);
        assertOpen(available.get(0), 1, 10);
    }

    private void assertOpen(SeatRange open, int from, int to) {
        assertEquals(from, open.getStart().getCol());
        assertEquals(to, open.getEnd().getCol());
    }


}
