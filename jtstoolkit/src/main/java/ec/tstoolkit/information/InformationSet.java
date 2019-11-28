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
package ec.tstoolkit.information;

import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.utilities.WildCards;
import java.lang.reflect.Array;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Set of information. That structure can be used recursively: An information
 * set can contain other information sets.
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class InformationSet implements Cloneable {

    public static final char SEP = '.';
    public static final String STRSEP = new String(new char[]{SEP});
    public static final String CONTENT = "content", TYPE_KEY = "type", VERSION_KEY = "version";
    public static final String LOG = "log";

    public static String concatenate(String... s) {
        switch (s.length) {
            case 0:
                return "";
            case 1:
                return s[0];
            default:
                boolean first = true;
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < s.length; ++i) {
                    if (s[i] != null) {
                        if (!first) {
                            builder.append(SEP);
                        } else {
                            first = false;
                        }
                        builder.append(s[i]);
                    }
                }
                return builder.toString();
        }
    }

    public static boolean isPrefix(String fullName, String prefix) {
        if (fullName.length() <= prefix.length() + 1) {
            return false;
        }
        if (fullName.charAt(prefix.length()) != SEP) {
            return false;
        }
        for (int i = 0; i < prefix.length(); ++i) {
            if (fullName.charAt(i) != prefix.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasWildCards(String str) {
        return str.indexOf('*') >= 0 || str.indexOf('?') >= 0;
    }

    public static String removePrefix(String name) {
        int pos = 0;
        int len = name.length();
        while (pos < len && name.charAt(pos) != SEP) {
            ++pos;
        }
        if (pos == len) {
            return null;
        }
        return name.substring(pos + 1, len);
    }

    public static String simpleName(String fullName) {
        int pos = fullName.length();
        while (pos > 0 && fullName.charAt(pos - 1) != SEP) {
            --pos;
        }
        if (pos > 0) {
            return fullName.substring(pos, fullName.length());
        } else {
            return fullName;
        }
    }

    public static String[] split(String fullName) {
        int nsep = 0;
        int len = fullName.length();
        for (int i = 0; i < len; ++i) {
            if (fullName.charAt(i) == SEP) {
                ++nsep;
            }
        }
        if (nsep == 0) {
            return new String[]{fullName};
        } else {
            String[] s = new String[nsep + 1];
            int pos = 0;
            int end = 0, beg = 0;
            while (pos < nsep) {
                if (fullName.charAt(end) == SEP) {
                    s[pos++] = fullName.substring(beg, end);
                    beg = end + 1;
                    end = beg;
                }
                ++end;
            }
            s[pos] = fullName.substring(beg);
            return s;
        }

    }

    static class IndexedName implements Comparable<IndexedName> {

        final long index;
        final String name;
        private static final AtomicLong curIndex = new AtomicLong(0);

        IndexedName(long idx, String name) {
            this.index = idx;
            this.name = name;
        }

        IndexedName(String name) {
            this.index = curIndex.getAndIncrement();
            this.name = name;
        }

        @Override
        public int compareTo(IndexedName o) {
            return Long.compare(index, o.index);
        }
    }

    static class IndexedObject<S> {

        final long index;
        final S obj;
        private static final AtomicLong curIndex = new AtomicLong(0);

        IndexedObject(S obj) {
            this.index = curIndex.getAndIncrement();
            this.obj = obj;
        }
    }

    /**
     * Pre-specified names for errors and warnings
     *
     * @param wString
     * @param curString
     * @return
     */
    public static boolean wildCompare(String wString, String curString) {

        if (wString == null) {
            return true;
        }
        WildCards wc = new WildCards(wString);
        return wc.match(curString);
    }

    public static String item(String prefix, String name) {
        if (prefix == null || prefix.isEmpty()) {
            return name;
        } else if (name == null || name.isEmpty()) {
            return prefix;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append(prefix).append(SEP).append(name);
            return builder.toString();
        }
    }
    private ConcurrentHashMap<String, IndexedObject<?>> information_ = new ConcurrentHashMap<>();
    private boolean locked_;
    public static final String ERRORS = "errors", WARNINGS = "warnings";

    /**
     * Creates a new empty information set.
     */
    public InformationSet() {
    }

    /**
     * Makes a clone of this information set.
     *
     * @return The clone of this object. Any child InformationSet is also
     * cloned. However the other information are not cloned. The clone object is
     * never locked !
     */
    @Override
    public InformationSet clone() {
        try {
            InformationSet set = (InformationSet) super.clone();
            set.information_ = new ConcurrentHashMap<>();
            for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
                if (kv.getValue().obj instanceof InformationSet) {
                    InformationSet subset = (InformationSet) kv.getValue().obj;
                    set.add(kv.getKey(), subset.clone());
                } else {
                    set.add(kv.getKey(), kv.getValue().obj);
                }
            }
            return set;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }

    public boolean isLocked() {
        return locked_;
    }

    public void setLocked(boolean locked) {
        locked_ = locked;
        // propagates the locking
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                ((InformationSet) kv.getValue().obj).setLocked(locked);

            }

        }
    }

    /**
     * Adds a new information in the set
     *
     * @param info Information
     * @return True is returned if the information has been successfully added.
     * False otherwise. Information can be added if the set doesn't contain an
     * entry with the same name.
     */
    @SuppressWarnings("unchecked")
    public boolean set(final Information<?> info) {
        if (locked_) {
            return false;

        }
        if (info == null || info.value == null) {
            return false;

        }
        information_.put(info.name, new IndexedObject(info.value));
        return true;
    }

    /**
     * Adds a new information in the set
     *
     * @param info Information
     * @return True is returned if the information has been successfully added.
     * False otherwise. Information can be added if the set doesn't contain an
     * entry with the same name.
     */
    @SuppressWarnings("unchecked")
    public boolean add(final Information<?> info) {
        if (locked_) {
            return false;

        }
        if (info == null || info.value == null) {
            return false;

        }
        String name = info.name;
        while (information_.containsKey(name)) {
            name += "_";
        }
        information_.put(name, new IndexedObject(info.value));
        return true;
    }

    /**
     * Adds a new information in the set
     *
     * @param <S> Type of information
     * @param name Name of information
     * @param info Information
     * @return True is returned if the information has been successfully added.
     * False otherwise. Information can be added if the set doesn't contain an
     * entry with the same name.
     */
    public <S> boolean add(final String name, final S info) {
        return add(new Information<>(name, info));
    }

    public <S> boolean add(final String[] deepname, final S info) {
        int len = deepname.length - 1;
        if (len < 0) {
            return false;
        }
        if (len == 0) {
            return add(new Information<>(deepname[0], info));
        }
        InformationSet cur = this;
        for (int i = 0; i < len; ++i) {
            cur = cur.subSet(deepname[i]);
            if (cur == null) {
                return false;
            }
        }
        return cur.add(new Information<>(deepname[len], info));
    }

    public <S> boolean set(final String[] deepname, final S info) {
        int len = deepname.length - 1;
        if (len < 0) {
            return false;
        }
        if (len == 0) {
            return set(deepname[0], info);
        }
        InformationSet cur = this;
        for (int i = 0; i < len; ++i) {
            cur = cur.subSet(deepname[i]);
            if (cur == null) {
                return false;
            }
        }
        return cur.set(deepname[len], info);
    }

    /**
     *
     * @param <S>
     * @param e
     * @param info
     * @return
     */
    public <S> boolean add(final Enum e, final S info) {
        return add(new Information<>(e.toString(), info));
    }

    /**
     *
     * @param string
     */
    public void addWarning(String string) {
        if (locked_) {
            return;
        }
        addMessage(WARNINGS, string);
    }

    /**
     *
     * @param string
     */
    public void addError(String string) {
        if (locked_) {
            return;
        }
        addMessage(ERRORS, string);
    }

    private void addMessage(String item, String msg) {
        String[] items = get(item, String[].class);
        if (items != null) {
            String[] nitems = new String[items.length + 1];
            for (int i = 0; i < items.length; ++i) {
                nitems[i] = items[i];

            }
            nitems[items.length] = msg;
            set(item, nitems);
        } else {
            set(item, new String[]{msg});
        }
    }

    private void addMessages(String item, String[] msg) {
        String[] items = get(item, String[].class);
        if (items != null) {
            String[] nitems = new String[items.length + msg.length];
            for (int i = 0; i < items.length; ++i) {
                nitems[i] = items[i];

            }
            for (int i = 0; i < msg.length; ++i) {
                nitems[items.length + i] = msg[i];
            }
            set(item, nitems);
        } else {
            set(item, msg);
        }
    }

    private List<String> allItems(String name) {
        ArrayList<String> allitems = new ArrayList<>();

        String[] items = get(name, String[].class);
        if (items != null) {
            Collections.addAll(allitems, items);
        }
        List<Information<InformationSet>> subsets = select(InformationSet.class);
        for (Information<InformationSet> subset : subsets) {
            allitems.addAll(subset.value.allItems(name));

        }
        return allitems;
    }

    /**
     * Clears the information set.
     */
    public void clear() {
        if (locked_) {
            return;

        }
        information_.clear();
    }

    public void copy(InformationSet info) {
        for (Entry<String, IndexedObject<?>> kv : info.information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                InformationSet subset = (InformationSet) kv.getValue().obj;
                set(kv.getKey(), subset.clone());
            } else {
                set(kv.getKey(), kv.getValue().obj);
            }
        }
    }

    /**
     * Provides the content of the set
     *
     * @return An array with all the entries is returned. The order of the set
     * corresponds in the current implementation to the order in which
     * information has been added. However, that property will not be guaranteed
     * in the future.
     */
    @NewObject
    public String[] content() {
        IndexedName[] inames = new IndexedName[information_.size()];
        int idx = 0;
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            inames[idx++] = new IndexedName(kv.getValue().index, kv.getKey());

        }
        Arrays.sort(inames);
        String[] names = new String[inames.length];
        for (int i = 0; i < inames.length; ++i) {
            names[i] = inames[i].name;
        }
        return names;
    }

    /**
     *
     * @param <S>
     * @param name
     * @param sclass
     * @return
     */
    @SuppressWarnings("unchecked")
    public <S> S deepSearch(String name, Class<S> sclass) {
        IndexedObject<?> iobj = information_.get(name);
        if (iobj != null) {
            return convert(iobj.obj, sclass);
        } else {
            List<Information<InformationSet>> subsets = this.select(InformationSet.class);
            if (subsets.isEmpty()) {
                return null;

            } else {
                for (Information<InformationSet> subset : subsets) {
                    S rslt = subset.value.deepSearch(name, sclass);
                    if (rslt != null) {
                        return rslt;

                    }
                }
                return null;
            }
        }
    }

    private <S> void fillSelection(String path, InformationSet set, List<Information<S>> sel, Class<S> sclass) {
        for (Entry<String, IndexedObject<?>> kv : set.information_.entrySet()) {
            S s = convert(kv.getValue().obj, sclass);
            if (s != null) {
                sel.add(new Information<>(item(path, kv.getKey()), s,
                        kv.getValue().index));
            }
        }
        List<Information<InformationSet>> subsets = set.select(InformationSet.class);
        if (!subsets.isEmpty()) {
            for (Information<InformationSet> subset : subsets) {
                fillSelection(item(path, subset.name), subset.value, sel, sclass);
            }
        }
    }

    private <S> void fillSelection(String path, InformationSet set, List<Information<S>> sel, WildCards wc, Class<S> sclass) {
        for (Entry<String, IndexedObject<?>> kv : set.information_.entrySet()) {
            if (wc.match(kv.getKey())) {
                S s = convert(kv.getValue().obj, sclass);
                if (s != null) {
                    sel.add(new Information<>(item(path, kv.getKey()), s,
                            kv.getValue().index));
                }
            }
        }
        List<Information<InformationSet>> subsets = set.select(InformationSet.class);
        if (!subsets.isEmpty()) {
            for (Information<InformationSet> subset : subsets) {
                fillSelection(item(path, subset.name), subset.value, sel, wc, sclass);
            }
        }
    }

    public <S> List<Information<S>> deepSelect(String wc, Class<S> sclass) {
        ArrayList<Information<S>> list = new ArrayList<>();
        WildCards w = new WildCards(wc);
        fillSelection(null, this, list, w, sclass);
        java.util.Collections.sort(list, new InformationComparer<S>());
        return list;
    }

    public <S> List<Information<S>> deepSelect(Class<S> sclass) {
        ArrayList<Information<S>> list = new ArrayList<>();
        fillSelection(null, this, list, sclass);
        java.util.Collections.sort(list, new InformationComparer<S>());
        return list;
    }

    /**
     *
     * @return
     */
    public List<String> errors() {
        return allItems(ERRORS);
    }

    /**
     *
     * @param prefix
     * @param items
     */
    public void fillDictionary(String prefix, List<String> items) {
        ArrayList<IndexedName> pnames = new ArrayList<>();
        ArrayList<IndexedName> snames = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                snames.add(new IndexedName(kv.getValue().index, kv.getKey()));

            } else {
                pnames.add(new IndexedName(kv.getValue().index, kv.getKey()));

            }
        }
        java.util.Collections.sort(pnames);
        java.util.Collections.sort(snames);
        for (IndexedName pname : pnames) {
            items.add((prefix == null || prefix.isEmpty()) ? pname.name
                    : item(prefix, pname.name));

        }
        for (IndexedName sname : snames) {
            String nprefix = (prefix == null || prefix.isEmpty()) ? sname.name
                    : item(prefix, sname.name);
            InformationSet subset = getSubSet(sname.name);
            if (subset != null) {
                subset.fillDictionary(nprefix, items);

            }
        }
    }

    /**
     *
     * @param prefix
     * @param items
     */
    public void fillDictionary(String prefix, Map<String, Class> items) {
        ArrayList<IndexedName> pnames = new ArrayList<>();
        ArrayList<IndexedName> snames = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                snames.add(new IndexedName(kv.getValue().index, kv.getKey()));
            } else {
                pnames.add(new IndexedName(kv.getValue().index, kv.getKey()));
            }
        }
        java.util.Collections.sort(snames);
        java.util.Collections.sort(pnames);
        for (IndexedName pname : pnames) {
            Object obj = information_.get(pname.name).obj;
            if (obj != null) {
                items.put((prefix == null || prefix.isEmpty()) ? pname.name
                        : item(prefix, pname.name), obj.getClass());
            }

        }
        for (IndexedName sname : snames) {
            String nprefix = (prefix == null || prefix.isEmpty()) ? sname.name
                    : item(prefix, sname.name);
            InformationSet subset = getSubSet(sname.name);
            if (subset != null) {
                subset.fillDictionary(nprefix, items);

            }
        }
    }

    /**
     *
     * @param <T>
     * @param prefix
     * @param items
     * @param tclass
     */
    public <T> void fillDictionary(String prefix, List<String> items,
            Class<T> tclass) {
        ArrayList<IndexedName> pnames = new ArrayList<>();
        ArrayList<IndexedName> snames = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                snames.add(new IndexedName(kv.getValue().index, kv.getKey()));

            } else if (tclass.isInstance(kv.getValue().obj)) {
                pnames.add(new IndexedName(kv.getValue().index, kv.getKey()));

            }

        }
        java.util.Collections.sort(pnames);
        java.util.Collections.sort(snames);
        for (IndexedName pname : pnames) {
            items.add((prefix == null || prefix.isEmpty()) ? pname.name
                    : item(prefix, pname.name));

        }
        for (IndexedName sname : snames) {
            String nprefix = (prefix == null || prefix.isEmpty()) ? sname.name
                    : item(prefix, sname.name);
            InformationSet subset = getSubSet(sname.name);
            if (subset != null) {
                subset.fillDictionary(nprefix, items, tclass);

            }
        }
    }

    /**
     * Returns information identified by its name.
     *
     * @param name Name of the property (case insensitive).
     * @return The corresponding information or null if no information
     * corresponds to the given name.
     */
    public Object get(final String name) {
        IndexedObject<?> obj = information_.get(name);
        if (obj != null) {
            return obj.obj;

        } else {
            return null;

        }
    }

    public Object get(final Enum e) {
        return get(e.toString());
    }

    /**
     * Returns information identified by a name, provided it corresponds to a
     * given type.
     *
     * @param <S> Type of information
     * @param name Name of information (case insensitive)
     * @param sclass The class of the requested information.
     * @return The corresponding information or null if no information
     * corresponds to the given name and to the given type.
     */
    @SuppressWarnings("unchecked")
    public <S> S get(final String name, final Class<S> sclass) {
        Object obj = get(name);
        return convert(obj, sclass);
    }

    public <S> S get(final Enum e, final Class<S> sclass) {
        return get(e.toString(), sclass);
    }

    /**
     *
     * @return
     */
    public List<String> getDictionary() {
        ArrayList<String> items = new ArrayList<>();
        String prefix = null;
        fillDictionary(prefix, items);
        return items;
    }

    /**
     *
     * @param <T>
     * @param tclass
     * @return
     */
    public <T> List<String> getDictionary(Class<T> tclass) {
        ArrayList<String> items = new ArrayList<>();
        String prefix = null;
        fillDictionary(prefix, items, tclass);
        return items;
    }

    /**
     * Returns an information set corresponding to the given name.
     *
     * @param name Name o the sub-set (case insensitive).
     * @return The corresponding informationSet or null if no informationSet
     * corresponds to the given name.
     */
    public InformationSet getSubSet(final String name) {
        return get(name, InformationSet.class);
    }

    /**
     * Returns the current list of items contained in the object.
     *
     * @return The list of items. A new list is created. Calling that method in
     * an iteration could be expensive and it should be avoided.
     */
    @SuppressWarnings("unchecked")
    @NewObject
    public List<Information<?>> items() {
        ArrayList<Information<?>> list = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            list.add(new Information<>(kv.getKey(), kv.getValue().obj, kv.getValue().index));

        }
        java.util.Collections.sort(list, new InformationComparer());
        return list;
    }

    /**
     * Remove information corresponding to a given name. If the set doesn't
     * contain such information, no action is taken.
     *
     * @param name Name to be removed (case insensitive).
     */
    public void remove(final String name) {
        if (locked_) {
            return;

        }
        information_.remove(name);
    }

    public void remove(final Enum e) {
        remove(e.toString());
    }

    /**
     *
     * @param <S>
     * @param fullName
     * @param sclass
     * @return
     */
    public <S> S search(final String fullName, final Class<S> sclass) {
        String[] split = split(fullName);
        if (split.length <= 1) {
            return search(new String[]{fullName}, sclass);
        } else {
            return search(split, sclass);
        }
    }

    private InformationSet root(final String[] names) {
        InformationSet cur = this;
        if (names.length > 1) {
            for (int i = 0; i < names.length - 1; ++i) {
                cur = cur.getSubSet(names[i]);
                if (cur == null) {
                    return null;

                }
            }
        }
        return cur;
    }

    /**
     *
     * @param <S>
     * @param names
     * @param sclass
     * @return
     */
    public <S> S search(final String[] names, final Class<S> sclass) {
        InformationSet cur = root(names);
        if (cur == null) {
            return null;
        } else {
            return cur.get(names[names.length - 1], sclass);
        }
    }

    /**
     * Selects in the information set all the entries corresponding to a given
     * type
     *
     * @param <S> The requested type of information.
     * @param sclass The class that identifies th type.
     * @return A new list with the selection is returned. Callers are ensured
     * that the list will contain information that correspond to the requested
     * type (information that can be safely cast to that type).
     */
    @SuppressWarnings("unchecked")
    @NewObject
    public <S> List<Information<S>> select(final Class<S> sclass) {
        ArrayList<Information<S>> list = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : information_.entrySet()) {
            S s = convert(kv.getValue().obj, sclass);
            if (s != null) {
                list.add(new Information<>(kv.getKey(), (S) kv.getValue().obj,
                        kv.getValue().index));

            }

        }
        java.util.Collections.sort(list, new InformationComparer<S>());
        return list;
    }

    /**
     * Selects in the information set all the entries whose name match a given
     * wild cards criterion
     *
     * @param wc The wild cards criterion.
     * @return A new list with the selection is returned.
     */
    @NewObject
    public List<Information<Object>> select(final String wc) {
        String[] split = split(wc);
        InformationSet cur = root(split);
        if (cur == null) {
            return Collections.emptyList();
        }
        WildCards w = new WildCards(split[split.length - 1]);
        List<Information<Object>> list = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : cur.information_.entrySet()) {
            if (w.match(kv.getKey())) {
                list.add(new Information<>(kv.getKey(),
                        kv.getValue().obj, kv.getValue().index));

            }

        }
        java.util.Collections.sort(list, new InformationComparer<>());
        return list;
    }

    /**
     * Selects in the information set all the entries corresponding to a given
     * type and whose name match a given wild cards criterion
     *
     * @param <S> The requested type of information.
     * @param wc The wild cards criterion.
     * @param sclass The class that identifies th type.
     * @return A new list with the selection is returned.
     */
    @SuppressWarnings("unchecked")
    @NewObject
    public <S> List<Information<S>> select(final String wc,
            final Class<S> sclass) {
        String[] split = split(wc);
        InformationSet cur = root(split);
        if (cur == null) {
            return Collections.emptyList();
        }
        WildCards w = new WildCards(split[split.length - 1]);
        ArrayList<Information<S>> list = new ArrayList<>();
        for (Entry<String, IndexedObject<?>> kv : cur.information_.entrySet()) {
            if (w.match(kv.getKey())) {
                S s = convert(kv.getValue().obj, sclass);
                if (s != null) {
                    list.add(new Information<>(kv.getKey(), (S) kv.getValue().obj,
                            kv.getValue().index));
                }
            }
        }
        java.util.Collections.sort(list, new InformationComparer<S>());
        return list;
    }

    /**
     * Sets in the set a new information
     *
     * @param <S> The type of information
     * @param name The name of information
     * @param info Information
     * @return True is returned if the information has been successfully set.
     * False otherwise. Information can be set in the two following cases: - The
     * set doesn't contain any entry with the same name. - If the set contains
     * an entry with the same name, it also has the same type. In the first
     * case, information is added; in the second case, the new information
     * replaces the previous one.
     */
    @SuppressWarnings("unchecked")
    public <S> boolean set(final String name, final S info) {
        if (locked_) {
            return false;

        }
        IndexedObject obj = information_.get(name);
        if (obj != null && !obj.obj.getClass().isInstance(info)) {
            return false;

        }
        information_.put(name, new IndexedObject(info));
        return true;
    }

    public <S> boolean set(final Enum e, final S info) {
        return set(e.toString(), info);
    }

    /**
     * Returns an information set corresponding to the given name.
     *
     * @param name Name o the sub-set (case insensitive).
     * @return If the set doesn't contain any corresponding entry, a new
     * information sub-set is created, added to the set and returned. If the set
     * contains an entry with the given name, the corresponding information is
     * returned if it has the right type and null is returned otherwise.
     */
    public InformationSet subSet(final String name) {
        IndexedObject<?> obj = information_.get(name);
        if (obj == null) {
            if (!locked_) {
                InformationSet nset = new InformationSet();
                information_.put(name, new IndexedObject<>(nset));
                return nset;
            } else {
                return null;
            }
        } else if (obj.obj instanceof InformationSet) {
            return (InformationSet) obj.obj;

        } else {
            return null;

        }
    }

    /**
     *
     * @return
     */
    public List<String> warnings() {
        return allItems(WARNINGS);
    }

    public void setContent(String type, String version) {
        InformationSet cnt = subSet(CONTENT);
        cnt.set(TYPE_KEY, type);
        if (version != null) {
            cnt.setContent(VERSION_KEY, version);
        }
    }

    public String getContentType() {
        InformationSet cnt = getSubSet(CONTENT);
        if (cnt == null) {
            return null;
        }
        return cnt.get(TYPE_KEY, String.class);
    }

    public String getContentVersion() {
        InformationSet cnt = getSubSet(CONTENT);
        if (cnt == null) {
            return null;
        }
        return cnt.get(VERSION_KEY, String.class);
    }

    public boolean isContent(String type, String version) {
        InformationSet cnt = getSubSet(CONTENT);
        if (cnt == null) {
            return false;
        }
        if (!type.equalsIgnoreCase(cnt.get(TYPE_KEY, String.class))) {
            return false;
        }
        if (version != null && !version.equalsIgnoreCase(cnt.get(VERSION_KEY, String.class))) {
            return false;
        }
        return true;
    }

    public InformationSet filter(List<String> dictionary) {
        InformationSet info = new InformationSet();
        for (String s : dictionary) {
            String[] path = split(s);
            Object item = search(path, Object.class);
            if (item != null) {
                info.add(path, item);
            }
        }
        return info;
    }

    public boolean merge(InformationSet info) {
        for (Entry<String, IndexedObject<?>> kv : info.information_.entrySet()) {
            if (!information_.containsKey(kv.getKey())) {
                information_.put(kv.getKey(), new IndexedObject(kv.getValue().obj));
            } else // merge subset
            if (InformationSet.class.isInstance(kv.getValue().obj)) {
                InformationSet subset = getSubSet(kv.getKey());
                if (subset == null) {
                    return false;
                } else {
                    subset.merge((InformationSet) kv.getValue().obj);
                }
            } else {
                // merge warnings and errors
                switch (kv.getKey()) {
                    case WARNINGS:
                        this.addMessages(WARNINGS, (String[]) kv.getValue().obj);
                        break;
                    case ERRORS:
                        this.addMessages(ERRORS, (String[]) kv.getValue().obj);
                        break;
                    default:
                        return false;
                }
            }
        }
        return true;
    }

    public boolean update(InformationSet info) {
        for (Entry<String, IndexedObject<?>> kv : info.information_.entrySet()) {
            if (kv.getValue().obj instanceof InformationSet) {
                InformationSet subset = subSet(kv.getKey());
                if (subset == null) {
                    return false;
                } else {
                    subset.update((InformationSet) kv.getValue().obj);
                }
            } else {
                switch (kv.getKey()) {
                    case WARNINGS:
                        this.addMessages(WARNINGS, (String[]) kv.getValue().obj);
                        break;
                    case ERRORS:
                        this.addMessages(ERRORS, (String[]) kv.getValue().obj);
                        break;
                    default:
                        information_.put(kv.getKey(), new IndexedObject(kv.getValue().obj));
                        break;
                }
            }

        }
        return true;
    }

    /////////// History
    public <S> boolean addLog(final String name, final S info) {
        return subSet(LOG).add(name, info);
    }

    public <S> boolean setLog(final String name, final S info) {
        return subSet(LOG).set(name, info);
    }

    public InformationSet getLog() {
        return getSubSet(LOG);
    }

    public static <T> T convert(Object obj, Class<T> tclass) {
        if (obj == null) {
            return null;
        }
        if (tclass.isInstance(obj)) {
            return (T) obj;
        } else if (tclass.isArray()) {
            Class oclass = obj.getClass();
            if (oclass.isArray()) {
                int len = Array.getLength(obj);
                Object nobj = Array.newInstance(tclass.getComponentType(), len);
                try {
                    System.arraycopy(obj, 0, nobj, 0, len);
                    return (T) nobj;
                } catch (ArrayStoreException ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
