package tf.util;

import java.util.*;

/**
 * @author hat tip: https://stackoverflow.com/a/29309810/17675
 */
public class RandomIterator<T> implements Iterator<T> {

    private Iterator<Integer> indicies;

    List<T> delegate;

    public RandomIterator(List<T> delegate) {
        Random r = new Random();
        this.delegate = delegate;
        Set<Integer> indexSet = new LinkedHashSet<>();
        while(indexSet.size() != delegate.size())
            indexSet.add(r.nextInt(delegate.size()));
        indicies = indexSet.iterator();
    }

    @Override
    public boolean hasNext() {
        return indicies.hasNext();
    }

    @Override
    public T next() {
        return delegate.get(indicies.next());
    }
}
