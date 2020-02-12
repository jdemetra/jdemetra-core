/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa;

import demetra.data.Range;
import demetra.sa.ComponentType;
import demetra.timeseries.regression.AdditiveOutlier;
import demetra.timeseries.regression.Constant;
import demetra.timeseries.regression.ICalendarVariable;
import demetra.timeseries.regression.IMovingHolidayVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.LevelShift;
import demetra.timeseries.regression.LinearTrend;
import demetra.timeseries.regression.PeriodicContrasts;
import demetra.timeseries.regression.PeriodicDummies;
import demetra.timeseries.regression.PeriodicOutlier;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.SwitchOutlier;
import demetra.timeseries.regression.TransitoryChange;
import demetra.timeseries.regression.TrigonometricVariables;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author palatej
 */
public class SaVariablesMapping {

    @FunctionalInterface
    public static interface VariableMapping<V> {

        ComponentType map(V variable);
    }

    private static final Map< Class<? extends ITsVariable>, VariableMapping> DEFAULTMAPPING
            = new HashMap<>();

    /**
     * Register a new type
     * @param <V>
     * @param wclass Should be an interface or a final class (not checked)
     * @param mapping 
     * @return 
     */
    public static <V extends ITsVariable> boolean register(Class<V> wclass, VariableMapping<V> mapping) {
        synchronized (DEFAULTMAPPING) {
            if (DEFAULTMAPPING.containsKey(wclass)) {
                return false;
            }
            DEFAULTMAPPING.put(wclass, mapping);
            return true;
        }
    }

    private static <V extends ITsVariable> boolean put(Class<V> wclass, VariableMapping<V> factory) {
        synchronized (DEFAULTMAPPING) {
            if (DEFAULTMAPPING.containsKey(wclass)) {
                return false;
            }
            DEFAULTMAPPING.put(wclass, factory);
            return true;
        }
    }

    public static <V extends ITsVariable> boolean unregister(Class<V> vclass) {
        synchronized (DEFAULTMAPPING) {
            VariableMapping removed = DEFAULTMAPPING.remove(vclass);
            return removed != null;
        }
    }

    static {
        synchronized (DEFAULTMAPPING) {
            // Basic
            DEFAULTMAPPING.put(Constant.class, v -> ComponentType.Trend);
            DEFAULTMAPPING.put(LinearTrend.class, v -> ComponentType.Trend);

            // Outliers
            DEFAULTMAPPING.put(AdditiveOutlier.class, v -> ComponentType.Irregular);
            DEFAULTMAPPING.put(LevelShift.class, v -> ComponentType.Trend);
            DEFAULTMAPPING.put(TransitoryChange.class, v -> ComponentType.Irregular);
            DEFAULTMAPPING.put(SwitchOutlier.class, v -> ComponentType.Irregular);
            DEFAULTMAPPING.put(PeriodicOutlier.class, v -> ComponentType.Seasonal);

            // Calendar
            DEFAULTMAPPING.put(ICalendarVariable.class, v -> ComponentType.CalendarEffect);
            DEFAULTMAPPING.put(IMovingHolidayVariable.class, v -> ComponentType.CalendarEffect);

            // Others
            DEFAULTMAPPING.put(Ramp.class, v -> ComponentType.Trend);
            put(InterventionVariable.class, var -> {
                if (var.getDeltaSeasonal() > 0 && var.getDelta() > 0) {
                    return ComponentType.Undefined;
                }
                Range<LocalDateTime>[] sequences = var.getSequences();
                int maxseq = 0;
                for (int i = 0; i < sequences.length; ++i) {
                    int len = (int) sequences[i].start().until(sequences[i].end(), ChronoUnit.DAYS) / 365;
                    if (len > maxseq) {
                        maxseq = len;
                    }
                }
                if (maxseq > 0) {
                    return var.getDeltaSeasonal() == 0 ? ComponentType.Trend : ComponentType.Undefined;
                }
                if (var.getDeltaSeasonal() > 0) {
                    return ComponentType.Seasonal;
                }
                if (var.getDelta() > .8) {
                    return ComponentType.Trend;
                }
                return ComponentType.Irregular;

            });
            DEFAULTMAPPING.put(PeriodicDummies.class, v -> ComponentType.Seasonal);
            DEFAULTMAPPING.put(PeriodicContrasts.class, v -> ComponentType.Seasonal);
            DEFAULTMAPPING.put(TrigonometricVariables.class, v -> ComponentType.Seasonal);
        }
    }

    /**
     * Gets the default mapping for a given variable.
     * 
     * We search first a mapping for the class of the actual object, then for 
     * the direct interfaces and finally for indirect interfaces (ancestors of the 
     * direct interfaces)
     * 
     * Parent classes are not considered (they should not be considered)
     * 
     * @param var
     * @return 
     */
    public ComponentType defaultMapping(ITsVariable var) {
        synchronized (DEFAULTMAPPING) {
            VariableMapping m = DEFAULTMAPPING.get(var.getClass());
            if (m != null) {
                return m.map(var);
            } else {
                m = deepSearch(var.getClass());
                if (m != null) {
                    return m.map(var);
                } else {
                    return ComponentType.Undefined;
                }
            }
        }
    }

    private VariableMapping deepSearch(final Class t) {
        // root interface
        if (t == ITsVariable.class) {
            return null;
        }
        // search for direct interfaces (declaration order)
        Class[] interfaces = t.getInterfaces();
        for (int i = 0; i < interfaces.length; ++i) {
            VariableMapping m = DEFAULTMAPPING.get(interfaces[i]);
            if (m != null) {
                return m;
            }
        }
        // search for indirect interfaces
        for (int i = 0; i < interfaces.length; ++i) {
            VariableMapping m = deepSearch(interfaces[i]);
            if (m != null) {
                return m;
            }
        }
        return null;
    }

    private final Map<ITsVariable, ComponentType> mapping = new HashMap<>();

    public void addDefault(ITsVariable... vars) {
        for (ITsVariable var : vars) {
            mapping.put(var, defaultMapping(var));
        }
    }

    public void put(ITsVariable var, ComponentType type) {
        mapping.put(var, type);
    }

    public void clear() {
        mapping.clear();
    }

    public void remove(ITsVariable var) {
        mapping.remove(var);
    }

    public Map<ITsVariable, ComponentType> mapping() {
        return Collections.unmodifiableMap(mapping);
    }

    public ITsVariable[] forComponentType(ComponentType type) {
        List<ITsVariable> vars = new ArrayList();
        mapping.forEach((var, vtype) -> {
            if (type == vtype) {
                vars.add(var);
            }
        });
        return vars.toArray(n -> new ITsVariable[n]);
    }

}
