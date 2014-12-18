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
package ec.tss.documents;

import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsFactory;
import ec.tss.TsMoniker;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.algorithm.IProcDocument;
import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.SeriesInfo;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Scanner;

/**
 *
 * @author Jean Palate
 */
@Singleton
@Development(status = Development.Status.Alpha)
public final class DocumentManager extends Observable {

    public static final String COMPONENT = "@component", INFO = "@info";
    public static final String COMPOSITE = "@composite@";

    static {
        DocumentManager mgr = new DocumentManager();
        instance = mgr;
    }
    public static final DocumentManager instance;

    void remove(long id) {
        synchronized (tsMap_) {
            tsMap_.remove(id);
        }
    }

    static class CompositeTs {

        static CompositeTs decode(String str) {
            CompositeTs cmp = new CompositeTs();
            int cur = 0;
            int pos = str.indexOf('=');
            if (pos < 0) {
                return null;
            } else if (pos > cur) {
                cmp.name = str.substring(cur, pos);
            }
            cur = pos + 1;
            pos = str.indexOf(',', cur);
            if (pos < 0) {
                return null;
            } else if (pos > cur) {
                cmp.back = str.substring(cur, pos);
            }
            cur = pos + 1;
            pos = str.indexOf(',', cur);
            if (pos < 0) {
                return null;
            } else if (pos > cur) {
                cmp.now = str.substring(cur, pos);
            }
            cur = pos + 1;
            if (cur < str.length()) {
                cmp.fore = str.substring(cur);
            }
            return cmp;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(name).append('=');
            if (back != null) {
                builder.append(back);
            }
            builder.append(',');
            if (now != null) {
                builder.append(now);
            }
            builder.append(',');
            if (fore != null) {
                builder.append(fore);
            }
            return builder.toString();
        }
        String back, now, fore;
        String name;
        TsMoniker moniker;
    }

    private DocumentManager() {
    }
    private final HashMap<Long, HashMap<String, TsMoniker>> tsMap_ =
            new HashMap<>();
    private final HashMap<Long, HashMap<String, CompositeTs>> xtsMap_ =
            new HashMap<>();

//    private int cur_;
//    private static final int MAX = 20;
    public void update(ActiveDocument<?, ?, ?> doc) {
        synchronized (tsMap_) {
            HashMap<String, TsMoniker> map = tsMap_.get(doc.getKey());
            if (map != null) {
                HashMap<String, TsMoniker> tmp =
                        new HashMap<>();

                for (Entry<String, TsMoniker> kv : map.entrySet()) {
                    Ts s = TsFactory.instance.getTs(kv.getValue());
                    if (s != null) {
                        tmp.put(kv.getKey(), kv.getValue());
                        IProcResults r = doc.getResults();
                        if (r != null) {
                            s.set(r.getData(kv.getKey(), TsData.class));
                        } else {
                            s.set((TsData) null);
                        }
                    }
                }
                if (tmp.isEmpty()) {
                    tsMap_.remove(doc.getKey());
                } else {
                    tsMap_.put(doc.getKey(), tmp);
                }
            }
        }
        synchronized (xtsMap_) {
            HashMap<String, CompositeTs> map = xtsMap_.get(doc.getKey());
            if (map != null) {
                HashMap<String, CompositeTs> tmp =
                        new HashMap<>();
                for (Entry<String, CompositeTs> item : map.entrySet()) {
                    Ts s = TsFactory.instance.getTs(item.getValue().moniker);
                    if (s != null) {
                        tmp.put(item.getKey(), item.getValue());
                        IProcResults r = doc.getResults();
                        fillComposite(s, doc, item.getValue());
                    }
                }
                if (tmp.isEmpty()) {
                    xtsMap_.remove(doc.getKey());
                } else {
                    xtsMap_.put(doc.getKey(), tmp);
                }
            }
        }
    }

    private void fillComposite(Ts s, IProcDocument<?, ?, ?> doc, CompositeTs item) {
        TsData data = null;
        IProcResults rslts = doc.getResults();
        Day beg = null, end = null;
        if (rslts != null) {
            TsData b = null, n = null, f = null;
            if (item.back != null) {
                b = rslts.getData(item.back, TsData.class);
            }
            if (item.now != null) {
                n = rslts.getData(item.now, TsData.class);
                if (n != null) {
                    beg = n.getStart().firstday();
                    end = n.getLastPeriod().lastday();
                }
            }
            if (item.fore != null) {
                f = rslts.getData(item.fore, TsData.class);
            }
            data = TsData.concatenate(b, n);
            data = TsData.concatenate(data, f);
        }
        MetaData md = s.getMetaData();
        if (md == null) {
            md = new MetaData();
        }
        if (beg != null) {
            md.put(Ts.BEG, beg.toString());
        } else {
            md.remove(Ts.BEG);
        }
        if (end != null) {
            md.put(Ts.END, end.toString());
        } else {
            md.remove(Ts.END);
        }
        s.set(data, md);
    }

