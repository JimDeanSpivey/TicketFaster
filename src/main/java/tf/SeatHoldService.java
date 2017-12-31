package tf;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.springframework.scheduling.annotation.Scheduled;
import tf.seats.Seat;

import java.util.Collections;
import java.util.Date;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Jimmy Spivey
 */
public class SeatHoldService {

    //The most common operation is to query based on date
    private Queue<SeatHold> queue = new ConcurrentLinkedQueue<>();
    //To make looking up by email faster
    private Multimap<String,SeatHold> multimap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());
    //Quickly track what seats are available
    private Set<Seat> seatsHeld = Collections.newSetFromMap(new ConcurrentHashMap<Seat, Boolean>());


    public int seatsHeldCount() {
        return multimap.size();
    }

    @Scheduled(fixedRate = 1000)
    public void unholdSeats() {
        while (this.queue.peek().getExpiration().after(new Date())) {
            SeatHold poll = this.queue.poll();
            this.multimap.remove(poll.getEmail(), poll);
            poll.getHeld().forEach(seatsHeld::remove);
            //todo: remove logic for adjacents
        }
    }

    public boolean isHeld(Seat seat) {
        return multimap.containsKey(seat);
    }
}
