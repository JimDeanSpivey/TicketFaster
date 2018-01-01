package tf.services;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import tf.seats.Seat;
import tf.seats.SeatRange;
import tf.seats.Stadium;

import java.util.*;

/**
 * @author Jimmy Spivey
 */
public class AdjacentlyAvailableSeatsService implements SeatAvailabilityService {
    //Quickly track which ranges of adjacent seats are available.
    //Entries in adjacents multimap reflect unavailable seats.
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
            boolean removed = adjacents.remove(y, range);
            if (removed == false) {
                throw new IllegalStateException("Can't find adjacents to remove.");
            }
        });
        int takenCount = countSeats(ranges);
        this.takenCount -= takenCount;
    }

    private int countSeats(Set<SeatRange> ranges) {
        return ranges.stream().mapToInt(r -> r.getEnd().getCol() - r.getStart().getCol()+1).sum();
    }

    /**
     * Looks for only all adjacent seats of size numSeats. Will
     * return null if there are no adjacent seats of size numSeats.
     * If multiple available adjacents are found, the one closest
     * to the middle is chosen.
     *
     * Example: A row from 1 - 100 exists. Seats 1-80 are a mix of
     * not available and available. Seats 81-100 are all available.
     * Seats 1-80 do not contain any set of adjacent seats that are
     * greater than 20 (numSeats) to hold. Seats 81-100 will be
     * returned as the range as it's the only available choice.
     */
    @Override
    public SeatRange find(int row, int numSeats, int rowLength) {
        Seat[] seats = stadium.asArrays()[row-1];
        List<SeatRange> potential = getAvailable(row, rowLength);
        if (potential.isEmpty()) {
            return null;
        } if (potential.size() == 1) {
            SeatRange only = potential.get(0);
            if (only.getStart().getCol() == 1 && only.getEnd().getCol() == rowLength) {
                int middle = (rowLength - numSeats) / 2;
                return new SeatRange(seats[middle], seats[middle+numSeats-1]);
            }
        }
        //Pick seat closest to middle
        Map<Integer, SeatRange> scores = new HashMap<>();
        for (SeatRange maybe : potential) {
            int size = maybe.getEnd().getCol() - maybe.getStart().getCol() +1;
            if (size >= numSeats) {
                scores.put(this.score(maybe, rowLength /2 ), maybe);
            }
        }
        if (scores.isEmpty()) {
            return null;
        }
        return scores.get(Collections.min(scores.keySet()));
    }

    /**
     * Available seats are neither held nor reserved. Returns all
     * available seats of the given row as a List of SeatRanges.
     */
    @Override
    public List<SeatRange> getAvailable(int row, int rowLength) {
        List<SeatRange> potential = new ArrayList<>();
        Set<SeatRange> unavailable =  adjacents.get(row);
        if (unavailable.isEmpty()) {
            return ImmutableList.of(
                    getSeatRange(row, 1, rowLength));
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
            if (end+1 != nextStart) { // This element has open space to the right
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
