/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
}
