package tf.ui;

import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import tf.SeatHold;
import tf.TicketService;

/**
 * @author Jimmy Spivey
 */
@ShellComponent
public class Commands {

    private TicketService service;

    @ShellMethod("Find seats to hold")
    public String find(
            @ShellOption
            String email,
            @ShellOption
            int seats
    ) {
        SeatHold seatHold = service.findAndHoldSeats(seats, email);
        return seatHold.getId();
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
        return String.format("%d number of seats available", available);
    }

    @ShellMethod("List seat hold ids")
    public String seatsHeld() {
        return "TODO";
    }

}
