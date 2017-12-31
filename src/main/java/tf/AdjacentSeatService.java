package tf;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import tf.seats.Seat;
import tf.seats.SeatRange;
import tf.seats.Stadium;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jimmy Spivey
 */
public class AdjacentSeatService {
    //Quickly track which ranges of adjacent seats are available
    private Multimap<Integer,SeatRange> adjacents = ArrayListMultimap.create();
    private Stadium stadium;

    void reserveSeatRanges(Set<SeatRange> ranges) {
        ranges.forEach(range -> {
            int y = range.getStart().getRow();
            Collection<SeatRange> values = adjacents.get(y);
            //TODO: validate incoming isn't overlapping
            values.add(range);
        });
    }

    void reserveSeats(Set<Seat> seats) {
        Set<SeatRange> set = transform(seats);
        reserveSeatRanges(set);
    }

    private Set<SeatRange> transform(Set<Seat> seats) {
        return seats.stream().map(SeatRange::new).collect(Collectors.toSet());
    }

    void freeSeats(Set<Seat> seats) {
        Set<SeatRange> set = transform(seats);
        freeSeatRanges(set);
    }

    void freeSeatRanges(Set<SeatRange> ranges) {
        ranges.forEach(range -> {
            int y = range.getStart().getRow();
            adjacents.remove(y, range);
        });
    }

    public SeatRange find(int row, int numSeats, int rowLength) {
        List<SeatRange> unavailable = (List<SeatRange>) adjacents.get(row);
        Seat[] seats = stadium.asArrays()[row];
        if (unavailable.isEmpty()) {
            int middle = (rowLength - numSeats) / 2;
            return new SeatRange(seats[middle], seats[middle+numSeats]);
        }
        Set<SeatRange> potential = findPotentialRanges(rowLength, unavailable, seats);
        //Pick seat closest to middle
        Map<Integer, SeatRange> scores = potential.stream().collect(
                Collectors.toMap(maybe ->
                        this.score(maybe, rowLength / 2), maybe -> maybe)
        );
        return scores.get(Collections.min(scores.keySet()));
    }

    private Set<SeatRange> findPotentialRanges(int rowLength, List<SeatRange> unavailable, Seat[] seats) {
        Set<SeatRange> potential = new HashSet<>();
        int trail = 0;
        for (int i = 0; i < unavailable.size(); i++) {
            SeatRange taken = unavailable.get(i);
            int start = taken.getStart().getCol();
            int end = taken.getEnd().getCol();
            if (start - 1 != trail) {
                int to = i == unavailable.size() ?
                        rowLength : unavailable.get(i+1).getStart().getCol() - 1;
                potential.add(new SeatRange(seats[trail+1], seats[to]));
            }
            trail = end;
        }
        return potential;
    }

    private Integer score(SeatRange maybe, int middle) {
        int start = maybe.getStart().getCol();
        int end = maybe.getEnd().getCol();
        if (middle <= end && middle >= start) {
            return 0;
        } if (middle < start) {
            return start - middle;
        } else {
            return middle - end;
        }
    }
}
