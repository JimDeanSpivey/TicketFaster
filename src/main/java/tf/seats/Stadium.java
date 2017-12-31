package tf.seats;

/**
 * @author Jimmy Spivey
 *
 * Implementing classes are not intended to be thread safe. Thread safety is
 * implemented by caller.
 */
public interface Stadium {

    Seat getSeat(String id);

    Seat getSeat(int y, int x);

    Seat[][] asArrays();

    int totalSeats();

    //Would be better to let the stadium score it seats so the allocation
    //services can deal with already scored data and alleviate the concern
    //of knowing anything about the stadium
//    Multimap<Integer, Seat> scoredSeats();

}
