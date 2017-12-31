package tf.seats;

import com.google.common.base.Objects;

import java.util.Date;
import java.util.Set;

/**
 * @author Jimmy Spivey
 */
public class SeatHold {

    private Integer id;
    private Set<SeatRange> held;
    private String email;
    private Date expiration;

    public SeatHold(Integer id, Set<SeatRange> held, String email, Date expiration) {
        this.id = id;
        this.held = held;
        this.email = email;
        this.expiration = expiration;
    }

    public Integer getId() {
        return id;
    }

    public Set<SeatRange> getHeld() {
        return held;
    }

    public String getEmail() {
        return email;
    }

    public Date getExpiration() {
        return expiration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        SeatHold seatHold = (SeatHold) o;
        return Objects.equal(this.id, seatHold.id) &&
                Objects.equal(this.held, seatHold.held) &&
                Objects.equal(this.email, seatHold.email) &&
                Objects.equal(this.expiration, seatHold.expiration);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id, this.held, this.email, this.expiration);
    }
}
