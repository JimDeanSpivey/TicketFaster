package tf.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import tf.seats.SeatHold;
import tf.seats.SeatRange;
import tf.seats.Stadium;
import tf.services.ReservationService;
import tf.services.SeatHoldService;
import tf.services.TicketService;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Jimmy Spivey
 */
@ShellComponent
public class Commands {

    @Autowired
    private TicketService service;
    @Autowired
    private SeatHoldService seatHoldService;
    @Autowired
    private Stadium stadium;
    @Autowired
    private ReservationService reservationService;

    @ShellMethod("Find seats to hold")
    public String find(
            @ShellOption
            String email,
            @ShellOption
            int seats
    ) {
        SeatHold hold = service.findAndHoldSeats(seats, email);
        return String.format("Seats held: %s%nSeat Hold id: %d", showReserved(hold), hold.getId());
    }

    private String showReserved(SeatHold hold) {
        int y = 0;
        for (SeatRange held : hold.getHeld()) {
            y = held.getStart().getRow();
            break;
        }
        int finalY = y;
        return hold.getHeld().stream().flatMapToInt(seats ->
                IntStream.range(seats.getStart().getCol(), seats.getEnd().getCol()+1)
        ).mapToObj(x -> stadium.getSeat(finalY, x).getId()).collect(Collectors.joining(", "));

//        return hold.getHeld().stream().map( range ->
//                IntStream.range(range.getStart().getCol(), range.seatCount() + 1).mapToObj(x ->
//                stadium.getSeat(range.getStart().getRow(), x).getId()
//    ).collect(Collectors.joining(", "))).collect(Collectors.joining(System.lineSeparator()));
    }

    @ShellMethod("Reserve held seats")
    public String reserve(
            @ShellOption
            String email,
            @ShellOption
            int seatHoldId
    ) {
        String confirmation = service.reserveSeats(seatHoldId, email);
        return String.format("Confirmation code: %s", confirmation);
    }

    @ShellMethod("List number of seats available to reserve")
    public String seats() {
        int available = service.numSeatsAvailable();
        return String.format("%d of seats available of %d total.",
                available, stadium.totalSeats()
        );
    }

    @ShellMethod("List seat hold ids")
    public String held() {
        return seatHoldService.nonExpired().stream().filter(id ->
                !reservationService.isReserved(id)).map(Object::toString)
                .collect(Collectors.joining(System.lineSeparator()));
    }

}
