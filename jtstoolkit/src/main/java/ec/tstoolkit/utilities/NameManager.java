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

package ec.tstoolkit.utilities;

import ec.tstoolkit.BaseException;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author pcuser
 */
public class NameManager<T> implements InformationSetSerializable, IModifiable {

    private final String prefix_;
    private boolean dirty_;
    private final HashMap<String, T> tdic_ = new HashMap<>();
    private final HashMap<T, NameKey> ndic_ = new HashMap<>();
    private final HashMap<String, Integer> locks_ = new HashMap<>();
    private final Class<T> tclass_;
    private final INameValidator validator_;

    public NameManager(Class<T> tclass, String prefix, INameValidator validator) {
        tclass_ = tclass;
        prefix_ = prefix;
        validator_ = validator;
    }

    public String getLastNameError() {
        return validator_ == null ? null : validator_.getLastError();
    }
    
    /**
     * 
     * @return Current name validator. Might be null.
     * @since 1.5.2
     */
    public INameValidator getNameValidator(){
        return validator_;
    }

    public void clear() {
        synchronized (tdic_) {
            if (tdic_.isEmpty()) {
                return;
            }
            if (locks_.isEmpty()) {
                tdic_.clear();
                ndic_.clear();
            } else {
                String[] names = unlockedNames();
                for (String n : names) {
                    T obj = tdic_.get(n);
                    ndic_.remove(obj);
                    tdic_.remove(n);
                }
            }

            dirty_ = true;
        }
    }

    public void lock(String name) {
        synchronized (tdic_) {
            if (!tdic_.containsKey(name)) {
                throw new BaseException("Unknown name");
            }
            Integer n = locks_.get(name);
            if (n == null) {
                locks_.put(name, 1);
            } else {
                locks_.put(name, n + 1);
            }
        }
    }

    public void unlock(String name) {
        synchronized (tdic_) {
            if (!tdic_.containsKey(name)) {
                throw new BaseException("Unknown name");
            }
            Integer n = locks_.get(name);
            if (n != null) {
                if (n == 1) {
                    locks_.remove(name);
                } else {
                    locks_.put(name, n - 1);
                }
            }
        }
    }

    public void unlock() {
        synchronized (tdic_) {
            locks_.clear();
        }
    }

    public boolean isLocked(String name) {
        synchronized (tdic_) {
            return locks_.containsKey(name);
        }
    }

    public Collection<T> variables() {
        synchronized (tdic_) {
            return tdic_.values();
        }
    }

    public int getCount() {
        synchronized (tdic_) {
            return tdic_.size();
        }
    }

    public T get(String name) {
        synchronized (tdic_) {
            return tdic_.get(name);
        }
    }

    public String get(T obj) {
        synchronized (tdic_) {
            NameKey nk = ndic_.get(obj);
            if (nk != null) {
                return nk.name;
            } else {
                return null;
            }
        }
    }

    public boolean rename(String item, String newname) {
        if (validator_ != null && !validator_.accept(newname)) {
            throw new BaseException(validator_.getLastError());
        }
        synchronized (tdic_) {
            if (tdic_.containsKey(newname)) {
                return false;
            }
            if (locks_.containsKey(item)) {
                return false;
            }
            T var = tdic_.get(item);
            if (var == null) {
                return false;
            }
            tdic_.remove(item);
            tdic_.put(newname, var);
            // We want to preserve the initial order
            NameKey nk = ndic_.get(var); 
            nk.name=newname;
            dirty_ = true;
            return true;
        }
    }

    public void set(String name, T var) {
        if (validator_ != null && !validator_.accept(name)) {
            throw new BaseException(validator_.getLastError());
        }
        synchronized (tdic_) {
            T cur = tdic_.get(name);
            // name in use
            if (cur != null) {
                if (var == cur) {
                    return;
                } else {
                    throw new BaseException("Name already in use");
                }
            }
            NameKey curname = ndic_.get(var);
            if (curname != null) {
                // obj already named. rename the object
                if (locks_.containsKey(curname.name)) {
                    throw new BaseException("Locked object");
                }
                tdic_.remove(curname.name);
                tdic_.put(name, var);
                curname.name = name;
                dirty_ = true;
            } else {
                // new object
                tdic_.put(name, var);
                ndic_.put(var, new NameKey(name));
                dirty_ = true;
            }
        }
    }

    public String nextName() {
        int id = 1;
        String name;

        synchronized (tdic_) {
            do {
                name = prefix_ + Integer.toString(id);
                ++id;
            } while (tdic_.containsKey(name));
        }
        return name;
    }

    public boolean contains(String name) {
        synchronized (tdic_) {
            return tdic_.containsKey(name);
        }
    }

    public boolean contains(T obj) {
        synchronized (tdic_) {
            return ndic_.containsKey(obj);
        }
    }

    public boolean remove(String name) {
        synchronized (tdic_) {
            if (locks_.containsKey(name)) {
                return false;
            }
            T obj = tdic_.get(name);
            if (obj == null) {
                return false;
            } else {
                tdic_.remove(name);
                ndic_.remove(obj);
                dirty_ = true;
                return true;
            }
        }
    }

    public boolean remove(T obj) {
        synchronized (tdic_) {
            NameKey nk = ndic_.get(obj);
            if (nk == null) {
                return false;
            } else {
                if (locks_.containsKey(nk.name)) {
                    return false;
                }
                tdic_.remove(nk.name);
                ndic_.remove(obj);
                dirty_ = true;
                return true;
            }
        }
    }

    public String[] getNames() {
        synchronized (tdic_) {
            return NameKey.sort(ndic_.values());
        }
    }

    public String[] unlockedNames() {
        synchronized (tdic_) {
            List<NameKey> names = new ArrayList<>();

            for (NameKey n : ndic_.values()) {
                if (!locks_.containsKey(n.name)) {
                    names.add(n);
                }
            }

            return NameKey.sort(names);
        }
    }

    @Override
    public boolean isDirty() {
        return dirty_;
    }

    @Override
    public void resetDirty() {
        dirty_ = false;
    }

    @Override
    public InformationSet write(boolean verbose) {
        synchronized (tdic_) {
            InformationSet info = new InformationSet();
            String[] names = getNames();
            for (int i = 0; i < names.length; ++i) {
                info.set(names[i], tdic_.get(names[i]));
            }
            return info;
        }
    }

    @Override
    public boolean read(InformationSet info) {
        synchronized (tdic_) {
            List<Information<T>> sel = info.select(tclass_);
            for (Information<T> item : sel) {
                tdic_.put(item.name, item.value);
                ndic_.put(item.value, new NameKey(item.name));
            }
            return true;
        }
    }
}
