package tf.seats;

/**
 * @author Jimmy Spivey
 *
 * Implementing classes are not intended to be thread safe. Thread safety is
 * implemented by caller.
 */
public interface Stadium {

    Seat getSeat(String id);

    Seat[][] asArrays();

    int totalSeats();

}
