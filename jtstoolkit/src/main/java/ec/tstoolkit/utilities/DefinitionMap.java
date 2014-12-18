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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefinitionMap {

    private final HashMap<String, String[]> map_ = new HashMap<>();
    private String name_;
    private int id_ = 1;

    public DefinitionMap(String name) {
        name_ = name;
    }

    public void clear() {
        map_.clear();
    }

    public List<String> getItems() {
        Set<String> keys = map_.keySet();
        return Jdk6.newArrayList(keys);
    }

    public String add() {
        String name = nextValidName();
        map_.put(name, null);
        return name;
    }

    public boolean rename(String oldname, String newname) {
        if (map_.containsKey(newname)) {
            return false;
            // change dependencies...

        }
        String[] dep = map_.get(oldname);
        if (dep != null) {
            map_.remove(oldname);
            map_.put(newname, dep);
        }
        for (String[] sdep : map_.values()) {
            if (sdep != null) {
                for (int i = 0; i < sdep.length; ++i) {
                    if (sdep[i].equals(oldname)) {
                        sdep[i] = newname;

                    }
                }
            }
        }
        return true;
    }

     /**
     * Checks that the first one depends on the second one
     * @param c First item
     * @param p Second item
     * @return 
     */
    public boolean depend(String c, String p) {
        if (c.endsWith(p)) {
            return true;

        }
        String[] dep = map_.get(c);
        if (dep != null) {
            for (int i = 0; i < dep.length; ++i) {
                if (dep[i].equals(p) || depend(dep[i], p)) {
                    return true;

                }

            }
        }
        return false;
    }

    public void buildSequence(List<String> list) {
        HashSet<String> set = new HashSet<>();
        for (String s : map_.keySet()) {
            buildSubSequence(s, list, set);

        }
    }

    private void buildSubSequence(String s, List<String> list, HashSet<String> set) {
        String[] dep = map_.get(s);
        if (dep != null) {
            for (int i = 0; i < dep.length; ++i) {
                if (!set.contains(dep[i])) {
                    buildSubSequence(dep[i], list, set);
               }
            }
        }
        if (!set.contains(s)) {
            list.add(s);
            set.add(s);
        }
    }

    private String nextName() {
        StringBuilder builder = new StringBuilder();
        builder.append(name_).append(id_++);
        return builder.toString();
    }

    public String nextValidName() {
        String s = null;
        do {
            s = nextName();
        }
        while (map_.containsKey(s));
        return s;
    }

    public boolean add(String name, String[] dep) {
        if (map_.containsKey(name)) {
            return false;
       }
        else {
            map_.put(name, dep);
            return true;
        }
    }

    public boolean contains(String name) {
        return map_.containsKey(name);
    }

    public void setDependencies(String obj, String[] source) {
        map_.put(obj, source);
    }

    public boolean isLinked(String name) {
        for (String[] dep : map_.values()) {
            if (dep != null) {
                for (int i = 0; i < dep.length; ++i) {
                    if (name.equals(dep[i])) {
                        return true;
                   }
                }
            }
        }
        return false;
    }

    public void remove(String name) {
        map_.remove(name);
    }
}
