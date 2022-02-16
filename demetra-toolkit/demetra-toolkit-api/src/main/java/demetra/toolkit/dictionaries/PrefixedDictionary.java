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

    Dictionary core;
    
    EntryType type; 
    
    public PrefixedDictionary(String path, Dictionary core){
        this(path, core, EntryType.Normal);
    }

    @Override
    public Stream<? extends Entry> entries() {
        return path == null || path.length() == 0 ? core.entries()
                : core.entries().map(entry -> new DerivedItem(entry));
    }
    
    @Override
    public Stream<String> keys() {
        return path == null || path.length() == 0 ? core.keys()
                : core.keys().map(keyitem -> derivedName(Dictionary.fullName(path, type), keyitem));
    }

    @Override
    public Stream<String> keys(Class tclass) {
        return path == null || path.length() == 0 ? core.keys(tclass)
                : core.keys()
                        .filter(item -> tclass.isAssignableFrom(item.getClass()))
                        .map(keyitem -> derivedName(Dictionary.fullName(path, type), keyitem));
    }

    @lombok.Getter
    @lombok.AllArgsConstructor
    public class DerivedItem implements Entry {

        Entry core;

        @Override
        public String getName() {
            return derivedName(Dictionary.fullName(path, type), core.getName());
        }

        @Override
        public String getDescription() {
            return core.getDescription();
        }

        @Override
        public Class getOutputClass() {
            return core.getOutputClass();
        }

        @Override
        public EntryType getType() {
            return core.getType();
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
