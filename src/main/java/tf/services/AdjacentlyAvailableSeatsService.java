package tf.services;

import com.google.common.collect.*;
import tf.seats.Seat;
import tf.seats.SeatRange;
import tf.seats.Stadium;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jimmy Spivey
 */
public class AdjacentlyAvailableSeatsService implements SeatAvailabilityService {
    //Quickly track which ranges of adjacent seats are available
    private SortedSetMultimap<Integer,SeatRange> adjacents = TreeMultimap.create();
    private Stadium stadium;
    private int takenCount;

    public AdjacentlyAvailableSeatsService(Stadium stadium) {
        this.stadium = stadium;
    }

    @Override
    public void holdSeats(Set<SeatRange> ranges) {
        ranges.forEach(range -> {
            int y = range.getStart().getRow();
            Collection<SeatRange> values = adjacents.get(y);
            //TODO: could validate that incoming isn't overlapping
            values.add(range);
        });
        takenCount += countSeats(ranges);
    }

    @Override
    public void freeSeatRanges(Set<SeatRange> ranges) {
        ranges.forEach(range -> {
            int y = range.getStart().getRow();
            adjacents.remove(y, range);
        });
        takenCount -= countSeats(ranges);
    }

    private int countSeats(Set<SeatRange> ranges) {
        return ranges.stream().mapToInt(r -> r.getEnd().getCol() - r.getStart().getCol()+1).sum();
    }

    @Override
    public SeatRange find(int row, int numSeats, int rowLength) {
        Seat[] seats = stadium.asArrays()[row];
        List<SeatRange> potential = getAvailable(row, rowLength);
        if (potential.isEmpty()) {
            return null;
        } if (potential.size() == 1) {
            SeatRange only = potential.get(0);
            if (only.getStart().getCol() == 1 && only.getEnd().getCol() == rowLength) {
                int middle = (rowLength - numSeats) / 2;
                return new SeatRange(seats[middle], seats[middle+numSeats]);
            }
        }
        //Pick seat closest to middle
        Map<Integer, SeatRange> scores = potential.stream().collect(
                Collectors.toMap(maybe ->
                        this.score(maybe, rowLength / 2), maybe -> maybe)
        );
        return scores.get(Collections.min(scores.keySet()));
    }

    @Override
    public List<SeatRange> getAvailable(int row, int rowLength) {
        List<SeatRange> potential = new ArrayList<>();
        Set<SeatRange> unavailable =  adjacents.get(row);
        Seat[] seats = stadium.asArrays()[row];
        if (unavailable.isEmpty()) {
            return ImmutableList.of(new SeatRange(
                    seats[0], seats[rowLength-1]
            ));
        }
        Iterator<SeatRange> iterator = unavailable.iterator();
        SeatRange current = iterator.hasNext() ? iterator.next() : null;
        boolean first = true;
        while (current != null) {
            SeatRange next = iterator.hasNext() ? iterator.next() : null;
            int start = current.getStart().getCol();
            int end = current.getEnd().getCol();
            if (first && start != 1) { // Check for previous open range at start
                potential.add(getSeatRange(row, 1, start-1));
            }
            if (next == null) { // Check for last open range at end
                if (end != rowLength) {
                    potential.add(getSeatRange(row, end+1, rowLength));
                }
                break;
            }
            int nextStart = next.getStart().getCol();
            if (end+1 != nextStart) { //This element has open space to the right
                potential.add(getSeatRange(row, end+1, nextStart-1));
            }
            current = next;
            first = false;
        }
        return potential;
    }

    private SeatRange getSeatRange(int row, int start, int end) {
        return new SeatRange(
                stadium.getSeat(row, start),
                stadium.getSeat(row, end)
                );
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

    @Override
    public int takenSeats() {
        return takenCount;
    }

}
