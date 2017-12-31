package tf.seats;

import com.google.common.base.Objects;

/**
 * @author Jimmy Spivey
 */
public class Seat {

    private String id;
    private int cmFromStage; // Can be used to score seats
    private int row;
    private int col;

    public Seat(String id, int cmFromStage, int row, int col) {
        this.id = id;
        this.cmFromStage = cmFromStage;
        this.row = row;
        this.col = col;
    }

    public String getId() {
        return this.id;
    }

    public int getCmFromStage() {
        return this.cmFromStage;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return this.cmFromStage == seat.cmFromStage &&
                this.row == seat.row &&
                this.col == seat.col &&
                Objects.equal(this.id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id, this.cmFromStage, this.row, this.col);
    }
}
