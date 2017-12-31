package tf.seats;

/**
 * @author Jimmy Spivey
 */
public class Seat {

    private String id;
    private int cmFromStage;
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
}
