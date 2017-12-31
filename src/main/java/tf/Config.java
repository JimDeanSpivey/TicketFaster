package tf;

import javafx.collections.transformation.SortedList;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.jline.PromptProvider;
import tf.seats.SeatRange;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jimmy Spivey
 */
@Configuration
public class Config {

    @Bean
    public PromptProvider myPromptProvider() {
        return () -> new AttributedString("my-shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

    @Bean
    public AdjacentSeatService adjacentSeatService() {
        AdjacentSeatService service = new AdjacentSeatService();
    }

    @Bean
    public List<List<SeatRange>> adjacentSeats() {
        List<List<SeatRange>> list = new ArrayList<>();
        List<List<SeatRange>> autoSorted = new SortedList<>(comparator);

    }
}
