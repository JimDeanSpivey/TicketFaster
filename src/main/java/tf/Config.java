package tf;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import tf.seats.*;
import tf.services.*;
import tf.services.helpers.LeftOrRight;
import tf.services.helpers.RandomLeftOrRight;

import java.security.SecureRandom;

/**
 * @author Jimmy Spivey
 */
@Configuration
public class Config {

    private static final String PROMPT = "TicketFaster:>";
    private static final int STADIUM_WIDTH = 10;
    private static final int STADIUM_HEIGHT = 10;
    private static final int STADIUM_SEAT_LENGTH = 100;

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString(PROMPT, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public Stadium stadium(SeatNaming rectangularSeatNaming) {
        RectangularSeatingStadium.Factory factory = new RectangularSeatingStadium.Factory();
        return factory.getInstance(
                STADIUM_WIDTH,
                STADIUM_HEIGHT,
                STADIUM_SEAT_LENGTH,
                rectangularSeatNaming
        );
    }

    @Bean
    public SeatNaming rectangularSeatNaming() {
        return new RectangularSeatNaming();
    }

    @Bean
    public TicketService ticketFaster(
            Stadium stadium, SeatHoldService seatHoldService,
            ReservationService reservationService,
            SeatAvailabilityService seatAvailabilityService,
            Integer expirySeconds,
            LeftOrRight randomLeftOrRight
    ) {
        return new TicketFaster(
                stadium, seatHoldService, reservationService,
                seatAvailabilityService, expirySeconds, randomLeftOrRight
                );
    }

    @Bean
    public LeftOrRight randomLeftOrRight() {
        return new RandomLeftOrRight();
    }

    @Bean
    public Integer expirySeconds() {
        return 60 * 2;
    }

    @Bean
    public SeatHoldService seatHoldService(SeatAvailabilityService seatAvailabilityService,
                                           RandomStringService sevenRandomNumbers,
                                           ReservationService reservationService) {
        return new SeatHoldService(seatAvailabilityService, sevenRandomNumbers,
                reservationService);
    }

    @Bean
    public ReservationService reservationService(RandomStringService fiveRandomChars) {
        return new ReservationService(fiveRandomChars);
    }

    @Bean
    public RandomStringService fiveRandomChars() {
        return new RandomStringService(5, new SecureRandom(),
                RandomStringService.upper + RandomStringService.digits);
    }

    @Bean
    public RandomStringService sevenRandomNumbers() {
        return new RandomStringService(
                7, new SecureRandom(), RandomStringService.digits
        );
    }

    @Bean
    public SeatAvailabilityService adjacentSeatService(Stadium stadium) {
        return new AdjacentlyAvailableSeatsService(stadium);
    }

}
