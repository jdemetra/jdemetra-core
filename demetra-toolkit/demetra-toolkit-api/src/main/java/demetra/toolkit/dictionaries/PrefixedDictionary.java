/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import java.util.List;
import java.util.stream.Stream;
import lombok.NonNull;

/**
 *
 * @author PALATEJ
 */
@lombok.Getter
@lombok.AllArgsConstructor
public class PrefixedDictionary implements Dictionary {


    String path;

    @lombok.experimental.Delegate
    Dictionary core;

    @Override
    public Stream<? extends Entry> entries() {
        return path == null || path.length() == 0 ? core.entries()
                : core.entries().map(entry -> new DerivedItem(path, entry));
    }

    @Override
    public Stream<String> keys() {
        return path == null || path.length() == 0 ? core.keys()
                : core.keys().map(keyitem -> derivedName(path, keyitem));
    }

    @Override
    public Stream<String> keys(Class tclass) {
        return path == null || path.length() == 0 ? core.keys(tclass)
                : core.keys()
                        .filter(item -> tclass.isAssignableFrom(item.getClass()))
                        .map(keyitem -> derivedName(path, keyitem));
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public static class DerivedItem implements Entry {

        @NonNull
        String path;

        @lombok.experimental.Delegate
        Entry core;

        @Override
        public String getName() {
            return derivedName(path, core.getName());
        }

    }

    private static String derivedName(String path, String name) {
        return new StringBuilder()
                .append(path)
                .append(SEP)
                .append(name)
                .toString();

    }

}
