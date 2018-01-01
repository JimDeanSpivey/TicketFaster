package tf.services;

import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import tf.seats.Reservation;
import tf.test.util.Generation;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Jimmy Spivey
 */
public class ReservationServiceTest {

    private ReservationService service = new ReservationService(
            new RandomStringService(5)
    );

    private boolean setup = false;

    @Before
    public void setup() {
        if (setup) {
            return;
        }
        Set<Reservation> reservations = Sets.newHashSet(
                Generation.reservation(123, "ABC"),
                Generation.reservation(456, "EDF"),
                Generation.reservation(789, "GHI")
        );
        Map<Integer, Reservation> reservationsById =
                reservations.stream().collect(Collectors.toMap(r -> r.getSeatHold().getId(),
                        Function.identity()));
        service.setReservations(reservations);
        service.setReservationsByHoldId(reservationsById);
        setup = true;
    }

    @Test
    public void testReserve() {
        String code = service.reserve(Generation.hold(123, null));
        Assert.assertEquals(5, code.length());
    }

}
