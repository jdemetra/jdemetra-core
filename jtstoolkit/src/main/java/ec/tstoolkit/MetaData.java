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

package ec.tstoolkit;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.utilities.IModifiable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class MetaData implements Map<String, String>, Cloneable, IModifiable {
    
    /**
     * Reserved items
     */
    public static final String 
            DESCRIPTION="@description", OWNER="@owner",
            SOURCE="@source", ID="@id", DATE="@timestamp",
            DOCUMENT="@document", SUMMARY="@summary", 
            NOTE="@note", TODO="@todo",
            ALGORITHM="@algorithm", 
            QUALITY="@quality";
       

    private final Map<String, String> props_;
    private boolean dirty_;

    /**
     *
     */
    public MetaData() {
        this(new HashMap<>());
    }

    /**
     *
     * @param dic
     */
    public MetaData(Map<String, String> dic) {
        props_ = new HashMap<>();
        props_.putAll(dic);
    }

    private String normalizeKey(String key) {
        return key.toLowerCase();
    }

    @SuppressWarnings("unchecked")
    @Override
    public MetaData clone() {
        MetaData result = new MetaData();
        result.props_.putAll(props_);
        return result;
    }

    /**
     *
     * @param md
     */
    public void copy(MetaData md) {
        clear();
        props_.putAll(md.props_);
    }

    /**
     *
     * @param name
     * @param value
     * @deprecated use {@link MetaData#put(java.lang.String, java.lang.String) }
     * instead
     */
    @Deprecated
    public void set(String name, String value) {
        put(name, value);
        dirty_=true;
    }

    @Override
    public int size() {
        return props_.size();
    }

    @Override
    public boolean isEmpty() {
        return props_.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String ? props_.containsKey(normalizeKey((String) key)) : false;
    }

    @Override
    public boolean containsValue(Object value) {
        return props_.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return key instanceof String ? props_.get(normalizeKey((String) key)) : null;
    }

    @Override
    public String put(String key, String value) {
        dirty_=true;
        return props_.put(normalizeKey(key), value);
    }

    @Override
    public String remove(Object key) {
        dirty_=true;
        return key instanceof String ? props_.remove(normalizeKey((String) key)) : null;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        dirty_=true;
        for (Entry<? extends String, ? extends String> o : m.entrySet()) {
            put(o.getKey(), o.getValue());
        }
    }

    @Override
    public void clear() {
        dirty_=true;
        props_.clear();
    }

    @Override
    public Set<String> keySet() {
        return props_.keySet();
    }

    @Override
    public Collection<String> values() {
        return props_.values();
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        return Collections.unmodifiableSet(props_.entrySet());
    }

    @Override
    public boolean isDirty() {
        return dirty_; 
    }

    @Override
    public void resetDirty() {
        dirty_=false;
    }
    
    public static boolean isNullOrEmpty(MetaData md){
        return md == null || md.isEmpty();
    }
}
