/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import java.util.stream.Stream;
import lombok.NonNull;

/**
 *
 * @author PALATEJ
 */
public interface Dictionary {

    interface Entry {

        String getName();

        String getDescription();

        Class getType();

        boolean isList();
    }

    Stream<? extends Entry> entries();

    default Stream<String> keys() {
        return entries()
                .map(item -> item.getName());
    }

    default Stream<String> keys(Class tclass) {
        return entries()
                .filter(item -> tclass.isAssignableFrom(item.getClass()))
                .map(item -> item.getName());
    }

    public static String concat(@NonNull String... st) {
        if (st.length == 1) {
            return st[0];
        }
        StringBuilder builder = new StringBuilder();
        builder.append(st[0]);
        for (int i = 1; i < st.length; ++i) {
            builder.append(SEP).append(st[i]);
        }
        return builder.toString();
    }

    public static final char SEP = '.';

}
