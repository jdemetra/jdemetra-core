/*
* Copyright 2013 National Bank of Belgium
*
* Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
* by the European Commission - subsequent versions of the EUPL (the "Licence");
* You may not use this work except in compliance with the Licence.
* You may obtain a copy of the Licence at:
*
* http://ec.europa.eu/idabc/eupl
*
* Unless required by applicable law or agreed to in writing, software 
* distributed under the Licence is distributed on an "AS IS" basis,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the Licence for the specific language governing permissions and 
* limitations under the Licence.
*/

package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.utilities.Jdk6;
import java.util.ArrayList;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
public class TsVariableList implements Cloneable {

    /**
     * Retrieves the actual variable, which can be hidden behind modifier(s).
     * @param var The considered variable.
     * @return When there isn't any modifier, var is returned. Otherwise, the 
     * the actual variable is returned.
     */
    public static ITsVariable getRoot(ITsVariable var) {
        ITsVariable current = var;
        while (current instanceof ITsModifier) {
            ITsModifier modifier = (ITsModifier) current;
            current = modifier.getVariable();
        }
        return current;
    }
    private ArrayList<ITsVariable> vars_ = new ArrayList<>();

    /**
     * 
     */
    public TsVariableList() {
    }

    /**
     * 
     * @param var
     */
    public void add(ITsVariable var) {
        vars_.add(var);
    }

    /**
     * Selects all the regression variables
     * @return
     */
    public TsVariableSelection<ITsVariable> all() {
        TsVariableSelection<ITsVariable> sel = new TsVariableSelection<>();
        int pos = 0;
        for (ITsVariable item : vars_) {
            sel.add(item, pos);
            pos += item.getDim();
        }
        return sel;
    }

    @Override
    public TsVariableList clone() {
        try {
            TsVariableList list = (TsVariableList) super.clone();
            list.vars_ = new ArrayList<>();
            for (ITsVariable var : vars_) {
                list.vars_.add(var);
            }
            return list;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    /**
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T extends ITsVariable> int getCompatibleCount(Class<T> type) {
        int n = 0;
        for (ITsVariable group : vars_) {
            if (type.isAssignableFrom(group.getClass())) {
                n += group.getDim();
            }
        }
        return n;
    }

    /**
     * 
     * @param <T>
     * @param type
     * @return
     */
    public <T extends ITsVariable> int getCount(Class<T> type) {
        int n = 0;
        for (ITsVariable group : vars_) {
            if (type.isInstance(group)) {
                n += group.getDim();
            }
        }
        return n;
    }

    // / <summary>
    // / Returns the common domain of the regression variables
    // / </summary>
    /**
     * 
     * @return
     */
    public TsDomain getDomain() {
        TsDomain domain = null;
        for (ITsVariable item : vars_) {
            TsDomain curdomain = item.getDefinitionDomain();
            if (curdomain != null) {
                if (domain == null) {
                    domain = curdomain;
                }
                else if (domain.getFrequency() != curdomain.getFrequency()) // incompatible
                // frequencies
                {
                    throw new TsException(TsException.INCOMPATIBLE_FREQ);
                }
                else {
                    domain = domain.intersection(curdomain);
                }
            }
        }
        return domain;
    }

    /**
     * 
     * @return
     */
    public TsFrequency getFrequency() {
        TsFrequency freq = TsFrequency.Undefined;
        for (ITsVariable item : vars_) {
            TsFrequency curfreq = item.getDefinitionFrequency();
            if (curfreq != TsFrequency.Undefined) {
                if (freq == TsFrequency.Undefined) {
                    freq = curfreq;
                }
                else if (freq != curfreq) // incompatible frequencies
                {
                    throw new TsException(TsException.INCOMPATIBLE_FREQ);
                }
            }
        }
        return freq;
    }

    /**
     * 
     * @param idx
     * @return
     */
    public int getItemPosition(int idx) {
        int pos = 0;
        for (int i = 0; i < idx; ++i) {
            pos += vars_.get(i).getDim();
        }
        return pos;
    }

    /**
     * 
     * @param var
     * @return
     */
    public int getItemPosition(ITsVariable var) {
        int pos = 0;
        for (ITsVariable item : vars_) {
            if (item == var) {
                return pos;
            }
            pos += item.getDim();
        }
        return -1;
    }

    /**
     * 
     * @return
     */
    public int getVariablesCount() {
        int n = 0;
        for (ITsVariable group : vars_) {
            n += group.getDim();
        }
        return n;
    }

    /**
     * 
     * @return
     */
    public boolean isEmpty() {
        return vars_.isEmpty();
    }

    /**
     * 
     * @return
     */
    public ITsVariable[] items() {
        return Jdk6.Collections.toArray(vars_, ITsVariable.class);
    }

    /**
     * Selects all the variables that can be assigned to the given type
     * @param <T> The type for selection
     * @param type Class of the selection type
     * @return The selected variables. May be an empty selection. However,null is never returned 
     */
    public <T extends ITsVariable> TsVariableSelection<T> select(Class<T> type) {
        int cur = 0;
        TsVariableSelection<T> sel = new TsVariableSelection<>();
        for (ITsVariable group : vars_) {
            if (type.isInstance(group)) {
                sel.add((T) group, cur);
            }
            cur += group.getDim();
        }

        return sel;
    }

    /**
     * Selects all the variables that are compatible with the given type.
     * Variables compatible with a given type are variables that
     * can be assigned to the given type or that contain (through modifier(s))
     * variables of that type.
     * @param <T> The type for selection
     * @param type Class of the selection type
     * @return The selected variables. May be an empty selection. However,null is never returned 
     */
    public <T extends ITsVariable> TsVariableSelection<ITsVariable> selectCompatible(
            Class<T> type) {
        int cur = 0;
        TsVariableSelection<ITsVariable> sel = new TsVariableSelection<>();
        for (ITsVariable group : vars_) {
            if (type.isInstance(getRoot(group))) {
                sel.add(group, cur);
            }
            cur += group.getDim();
        }

        return sel;
    }

    public TsVariableSelection<IOutlierVariable> select(OutlierType type) {
        int cur = 0;
        TsVariableSelection<IOutlierVariable> sel = new TsVariableSelection<>();
        for (ITsVariable group : vars_) {
            if (group instanceof IOutlierVariable) {
                IOutlierVariable o = (IOutlierVariable) group;
                if (type == OutlierType.Undefined || o.getOutlierType() == type) {
                    sel.add(o, cur);
                }
            }
            cur += group.getDim();
        }

        return sel;
    }

    public void clear() {
        vars_.clear();
    }

    public interface ISelector {

        boolean accept(ITsVariable var);
    }

    public TsVariableSelection<ITsVariable> select(ISelector selector) {
        int cur = 0;
        TsVariableSelection<ITsVariable> sel = new TsVariableSelection<>();
        for (ITsVariable group : vars_) {
            if (selector.accept(group)) {
                sel.add(group, cur);
            }
            cur += group.getDim();
        }
        return sel;
    }
}
