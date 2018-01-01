package tf.services.helpers;

/**
 * @author Jimmy Spivey
 */
public class RandomLeftOrRight implements LeftOrRight {
    @Override
    public boolean choose() {
        return Math.random() < .5;
    }
}
