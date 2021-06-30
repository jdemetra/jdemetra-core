package demetra.util;

import java.util.Objects;

@lombok.Value
public final class NamedObject<T> implements Comparable<NamedObject<T>> {

    private String name;
    private T object;

    @Override
    public int compareTo(NamedObject<T> o) {
        int result = name.compareTo(o.name);
        if (result != 0) {
            return result;
        }
        if (object instanceof Comparable) {
            return ((Comparable) object).compareTo(o.object);
        }
        return Objects.equals(object, o.object) ? 0 : 1;
    }

}
