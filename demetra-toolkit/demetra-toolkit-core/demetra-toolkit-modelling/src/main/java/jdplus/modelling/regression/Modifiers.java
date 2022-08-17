/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.modelling.regression;

import demetra.timeseries.regression.ModifiedTsVariable;
import demetra.timeseries.regression.TsLags;
import java.util.HashMap;
import java.util.Map;


/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class Modifiers {

    private final Map< Class<? extends ModifiedTsVariable.Modifier>, ModifierFactory> FACTORIES
            = new HashMap<>();

    public <V extends  ModifiedTsVariable.Modifier, W extends V> boolean register(Class<W> wclass, ModifierFactory<V> factory) {
        synchronized (FACTORIES) {
            if (FACTORIES.containsKey(wclass)) {
                return false;
            }
            FACTORIES.put(wclass, factory);
            return true;
        }
    }

    public <V extends  ModifiedTsVariable.Modifier> boolean unregister(Class<V> vclass) {
        synchronized (FACTORIES) {
            ModifierFactory removed = FACTORIES.remove(vclass);
            return removed != null;
        }
    }

    static {
        synchronized (FACTORIES) {
            // Basic
            FACTORIES.put(TsLags.class, LagsFactory.FACTORY);
        }
    }
    
    public static ModifierFactory factoryFor(ModifiedTsVariable.Modifier modifier){
       synchronized (FACTORIES) {
            // Basic
            return FACTORIES.get(modifier.getClass());
        }
        
    }


}
