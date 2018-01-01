package tf.util;

import tf.services.helpers.LeftOrRight;

/**
 * @author Jimmy Spivey
 */
public class AlwaysLeft implements LeftOrRight {
    @Override
    public boolean choose() {
        return true;
    }
}
