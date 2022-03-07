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
    
    public enum EntryType{
        Normal,
        Parametric,
        Array
    }
    
     public static String fullName(String name, EntryType type){
            switch (type){
                case Parametric:
                    return name+"(?)";
                case Array:
                    return name+"(*)";
                default: return name;
            }
        }


    interface Entry {

        String getName();

        String getDescription();

        Class getOutputClass();
        
        EntryType getType();
        
        default String fullName(){
            return Dictionary.fullName(getName(), getType());
        }
        
        default String display(){
            return new StringBuilder()
                    .append(fullName())
                    .append('\t')
                    .append(getDescription())
                    .append('\t')
                    .append(getOutputClass().getCanonicalName())
                    .toString();
        }
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

    public static String concatenate(@NonNull String... st) {
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
