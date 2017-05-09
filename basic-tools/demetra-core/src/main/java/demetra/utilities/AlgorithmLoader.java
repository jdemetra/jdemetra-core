/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.utilities;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class AlgorithmLoader {
    
    public static <T> T load(Class<T> tclass){
        ServiceLoader<T> loader = ServiceLoader.load(tclass);
        Iterator<T> iterator = loader.iterator();
        
        return null;
    }
    
}
