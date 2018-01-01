package tf.services;

import org.springframework.scheduling.annotation.Scheduled;
import tf.seats.SeatHold;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Jimmy Spivey
 */
public class SeatHoldService {

    private SeatAvailabilityService seatAvailabilityService;
    private RandomStringService sevenRandomNumbers;
    //The most common operation is to query based on date when checking for expired seat holds
    private Queue<SeatHold> queue = new ConcurrentLinkedQueue<>();
    //To make looking up by email faster
    private Map<Integer,SeatHold> heldById = new ConcurrentHashMap<>();
    //To make expiration checking faster
    private Set<Integer> nonExpired =  Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>());

    public SeatHoldService(SeatAvailabilityService seatAvailabilityService, RandomStringService sevenRandomNumbers) {
        this.seatAvailabilityService = seatAvailabilityService;
        this.sevenRandomNumbers = sevenRandomNumbers;
    }

    public void hold(SeatHold seatHold) {
        heldById.put(seatHold.getId(), seatHold);
        nonExpired.add(seatHold.getId());
        queue.add(seatHold);
    }

    @Scheduled(fixedRate = 1000)
    public synchronized void unholdSeats() {
        while (!queue.isEmpty() && queue.peek().getExpiration().before(new Date())) {
            SeatHold hold = this.queue.poll();
            nonExpired.remove(hold.getId());
            seatAvailabilityService.freeSeatRanges(hold.getHeld());
        }
    }

    public int generateId() {
        Integer newId;
        do {
            newId = Integer.parseInt(sevenRandomNumbers.nextString());
        } while (heldById.keySet().contains(newId));
        return newId;
    }

    public boolean hasExpired(Integer id) {
        return !nonExpired.contains(id);
    }

    public SeatHold getSeatHold(Integer id) {
        return heldById.get(id);
    }

    public Set<Integer> nonExpired() {
        return nonExpired;
    }

}
