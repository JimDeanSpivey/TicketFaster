package tf.util;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Jimmy Spivey
 */
public class RandomIteratorTest {

    private List<String> strings = ImmutableList.of("a", "b", "c", "d");

    @Test
    public void test() throws Exception {
        RandomIterator<String> itr = new RandomIterator<>(strings);
        Set<String> collect = new HashSet<>();
        while (itr.hasNext()) {
            collect.add(itr.next());
        }
        Assert.assertTrue(collect.contains("a"));
        Assert.assertTrue(collect.contains("b"));
        Assert.assertTrue(collect.contains("c"));
    }


}
