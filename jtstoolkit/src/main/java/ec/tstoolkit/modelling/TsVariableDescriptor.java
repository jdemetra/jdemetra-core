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
package ec.tstoolkit.modelling;

import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.timeseries.TsException;
import ec.tstoolkit.timeseries.regression.DecoratedTsVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.LaggedTsVariable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class TsVariableDescriptor implements Cloneable, InformationSetSerializable {
    
    public static enum UserComponentType{
     /**
     *
     */
    Undefined,
    /**
     *
     */
    Series,
    /**
     *
     */
    Trend,
    /**
     *
     */
    Seasonal,
     /**
     *
     */
    SeasonallyAdjusted,
    /**
     *
     */
    Irregular;
       public static UserComponentType of(ComponentType type){
           switch (type){
               case Series:
                   return Series;
               case Trend:
                   return Trend;
               case Irregular:
                   return Irregular;
               case SeasonallyAdjusted:
                   return SeasonallyAdjusted;
               case Seasonal:
               case CalendarEffect:
                   return Seasonal;
               default:
                   return Undefined;
           }
       }
       
       public static UserComponentType from(String s){
           UserComponentType type=UserComponentType.valueOf(s);
           if (type != null)
               return type;
           ComponentType ctype=ComponentType.valueOf(s);
           if (ctype != null)
               return of(ctype);
           else
               return UserComponentType.Undefined;
       }
       
       public ComponentType type(){
            switch (this){
               case Series:
                   return ComponentType.Series;
               case Trend:
                   return ComponentType.Trend;
               case Irregular:
                   return ComponentType.Irregular;
               case SeasonallyAdjusted:
                   return ComponentType.SeasonallyAdjusted;
               case Seasonal:
                   return ComponentType.Seasonal;
               default:
                   return ComponentType.Undefined;
           }
          
       }
    }

    public static final String NAME = "name",
            EFFECT = "effect",
            FIRSTLAG = "firstlag",
            LASTLAG = "lastlag";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, NAME), String.class);
        dic.put(InformationSet.item(prefix, EFFECT), String.class);
        dic.put(InformationSet.item(prefix, FIRSTLAG), Integer.class);
        dic.put(InformationSet.item(prefix, LASTLAG), Integer.class);
    }

    private String m_name;
    private int m_firstlag, m_lastlag;
    private UserComponentType m_effect = UserComponentType.Undefined;

    public TsVariableDescriptor() {
    }

    public TsVariableDescriptor(String name) {
        m_name = name;
    }

    @Override
    public TsVariableDescriptor clone() {
        try {
            return (TsVariableDescriptor) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public String toString() {
        if (m_name == null) {
            return "Unnamed";
        }
        StringBuilder builder = new StringBuilder();
        builder.append(m_name);
        if (m_firstlag != 0 || m_lastlag != 0) {
            builder.append('[').append(m_firstlag);
            if (m_firstlag != m_lastlag) {
                builder.append(" : ").append(m_lastlag);
            }
            builder.append(']');
        }
        return builder.toString();
    }

    public ITsVariable toTsVariable(ProcessingContext context) {
        ITsVariable var = context.getTsVariable(m_name);
        if (var == null) {
            return null;
        }
        DecoratedTsVariable x = new DecoratedTsVariable(var, m_name);
        if (m_firstlag == 0 && m_lastlag == 0) {
            return x;
        }
        return new LaggedTsVariable(x, m_firstlag, m_lastlag);
    }

    public String getName() {
        return m_name;
    }

    public void setName(String value) {
        m_name = value;
    }

    public int getFirstLag() {
        return m_firstlag;
    }

    public int getLastLag() {
        return m_lastlag;
    }

    public void setFirstLag(int start) {
        if (start > this.m_lastlag) {
            return;
        }

        m_firstlag = start;
    }

    public void setLastLag(int end) {
        if (this.m_firstlag > end) {
            return;
        }

        m_lastlag = end;
    }

    public void setLags(int start, int end) {
        if (start > end) {
            throw new TsException("Invalid lags");
        }
        m_firstlag = start;
        m_lastlag = end;
    }

    public UserComponentType getEffect() {
        return m_effect;
    }

    public void setEffect(UserComponentType value) {
        m_effect = value;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsVariableDescriptor && equals((TsVariableDescriptor) obj));
    }

    private boolean equals(TsVariableDescriptor other) {
        return other.m_firstlag == m_firstlag && other.m_lastlag == m_lastlag
                && other.m_name.equals(m_name) && other.m_effect == m_effect;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.m_name);
        hash = 37 * hash + this.m_firstlag;
        hash = 37 * hash + this.m_lastlag;
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (m_name != null) {
            info.add(NAME, m_name);
        }
        if (verbose || m_firstlag != 0) {
            info.add(FIRSTLAG, m_firstlag);
        }
        if (verbose || m_lastlag != 0) {
            info.add(LASTLAG, m_lastlag);
        }
        if (verbose || m_effect != UserComponentType.Undefined) {
            info.add(EFFECT, m_effect.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        m_name = info.get(NAME, String.class);
        Integer flag = info.get(FIRSTLAG, Integer.class);
        if (flag != null) {
            m_firstlag = flag;
        }
        Integer llag = info.get(LASTLAG, Integer.class);
        if (llag != null) {
            m_lastlag = llag;
        }
        String effect = info.get(EFFECT, String.class);
        if (effect != null) {
            m_effect = UserComponentType.from(effect);
        }
        return true;
    }

}
