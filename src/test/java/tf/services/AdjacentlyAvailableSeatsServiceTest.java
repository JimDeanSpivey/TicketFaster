package tf.services;

import org.junit.Test;
import tf.seats.RectangularSeatNaming;
import tf.seats.RectangularSeatingStadium;
import tf.seats.SeatRange;
import tf.test.util.Generation;

import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Jimmy Spivey
 */
public class AdjacentlyAvailableSeatsServiceTest {

    public static final int GET_ROW_LENGTH = 10;
    public static final int FIND_ROW_LENGTH = 100;

    private AdjacentlyAvailableSeatsService getService(int rowLength) {
        return new AdjacentlyAvailableSeatsService(
                new RectangularSeatingStadium.Factory().getInstance(
                        rowLength, 20, 100, new RectangularSeatNaming()
                )
        );
    }

    @Test
    public void testFind() {
        AdjacentlyAvailableSeatsService service = getService(FIND_ROW_LENGTH);
        SeatRange range;

        //Test on completely empty row
        range = service.find(20, 1, FIND_ROW_LENGTH);
        assertOpen(range, 50, 50);
        range = service.find(20, 100, FIND_ROW_LENGTH);
        assertOpen(range, 1, 100);
        range = service.find(20, 2, FIND_ROW_LENGTH);
        assertOpen(range, 50, 51);
        range = service.find(20, 3, FIND_ROW_LENGTH);
        assertOpen(range, 49,51);

        //Test on completely filled row
        service.holdSeats(Generation.range(1, 1, 100));
        range = service.find(1, 1, FIND_ROW_LENGTH);
        assertNull(range);

        //Test on row where middle seat isn't occupied
        service.holdSeats(Generation.range(2, 30, 40));
        service.holdSeats(Generation.range(2, 60, 70));
        range = service.find(2, 1, FIND_ROW_LENGTH);
        assertOpen(range, 41, 59);
        range = service.find(2, 19, FIND_ROW_LENGTH);
        assertOpen(range, 41, 59);
        range = service.find(2, 20, FIND_ROW_LENGTH);
        assertOpen(range, 71, 100);

        //Test on row where middle seat is occupied.
        service.holdSeats(Generation.range(3, 40, 60));
        range = service.find(3, 1, FIND_ROW_LENGTH);
        assertOpen(range,61, 100);
    }

    @Test
    public void testGetAvailable() {
        AdjacentlyAvailableSeatsService service = getService(GET_ROW_LENGTH);
        List<SeatRange> available;
        service.holdSeats(Generation.range(1, 1, 10));
        available = service.getAvailable(1, GET_ROW_LENGTH);
        assertTrue(available.isEmpty());

        service.holdSeats(Generation.range(2, 10, 10));
        available = service.getAvailable(2, GET_ROW_LENGTH);
        assertEquals(1, available.size());
        assertOpen(available.get(0), 1, 9);

        service.holdSeats(Generation.range(3, 1, 1));
        available = service.getAvailable(3, GET_ROW_LENGTH);
        assertOpen(available.get(0), 2, 10);

        service.holdSeats(Generation.range(4, 5, 5));
        available = service.getAvailable(4, GET_ROW_LENGTH);
        assertOpen(available.get(0), 1, 4);
        assertOpen(available.get(1), 6, 10);

        service.holdSeats(Generation.range(5, 5, 10));
        available = service.getAvailable(5, GET_ROW_LENGTH);
        assertOpen(available.get(0), 1, 4);

        service.holdSeats(Generation.range(6, 1, 5));
        available = service.getAvailable(6, GET_ROW_LENGTH);
        assertOpen(available.get(0), 6, 10);

        service.holdSeats(Generation.range(7, 1, 5));
        service.holdSeats(Generation.range(7, 6, 10));
        available = service.getAvailable(7, GET_ROW_LENGTH);
        assertEquals(0, available.size());

        service.holdSeats(Generation.range(8, 9, 9));
        service.holdSeats(Generation.range(8, 1, 2));
        service.holdSeats(Generation.range(8, 5, 6));
        available = service.getAvailable(8, GET_ROW_LENGTH);
        assertOpen(available.get(1), 7, 8);
        assertOpen(available.get(2), 10, 10);

        service.holdSeats(Generation.range(9, 1, 5));
        service.holdSeats(Generation.range(9, 6, 9));
        available = service.getAvailable(9, GET_ROW_LENGTH);
        assertOpen(available.get(0), 10, 10);

        service.holdSeats(Generation.range(10, 2, 5));
        service.holdSeats(Generation.range(10, 6, 10));
        available = service.getAvailable(10, GET_ROW_LENGTH);
        assertOpen(available.get(0), 1, 1);

        service.holdSeats(Generation.range(11, 1, 4));
        service.holdSeats(Generation.range(11, 6, 10));
        available = service.getAvailable(11, GET_ROW_LENGTH);
        assertOpen(available.get(0), 5, 5);

        service.holdSeats(Generation.range(12, 1, 1));
        service.holdSeats(Generation.range(12, 2, 3));
        service.holdSeats(Generation.range(12, 7, 7));
        service.holdSeats(Generation.range(12, 8, 10));
        available = service.getAvailable(12, GET_ROW_LENGTH);
        assertOpen(available.get(0), 4, 6);

        available = service.getAvailable(15, GET_ROW_LENGTH);
        assertOpen(available.get(0), 1, 10);
    }

    private void assertOpen(SeatRange open, int from, int to) {
        assertEquals(from, open.getStart().getCol());
        assertEquals(to, open.getEnd().getCol());
    }

    private void assertRandomOpen(SeatRange open, int from1, int from2, int to1, int to2) {
        int start = open.getStart().getCol();
        int end = open.getEnd().getCol();
        System.out.println("Start: "+start);
        System.out.println("End: "+end);
        assertTrue(
                (from1 == start && to1 == end)
                        || (from2 == start && to2 == end)
        );
    }

}
