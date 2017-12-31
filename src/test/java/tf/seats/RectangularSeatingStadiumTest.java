package tf.seats;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Jimmy Spivey
 */
public class RectangularSeatingStadiumTest {
    private RectangularSeatingStadium.Factory factory = new RectangularSeatingStadium.Factory();

    @Test
    public void testGetSeat() {
        Stadium stadium;
        Seat seat;
        stadium = factory.getInstance(5, 5, 100, new RectangularSeatNaming());
        seat = stadium.getSeat("1A");
        assertEquals(1, seat.getCol());
        assertEquals(1, seat.getRow());
        assertEquals("1A", seat.getId());
        assertEquals(100, seat.getCmFromStage());
        seat = stadium.getSeat("5E");
        assertEquals(5, seat.getCol());
        assertEquals(5, seat.getRow());
        assertEquals("5E", seat.getId());
        assertEquals(500, seat.getCmFromStage());
        seat = stadium.getSeat("3C");
        assertEquals(3, seat.getCol());
        assertEquals(3, seat.getRow());
        assertEquals("3C", seat.getId());
        stadium = factory.getInstance(5, 3, 100, new RectangularSeatNaming());
        seat = stadium.getSeat("1E");
        assertEquals(5, seat.getCol());
        assertEquals(1, seat.getRow());
        assertEquals("1E", seat.getId());
        stadium = factory.getInstance(30, 3, 100, new RectangularSeatNaming());
        seat = stadium.getSeat("2AA");
        assertEquals(27, seat.getCol());
        assertEquals(2, seat.getRow());
        assertEquals("2AA", seat.getId());
        seat = stadium.getSeat("3Z");
        assertEquals(26, seat.getCol());
        assertEquals(3, seat.getRow());
        assertEquals("3Z", seat.getId());
    }
}