    void notify(DocumentEvent event) {
        setChanged();
        notifyObservers(event);
    }

    private Ts getCompositeTs(IProcDocument<?, ?, ?> doc, String name) {
        synchronized (xtsMap_) {
            CompositeTs cmp = CompositeTs.decode(name);
            if (cmp == null) {
                return null;
            }
            String cid = cmp.toString();
            HashMap<String, CompositeTs> map = xtsMap_.get(doc.getKey());
            if (map != null) {
                CompositeTs mts = map.get(cid);
                if (mts != null) {
                    Ts ts = TsFactory.instance.getTs(mts.moniker);
                    if (ts != null) {
                        return ts;
                    }
                }
            } else {
                map = new HashMap<>();
                xtsMap_.put(doc.getKey(), map);
            }

            // creates the series
            TsMoniker m = TsMoniker.createDynamicMoniker();
            MetaData md = new MetaData();
            md.put(MetaData.ALGORITHM, doc.getDescription());
            Ts ts = TsFactory.instance.createTs(cmp.name, m, md, null);
            cmp.moniker = ts.getMoniker();
            fillComposite(ts, doc, cmp);
            map.put(cid, cmp);
            return ts;
        }
    }

    public Ts getTs(IProcDocument<?, ?, ?> doc, String id) {
        return getTs(doc, id, false);
    }

    public Ts getTs(IProcDocument<?, ?, ?> doc, String id, boolean fullName) {
        if (id.startsWith(COMPOSITE)) {
            return getCompositeTs(doc, id.substring(COMPOSITE.length()));
        }

        if (doc instanceof ActiveDocument) {
            return getActiveTs((ActiveDocument) doc, id, fullName);
        } else {
            return getStaticTs(doc, id, fullName);
        }
    }

    private Ts getActiveTs(ActiveDocument<?, ?, ?> doc, String id, boolean fullName) {

        synchronized (tsMap_) {
            HashMap<String, TsMoniker> map = tsMap_.get(doc.getKey());
            if (map != null) {
                TsMoniker mts = map.get(id);
                if (mts != null) {
                    Ts ts = TsFactory.instance.getTs(mts);
                    if (ts != null) {
                        return ts;
                    }
                }
            } else {
                map = new HashMap<>();
                tsMap_.put(doc.getKey(), map);
            }

            // creates the series
            Ts ts = getStaticTs(doc, id, fullName);
            map.put(id, ts.getMoniker());
            return ts;
        }
    }

    private Ts getStaticTs(IProcDocument<?, ?, ?> doc, String id, boolean fullName) {
        TsData data = null;
        IProcResults rslts = doc.getResults();
        if (rslts != null) {
            data = rslts.getData(id, TsData.class);
        }
        String[] ids = InformationSet.split(id);
        TsMoniker m = TsMoniker.createDynamicMoniker();
        MetaData md = new MetaData();
        md.put(MetaData.ALGORITHM, doc.getDescription());
        String name = fullName ? id : ids[ids.length - 1];

        Ts ts = TsFactory.instance.createTs(name, m, md, data);
        return ts;
    }

    public static TsCollection create(List<SeriesInfo> items, IProcDocument doc, String prefix, boolean desc) {
        TsCollection collection = TsFactory.instance.createTsCollection();
        for (SeriesInfo item : items) {
            Ts s = DocumentManager.instance.getTs(doc, InformationSet.item(prefix, item.getCode()));
            if (desc) {
                if (s.getMetaData() == null) {
                    MetaData md = new MetaData();
                    md.put(COMPONENT, item.component.toString());
                    md.put(INFO, item.info.toString());
                    if (!desc) {
                        md.put(MetaData.DESCRIPTION, item.toString());
                    }
                    s.set(md);
                }
            }
            collection.quietAdd(s);
        }
        return collection;
    }

    public static TsCollection create(List<String> items, IProcDocument doc) {
        TsCollection collection = TsFactory.instance.createTsCollection();
        for (String item : items) {
            Ts s = DocumentManager.instance.getTs(doc, item);
            collection.quietAdd(s);
        }
        return collection;
    }
}
