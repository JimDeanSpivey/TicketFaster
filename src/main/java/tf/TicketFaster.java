package tf;

import tf.seats.Seat;
import tf.seats.Stadium;

import java.util.*;

/**
 * @author Jimmy Spivey
 */
public class TicketFaster implements TicketService {

    private Stadium stadium;
    private SeatHoldService seatHoldService;
    private ReservationService reservationService;

    @Override
    public int numSeatsAvailable() { // better to just calculate on the fly instead of keep a counter and have to deal with syncing state
        return stadium.totalSeats() - reservationService.seatsReserved()
                - seatHoldService.seatsHeldCount();
    }

    @Override
    public SeatHold findAndHoldSeats(int numSeats, String customerEmail) {
        //first verify enough seats can be held.
        if (numSeats > numSeatsAvailable()) {
            throw new IllegalArgumentException(String.format(
                    "Unable to reserver %d seats. Only %d seats available.",
                    numSeats, numSeatsAvailable())
            );
        }
        //Search to find seats closest to stadium first. And search from middle col first.
        Seat cloest = findFirstClosest();
        //see if seats can be filled left or right.

        //keep searching until able to find consequtive, then fallback to first available
        //add seathold to queue and map and return it
    }

    private synchronized Set<Seat> allocateSeats(int seatNum) {
        //find needs to be synchronized and that method needs to also assign the seats too
        //keep finding seats. Fill up a list of seats found, but also keep looking for
        // adjacent seats (only if the group size is less than the row size). (actually
        // look for adjacent seats right there, if not found add that seat to the list).
        Set<Seat> firstAvailable = new HashSet<>();
        int firstFound = 0;
        Seat[][] seats = stadium.asArrays();
        int seatsInRow = seats[0].length;

        //First, if the group size is greater than the middle row, just search left to right
        if (seatNum <= seatsInRow && seatNum >= seatsInRow / 2) {
            Set<Seat> toHold = searchLeftToRight(seatNum, seats);
            if (toHold != null) {
                return toHold;
            }
        }

        //Prefer to search from middle and outward
        List<Integer> altSeq = generateAltSeq(seatsInRow);
        for (int y = 0; y < seats.length; y++) {
            for (int x : altSeq) {
                Seat seat = seats[y][x];
                if (!this.isAvailable(seat)) {
                    continue;
                }
                if (firstFound != seatNum) {
                    firstFound++;
                    firstAvailable.add(seat);
                }
                //check for adjacents
                if (seatNum < seatsInRow / 2 ) {

                }
            }
        }
        return firstAvailable;
    }

    private Set<Seat> searchLeftToRight(int seatNum, Seat[][] seats) {
        int seatsInRow = seats[0].length;
        for (int y = 0; y < seats.length; y++) {
            Iterator<Integer> itr = new AvailableSeatIterator(seatNum, seats[y]);
            while(itr.hasNext()) {
                Integer available = itr.next();
                int heldCount = 0;
                Set<Seat> toHold = new HashSet<>();
                for (int x = available; x < seatsInRow; x++) {
                    Seat seat = seats[y][x];
                    if (this.isAvailable(seat)) {
                        heldCount++;
                        toHold.add(seat);
                        if (heldCount == seatNum) {
                            return toHold;
                        }
                    } else {
                        break;
                    }
                }
            }
        }
        return null;
    }

//        Arrays.stream(seats).forEach(row -> altSeq.stream()
//            .forEach(x -> {
//                Seat seat = row[x];
//                if (!this.isAvailable(seat))
//                    return;
//                //check adjacents
//                if (seatNum < seatsInRow) {
//
//                }
//            }));
//        IntStream.range(0, seats.length).forEach(y -> altSeq.stream()
////            .map(x -> seats[y][x])
////            .filter(seat -> !this.isAvailable(seat))
//            .forEach(x -> {
//                Seat seat = seats[y][x];
//                //Check for adjacent
//                if ()
//        }));
//        for (int y = 0; y < seats.length; y++) {
//            int seatsInRow = seats[0].length;
//            int middle = seatsInRow / 2;
//            if (this.isAvailable(seats[y][middle])) {
//                Set<Seat> adjacent = getAdjacent(seats, y, middle, seatNum);
//                if (!adjacent.isEmpty()) {
//                    return adjacent;
//                } else if (fcfsFound > ) {
//
//                    firstAvailable.add(seats[y][middle]);
//                }
//            }
//            for (int offset = 1; true; offset++) {
//                int nextRight = middle + offset;
//                int nextLeft = middle - offset;
//                if (nextRight < seatsInRow) {
//                    Seat next = seats[y][nextRight];
//                    if (this.isAvailable(next)) { //put logic to check for available nearby seats here. then return all seats
//                        return next;
//                    }
//                }
//                if (nextLeft >= 0) {
//                    Seat next = seats[y][nextLeft];
//                    if (this.isAvailable(next)) {
//                        return next;
//                    }
//                }
//                if (nextRight >= seatsInRow) {
//                    break;
//                }
//                if (nextLeft < 0) {
//                    break;
//                }
//            }
//        }
//    }

    private List<Integer> generateAltSeq(int length) {
        List<Integer> altSeq = new LinkedList<>();
        int middle = length / 2;
        altSeq.add(middle);
        for (int offset = 1; true; offset++) {
            int nextRight = middle + offset;
            int nextLeft = middle - offset;
            if (nextRight < length) {
                altSeq.add(nextRight);
            }
            if (nextLeft >= 0) {
                altSeq.add(nextLeft);
            }
            if (nextRight >= length) {
                break;
            }
            if (nextLeft < 0) {
                break;
            }
        }
        return altSeq;
    }

    private boolean isAvailable(Seat seat) {
        return !seatHoldService.isHeld(seat) && !reservationService.isReserved(seat);
    }

    @Override
    public String reserveSeats(int seatHoldId, String customerEmail) {
        throw new UnsupportedOperationException("#reserveSeats()");
    }

    private class AvailableSeatIterator implements Iterator<Integer> {
        private int next = 0;
        private int seatNum;
        private Seat[] row;

        public AvailableSeatIterator(int seatNum, Seat[] row) {
            this.seatNum = seatNum;
            this.row = row;
        }

        @Override
        public boolean hasNext() {
            int next = this.next;
            boolean hasNext = getNext() != null;
            this.next = next;
            return hasNext;
        }

        @Override
        public Integer next() {
            Integer i = getNext();
            if (i != null) return i;
            return -1;
        }

        private Integer getNext() {
            boolean foundUnavailable = next == 0;
            for (int i = next; i < row.length-seatNum; i++) {
                if (isAvailable(row[i])) {
                    if (foundUnavailable) {
                        next = i+1;
                        return i;
                    }
                } else {
                    foundUnavailable = true;
                }
            }
            return null;
        }
    }
}
