/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import java.util.List;
import java.util.stream.Stream;

/**
 *
 * @author PALATEJ
 */
@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor(access=lombok.AccessLevel.PRIVATE)
public class ComplexDictionary implements Dictionary{
    
    @lombok.Singular("dictionary") 
    List<PrefixedDictionary> dictionaries;

    @Override
    public Stream<? extends Entry> entries() {
        
        Stream<? extends Entry> stream=Stream.empty();
        for (PrefixedDictionary dic : dictionaries)
            stream=Stream.concat(stream, dic.entries());
        
        return stream;
    }
    
    
}
