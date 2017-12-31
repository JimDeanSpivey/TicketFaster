package tf;

import tf.seats.SeatRange;

import java.util.Date;
import java.util.Set;

/**
 * @author Jimmy Spivey
 */
public interface SeatHold {

    String getId();
    Set<SeatRange> getHeld();
    String getEmail();
    Date getExpiration();

}
