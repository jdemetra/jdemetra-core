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
package ec.tss;

import com.google.common.base.Strings;
import ec.tss.tsproviders.utils.MultiLineNameUtil;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ec.tstoolkit.IDocumented;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDataTable;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public final class TsCollection implements ITsIdentified, IDocumented,
        Iterable<Ts> {

    private final TsMoniker m_moniker;
    private final String m_name;
    private MetaData m_metadata;
    private final List<Ts> m_ts = new ArrayList<>();
    private volatile TsInformationType m_info;
    private volatile Set<TsMoniker> m_set;
    private String m_invalidDataCause;

    TsCollection(String name) {
        m_name = Strings.nullToEmpty(name);
        m_moniker = new TsMoniker();
        m_info = TsInformationType.UserDefined;
    }

    TsCollection(String name, TsMoniker moniker) {
        m_name = name;
        m_moniker = moniker;
        if (m_moniker.getSource() == null) {
            m_info = TsInformationType.UserDefined;
        } else {
            m_info = TsInformationType.None;
        }
    }

    TsCollection(String name, TsMoniker moniker, MetaData md, Iterable<Ts> ts) {
        m_name = name;
        m_moniker = moniker;
        m_metadata = md;
        if (ts != null) {
            for (Ts s : ts) {
                m_ts.add(s);
            }
        }
        m_info = TsInformationType.UserDefined;
    }

    @Override
    public String getName() {
        return m_name;
    }

    public String getInvalidDataCause() {
        return m_invalidDataCause;
    }

    public void setInvalidDataCause(String message) {
        m_invalidDataCause = message;
    }

    /**
     *
     * @param ts
     * @return
     */
    public boolean add(Ts ts) {
        if (quietAdd(ts)) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
            return true;
        } else {
            return false;
        }
    }

    public boolean quietAdd(Ts ts) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            if (contains(ts)) {
                return false;
            }
            m_ts.add(ts);
            m_set = null;
        }
        return true;
    }

    /**
     *
     * @param list
     * @return
     */
    public int append(Iterable<? extends Ts> list) {
        int n = quietAppend(list);
        if (n > 0) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
        }
        return n;
    }

    public int quietAppend(Iterable<? extends Ts> list) {
        if (isLocked()) {
            return 0;
        }
        synchronized (m_moniker) {
            List<Ts> tmp = new ArrayList<>();
            for (Ts s : list) {
                if (!contains(s) && !tmp.contains(s)) {
                    tmp.add(s);
                }
            }
            if (!tmp.isEmpty()) {
                m_ts.addAll(tmp);
                m_set = null;
                return tmp.size();
            } else {
                return 0;
            }
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public int append(TsCollection c) {
        return append(c.m_ts);
    }

    public int quietAppend(TsCollection c) {
        return quietAppend(c.m_ts);
    }

    private void buildSet() {
        m_set = new HashSet<>(m_ts.size());
        for (Ts s : m_ts) {
            m_set.add(s.getMoniker());
        }
    }

    /**
     *
     * @param empty
     * @return
     */
    public TsCollection clean(boolean empty) {
        synchronized (m_moniker) {
            List<Ts> list = new ArrayList(m_ts.size());
            for (Ts s : m_ts) {
                if (s.hasData() == TsStatus.Valid) {
                    list.add(s);
                } else if (s.hasData() == TsStatus.Undefined) {
                    s.load(TsInformationType.Data);
                    if (s.hasData() == TsStatus.Valid) {
                        list.add(s);
                    }
                }
            }
            if (list.isEmpty() && !empty) {
                return null;
            }
            return TsFactory.instance.createTsCollection(m_name, new TsMoniker(), null,
                    list);
        }
    }

    /**
     *
     * @param cntVal
     * @param empty
     * @return
     */
    public TsCollection clean(double cntVal, boolean empty) {
        synchronized (m_moniker) {
            List<Ts> list = new ArrayList(m_ts.size());
            for (Ts s : m_ts) {
                if (s.hasData() == TsStatus.Undefined) {
                    s.load(TsInformationType.Data);
                }
                if (s.hasData() == TsStatus.Valid) {
                    TsData data = s.getTsData();
                    for (int i = 0; i < data.getLength(); ++i) {
                        double d = data.get(i);
                        if (!Double.isNaN(d) && d != cntVal) {
                            list.add(s);
                            break;
                        }
                    }
                }
            }
            if (list.isEmpty() && !empty) {
                return null;
            }
            return TsFactory.instance.createTsCollection(m_name, new TsMoniker(), null,
                    list);
        }
    }

    /**
     *
     */
    public void clear() {
        if (isLocked()) {
            return;
        }
        synchronized (m_moniker) {
            if (m_ts.isEmpty()) {
                return;
            }
            m_ts.clear();
            m_set = null;
        }
        TsFactory.instance.notify(this, TsInformationType.Definition, this);
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean contains(Ts s) {
        synchronized (m_moniker) {
            if (m_set == null) {
                buildSet();
            }
            return m_set.contains(s.getMoniker());
        }
    }

    public Ts[] retain(Iterable<Ts> all) {
        synchronized (m_moniker) {
            if (all == null) {
                return new Ts[0];
            } else {
                if (m_set == null) {
                    buildSet();
                }
                return StreamSupport.stream(all.spliterator(), false)
                        .filter(o -> m_set.contains(o.getMoniker()))
                        .toArray(Ts[]::new);
            }
        }
    }

    /**
     *
     * @return
     */
    public Ts[] toArray() {
        synchronized (m_moniker) {
            return m_ts.toArray(new Ts[m_ts.size()]);
        }
    }

    public int indexOf(Ts ts) {
        synchronized (m_moniker) {
            for (int i = 0; i < m_ts.size(); i++) {
                if (ts.equals(m_ts.get(i))) {
                    return i;
                }
            }
            return -1;
        }
    }

    /**
     *
     * @param idx
     * @return
     */
    public Ts get(int idx) {
        synchronized (m_moniker) {
            return m_ts.get(idx);
        }
    }

    /**
     * Gets all the available data in the collection.
     *
     * @return A list containing the data. Missing series are not included in
     * the list, so that the number of items in the list could differ from the
     * number of items in the collection.
     */
    public List<TsData> getAllData() {
        Ts[] all = this.toArray();
        ArrayList<TsData> data = new ArrayList<>();
        for (int i = 0; i < all.length; ++i) {
            if (all[i].hasData() == TsStatus.Undefined) {
                all[i].load(TsInformationType.Data);
            }
            if (all[i].hasData() == TsStatus.Valid) {
                data.add(all[i].getTsData());
            }
        }
        return data;
    }

    /**
     *
     * @return
     */
    public int getCount() {
        synchronized (m_moniker) {
            return m_ts.size();
        }
    }

    public boolean isEmpty() {
        synchronized (m_moniker) {
            return m_ts.isEmpty();
        }
    }

    /**
     *
     * @return
     */
    public TsInformationType getInformationType() {
        synchronized (m_moniker) {
            return m_info;
        }
    }

    @Override
    public MetaData getMetaData() {
        synchronized (m_moniker) {
            return m_metadata;
        }
    }

    @Override
    public TsMoniker getMoniker() {
        return m_moniker;
    }

    /**
     *
     * @return
     */
    public TsStatus hasMetaData() {
        if (m_info == TsInformationType.All
                || m_info == TsInformationType.MetaData
                || m_info == TsInformationType.UserDefined) {
            return m_metadata == null ? TsStatus.Invalid : TsStatus.Valid;
        } else {
            return TsStatus.Undefined;
        }
    }

    /**
     *
     * @param pos
     * @param ts
     * @return
     */
    public boolean insert(int pos, Ts ts) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            if (contains(ts)) {
                return false;
            }
            m_ts.add(pos, ts);
            m_set = null;
        }
        TsFactory.instance.notify(this, TsInformationType.Definition, this);
        return true;
    }

    /**
     *
     * @return
     */
    public boolean isLocked() {
        return m_moniker.getSource() != null;
    }

    @Override
    public Iterator<Ts> iterator() {
        return m_ts.iterator();
    }

    @Nonnull
    public Stream<Ts> stream() {
        return m_ts.stream();
    }

    /**
     *
     * @param type
     * @return
     */
    public boolean load(TsInformationType type) {
        // check if the information is available...
        if (m_info.encompass(type)) {
            return true;
        }
        return TsFactory.instance.load(this, type);
    }

    /**
     *
     * @return
     */
    public TsCollection makeCopy() {
        TsCollection coll = TsFactory.instance.createTsCollection(m_name);
        if (m_metadata != null) {
            coll.m_metadata = m_metadata.clone();
        }
        coll.m_ts.addAll(m_ts);
        return coll;
    }

    /**
     *
     * @param type
     * @return
     */
    public boolean query(TsInformationType type) {
        // check if the information is available...
        if (m_info.encompass(type)) {
            return true;
        }
        return TsFactory.instance.query(this, type);
    }

    /**
     *
     * @param type
     * @return
     */
    public boolean reload(TsInformationType type) {
        return TsFactory.instance.load(this, type);
    }

    /**
     *
     * @param c
     * @return
     */
    public int remove(Iterable<Ts> c) {
        if (isLocked()) {
            return 0;
        }
        int rslt = 0;
        synchronized (m_moniker) {
            List<Ts> tmp = new ArrayList<>();
            for (Ts s : c) {
                if (contains(s) && !tmp.contains(s)) {
                    tmp.add(s);
                }
            }
            if (!tmp.isEmpty()) {
                for (Ts s : tmp) {
                    m_ts.remove(s);
                }
                m_set = null;
                rslt = tmp.size();
            }
        }
        if (rslt > 0) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
        }
        return rslt;
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean remove(Ts s) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            if (!contains(s)) {
                return false;
            }
            m_ts.remove(s);
            m_set = null;
        }
        TsFactory.instance.notify(this, TsInformationType.Definition, this);
        return true;
    }

    /**
     *
     * @param pos
     */
    public void removeAt(int pos) {
        if (isLocked()) {
            return;
        }
        synchronized (m_moniker) {
            m_ts.remove(pos);
            m_set = null;
        }
        TsFactory.instance.notify(this, TsInformationType.Definition, this);
    }

    /**
     *
     * @param start
     * @param n
     */
    public void removeRange(int start, int n) {
        if (isLocked()) {
            return;
        }
        synchronized (m_moniker) {
            for (int pos = start + n - 1; pos >= start; --pos) {
                m_ts.remove(pos);
            }
            m_set = null;
        }
        TsFactory.instance.notify(this, TsInformationType.Definition, this);
    }

    /**
     *
     * @param c
     * @return
     */
    public boolean replace(Iterable<Ts> c) {
        if (quietReplace(c)) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public boolean quietReplace(Iterable<Ts> c) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            m_ts.clear();
            for (Ts s : c) {
                m_ts.add(s);
            }
            m_set = null;
        }
        return true;
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean replace(Ts s) {
        if (quietReplace(s)) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param s
     * @return
     */
    public boolean replace(Ts... s) {
        if (quietReplace(s)) {
            TsFactory.instance.notify(this, TsInformationType.Definition, this);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param c
     * @return
     */
    public boolean quietReplace(Ts... c) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            m_ts.clear();
            if (c != null) {
                m_ts.addAll(Arrays.asList(c));
            }
            m_set = null;
        }
        return true;
    }

    public boolean quietReplace(Ts s) {
        if (isLocked()) {
            return false;
        }
        synchronized (m_moniker) {
            if (m_ts.size() == 1
                    && m_ts.get(0).getMoniker().equals(s.getMoniker())) {
                return true;
            }
            m_ts.clear();
            m_ts.add(s);
            m_set = null;
        }
        return true;
    }

    /**
     * Search a series by its name
     * @param name The name of the series
     * @return The first series with the given name is returned
     */
    public Ts search(String name) {
        synchronized (m_moniker) {
            for (Ts s : m_ts) {
                if (s.getName().equals(name)) {
                    return s;
                }
            }
            return null;
        }
    }

    /**
     *
     * @param moniker
     * @return
     */
    public Ts search(TsMoniker moniker) {
        synchronized (m_moniker) {
            for (Ts s : m_ts) {
                if (s.getMoniker().equals(moniker)) {
                    return s;
                }
            }
            return null;
        }
    }

    public boolean rename(Ts s, String newname) {
        synchronized (m_moniker) {
            int pos = m_ts.indexOf(s);
            if (pos < 0) {
                return false;
            }
            m_ts.set(pos, m_ts.get(pos).rename(newname));
            return true;
        }
    }

    /**
     *
     * @param md
     * @return
     */
    public boolean set(MetaData md) {
        if (m_info != TsInformationType.UserDefined) {
            return false;
        }
        synchronized (m_moniker) {
            m_metadata = md;
        }
        TsFactory.instance.notify(this, TsInformationType.MetaData, this);
        return true;
    }

    List<Ts> update(TsCollectionInformation info) {
        synchronized (m_moniker) {
            List<Ts> updated;
            if (info.hasMetaData()) {
                m_metadata = info.metaData;
            }
            if (info.hasDefinition()) {
                // load the series...
                m_ts.clear();
                if (!info.items.isEmpty()) {
                    updated = new ArrayList(info.items.size());
                    for (TsInformation sinfo : info.items) {
                        Ts s = TsFactory.instance.getTs(sinfo.moniker);
                        if (s != null) {
                            s.getMaster().update(sinfo);
                            if (updated != null) {
                                updated.add(s);
                            }
                        } else {
                            s = TsFactory.instance.createTs(sinfo);
                        }
                        m_ts.add(s);
                    }
                } else {
                    updated = Collections.emptyList();
                }
            } else {
                updated = Collections.emptyList();
            }
            m_info = m_info.union(info.type);
            m_invalidDataCause = info.invalidDataCause;
            return updated;
        }
    }

    @Override
    public String toString() {
        synchronized (m_moniker) {
            String[] headers = new String[m_ts.size()];
            List<TsData> all = getAllData();
            for (int i=0; i<headers.length; ++i){
                headers[i]=MultiLineNameUtil.last(m_ts.get(i).getName());
            }
            TsDataTable table=new TsDataTable();
            table.add(all);
            return table.toString(headers);
        }
    }
}
