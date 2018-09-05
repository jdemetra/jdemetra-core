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

import ec.tss.tsproviders.DataSet;
import ec.tss.tsproviders.DataSource;
import ec.tss.tsproviders.HasDataSourceList;
import ec.tss.tsproviders.IDataSourceListener;
import ec.tss.tsproviders.IDataSourceProvider;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.InterfaceLoader;
import ec.tstoolkit.design.Internal;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.design.Singleton;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@Singleton
@Development(status = Development.Status.Alpha)
public class TsFactory {

    class NotificationsQueue extends Observable implements Runnable {

        final Thread notificationThread;
        private final ArrayDeque<TsEvent> m_events = new ArrayDeque<>();

        NotificationsQueue() {
            notificationThread = new Thread(this);
            notificationThread.setDaemon(true);
        }

        void add(TsEvent event) {
            synchronized (m_events) {
                boolean empty = m_events.isEmpty();
                // push add at the beginning. should be added at the end!!!
                // in the dispatching routine, this event may be removed and never dispatched
                m_events.add(event);
//                m_events.push(event);
                if (empty) {
                    LockSupport.unpark(notificationThread);
                }
            }
        }

        void dispatch(TsEvent event) {
            synchronized (m_events) {
                setChanged();
                try {
                    this.notifyObservers(event);
                } catch (RuntimeException ex) {
                }
            }
        }

        private TsEvent nextEvent() {
            synchronized (m_events) {
                if (m_events.isEmpty()) {
                    return null;
                } else {
                    return m_events.peek();
                }
            }
        }

        private void popEvent() {
            synchronized (m_events) {
                m_events.pop();
            }
        }

        @Override
        public void run() {
            while (true) {
                if (isClosing()) {
                    return;
                }
                TsEvent nev = nextEvent();
                if (nev == null) {
                    LockSupport.park();
                } else {
                    this.dispatch(nev);
                    this.popEvent();
                }
            }
        }

        void start() {
            notificationThread.start();
        }
    }

    class TsFactoryCleaner extends Thread {

        public TsFactoryCleaner() {
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (true) {
                if (isClosing()) {
                    return;
                }
                cleanTS();
                cleanTSCollection();
                LockSupport.parkNanos(10000000000l);
            }
        }
    }
    /**
     *
     */
    public static final TsFactory instance;

    static {
        TsFactory cur = new TsFactory();
        cur.start();
        instance = cur;
    }

    private final HashMap<String, ITsProvider> m_providers = new HashMap<>();
    private final HashMap<TsMoniker, WeakReference<TsCollection>> m_collections = new HashMap<>();
    private final HashMap<TsMoniker, WeakReference<Ts.Master>> m_ts = new HashMap<>();
    private boolean m_close;
    private final long m_threadID;
    private boolean m_useSynchronousNotifications = true;
    NotificationsQueue notifications = new NotificationsQueue();
    TsFactoryCleaner cleaner = new TsFactoryCleaner();
    private final ReloadListener reloadListener = new ReloadListener();
    private final Ts.FactoryCallback tsCallback = new TsCallback();
    private final TsCollection.FactoryCallback tsCollectionCallback = new TsCollectionCallback();

    private TsFactory() {
        m_threadID = Thread.currentThread().getId();
    }

    /**
     *
     * @param provider
     * @return
     */
    public final boolean add(ITsProvider provider) {
        if (m_providers.containsKey(provider.getSource())) {
            return false;
        }
        m_providers.put(provider.getSource(), provider);
        if (provider instanceof HasDataSourceList) {
            ((HasDataSourceList) provider).addDataSourceListener(reloadListener);
        }
        return true;
    }

    public final boolean addAll(Iterable<? extends ITsProvider> list) {
        boolean result = false;
        for (ITsProvider element : list) {
            result |= add(element);
        }
        return result;
    }

    private void addEvent(TsEvent ev) {
        // Synchronous call if this is the main thread
        if (m_useSynchronousNotifications
                && m_threadID == Thread.currentThread().getId()) {
            notifications.dispatch(ev);
        } else {
            notifications.add(ev);
        }
    }

    /**
     *
     * @param obs
     */
    public void addObserver(Observer obs) {
        notifications.addObserver(obs);
    }

    private void cleanTS() {
        synchronized (m_ts) {
            HashMap<TsMoniker, WeakReference<Ts.Master>> tmp = new HashMap<>();
            int n = 0;
            for (Entry<TsMoniker, WeakReference<Ts.Master>> r : m_ts.entrySet()) {
                if (r.getValue().get() != null) {
                    tmp.put(r.getKey(), r.getValue());
                } else {
                    ++n;
                }
            }
            m_ts.clear();
            m_ts.putAll(tmp);
        }
    }

    private void cleanTSCollection() {
        synchronized (m_collections) {
            HashMap<TsMoniker, WeakReference<TsCollection>> tmp = new HashMap<>();
            m_collections.forEach((k, v) -> {
                if (v.get() != null) {
                    tmp.put(k, v);
                }
            });
            m_collections.clear();
            m_collections.putAll(tmp);
        }
    }

    /**
     *
     */
    public void clear() {
        m_providers.values().forEach(ITsProvider::dispose);
        m_providers.clear();
    }

    /**
     *
     */
    public void clearCache() {
        m_providers.forEach((k, v) -> v.clearCache());
        cleanTSCollection();
        cleanTS();
    }

    /**
     * Creates a new time series with an empty name.
     *
     * @return a new time series
     */
    @Nonnull
    @NewObject
    public Ts createTs() {
        return createTs("");
    }

    /**
     * Creates a new time series with the specified name.
     *
     * @param name the time series name; null replaced by empty string
     * @return a new time series
     */
    @Nonnull
    @NewObject
    public Ts createTs(@Nullable String name) {
        synchronized (m_ts) {
            Ts.Master ts = new Ts.Master(tsCallback, name);
            m_ts.put(ts.getMoniker(), new WeakReference<>(ts));
            return ts;
        }
    }

    Ts createTs(TsInformation info) {
        synchronized (m_ts) {
            Ts.Master ts = null;
            WeakReference<Ts.Master> wref = m_ts.get(info.moniker);
            if (wref != null) {
                ts = wref.get();
            }
            if (ts == null) {
                ts = new Ts.Master(tsCallback, info.name, info.moniker);
            }
            ts.update(info);
            m_ts.put(ts.getMoniker(), new WeakReference<>(ts));
            return ts;
        }
    }

    /**
     * Creates a new time series with the specified name, metadata and data.
     *
     * @param name the time series name; null replaced by empty string
     * @param md the metadata; null permitted
     * @param d the data; null permitted
     * @return a new time series
     */
    @Nonnull
    @NewObject
    public Ts createTs(@Nullable String name, @Nullable MetaData md, @Nullable TsData d) {
        return createTs(name, new TsMoniker(), md, d);
    }

    /**
     * Gets or creates a time series from its moniker. [FIXME: TsData ignored if
     * Ts referenced by moniker already exists]
     *
     * @param name
     * @param moniker
     * @param md
     * @param d
     * @return an existing time series or a new one
     */
    @Nonnull
    public Ts createTs(@Nullable String name, @Nullable TsMoniker moniker, @Nullable MetaData md, @Nullable TsData d) {
        synchronized (m_ts) {
            WeakReference<Ts.Master> wref = null;
            if (moniker != null) {
                wref = m_ts.get(moniker);
            }
            if (wref != null) {
                Ts.Master ts = wref.get();
                if (ts != null) {
                    return ts.rename(name);
                } else {
                    ts = new Ts.Master(tsCallback, name, moniker, md, d);
                    m_ts.put(moniker, new WeakReference<>(ts));
                    return ts;
                }
            } else {
                if (moniker == null) {
                    moniker = new TsMoniker();
                }
                Ts.Master ts = new Ts.Master(tsCallback, name, moniker, md, d);
                m_ts.put(ts.getMoniker(), new WeakReference<>(ts));
                return ts;
            }
        }
    }

    /**
     * Gets or creates a time series and loads its data from a provider if
     * required by the information type.
     *
     * @param name
     * @param moniker [FIXME: null triggers an NPE if type != None]
     * @param type
     * @return a non-null time series
     */
    @Nonnull
    public Ts createTs(@Nullable String name, @Nonnull TsMoniker moniker, @Nonnull TsInformationType type) {
        synchronized (m_ts) {
            Ts.Master result = (Ts.Master) getTs(moniker);
            if (result == null) {
                if (type != TsInformationType.None) {
                    TsInformation info = new TsInformation(name, moniker, type);
                    fill(info);
                    result = new Ts.Master(tsCallback, name != null ? name : info.name, moniker);
                    result.update(info);
                } else {
                    result = new Ts.Master(tsCallback, name, moniker);
                }
                m_ts.put(moniker, new WeakReference<>(result));
            } else {
                result.load(type);
            }
            return result;
        }
    }

    private boolean fill(TsInformation info) {
        ITsProvider provider = getProvider(info.moniker.getSource());
        if (provider == null) {
            info.invalidDataCause = "Missing provider";
            return false;
        }
        if (!provider.get(info)) {
            if (info.invalidDataCause == null) {
                info.invalidDataCause = "Unknown error";
            }
            return false;
        }
        return true;
    }

    /**
     * Creates a new collection with the specified name.
     *
     * @return a new collection
     */
    @Nonnull
    @NewObject
    public TsCollection createTsCollection() {
        return createTsCollection("");
    }

    /**
     * Creates a new collection with the specified name.
     *
     * @param name the collection name; null replaced by empty string
     * @return a new collection
     */
    @Nonnull
    @NewObject
    public TsCollection createTsCollection(@Nullable String name) {
        synchronized (m_collections) {
            TsCollection coll = new TsCollection(tsCollectionCallback, name);
            m_collections.put(coll.getMoniker(),
                    new WeakReference<>(coll));
            return coll;
        }
    }

    /**
     * Gets or creates a collection from its moniker. [FIXME: Iterable<Ts>
     * ignored if TsCollection referenced by moniker already exists]
     *
     * @param name
     * @param moniker
     * @param md
     * @param ts
     * @return an existing collection or a new one
     */
    @Nonnull
    public TsCollection createTsCollection(@Nullable String name, @Nullable TsMoniker moniker, @Nullable MetaData md,
            @Nullable Iterable<Ts> ts) {
        synchronized (m_collections) {
            if (moniker == null) {
                TsCollection c = new TsCollection(tsCollectionCallback, name, new TsMoniker(), md, ts);
                m_collections.put(c.getMoniker(), new WeakReference<>(c));
                return c;
            } else {
                WeakReference<TsCollection> wref = m_collections.get(moniker);
                if (wref != null) {
                    TsCollection c = wref.get();
                    if (c != null) {
                        return c;
                    } else {
                        c = new TsCollection(tsCollectionCallback, name, moniker, md, ts);
                        m_collections.put(c.getMoniker(), new WeakReference<>(c));
                        return c;
                    }
                } else {
                    TsCollection c = new TsCollection(tsCollectionCallback, name, moniker, md, ts);
                    m_collections.put(c.getMoniker(), new WeakReference<>(c));
                    return c;
                }
            }
        }
    }

    /**
     * Gets or creates a collection and loads its data from a provider if
     * required by the information type.
     *
     * @param name
     * @param moniker [FIXME: null triggers an NPE]
     * @param type
     * @return a collection
     */
    @Nonnull
    public TsCollection createTsCollection(@Nullable String name, @Nonnull TsMoniker moniker,
            @Nonnull TsInformationType type) {
        // Search collection
        synchronized (m_collections) {
            TsCollection result = getTsCollection(moniker);
            if (result == null) {
                result = new TsCollection(tsCollectionCallback, name, moniker);
                if (type != TsInformationType.None) {
                    TsCollectionInformation info = new TsCollectionInformation(moniker, type);
                    fill(info);
                    // set data
                    List<Ts> updated = result.update(info);
                    for (Ts s : updated) {
                        notify(s, type, result);
                    }
                }
                // add collection
                m_collections.put(moniker, new WeakReference<>(result));
            } else {
                result.load(type);
            }
            return result;
        }
    }

    private boolean fill(TsCollectionInformation info) {
        ITsProvider provider = getProvider(info.moniker.getSource());
        if (provider == null) {
            info.invalidDataCause = "Missing provider";
            return false;
        }
        if (!provider.get(info)) {
            if (info.invalidDataCause == null) {
                info.invalidDataCause = "Unknown error";
            }
            return false;
        }
        return true;
    }

    /**
     *
     * @param obs
     */
    public void deleteObserver(Observer obs) {
        notifications.deleteObserver(obs);
    }

    /**
     *
     */
    public void dispose() {
        m_close = true;
        m_providers.values().forEach(ITsProvider::dispose);
        cleaner.interrupt();
        notifications.notificationThread.interrupt();
    }

    /**
     * Gets a provider by its name.
     *
     * @param source
     * @return a provider if found; null otherwise
     */
    @Nullable
    public ITsProvider getProvider(@Nullable String source) {
        if (source == null) {
            return null;
        }
        return m_providers.get(source);
    }

    /**
     * Gets the names of all registered providers.
     *
     * @return an array of provider names
     */
    @Nonnull
    public String[] getProviders() {
        return m_providers.keySet().stream().toArray(String[]::new);
    }

    /**
     * Gets a time series from its moniker.
     *
     * @param moniker [FIXME: returns null if null-moniker but is it intended?]
     * @return a time series if it already exists; null otherwise
     */
    @Nullable
    public Ts getTs(@Nullable TsMoniker moniker) {
        synchronized (m_ts) {
            WeakReference<Ts.Master> wref = m_ts.get(moniker);
            if (wref != null) {
                Ts ts = wref.get();
                if (ts == null) {
                    m_ts.remove(moniker);
                }
                // cleanTS();
                return ts;
            } else {
                return null;
            }
        }
    }

    /**
     * Gets a collection from its moniker.
     *
     * @param moniker [FIXME: works if null-moniker but is it intended? => i
     * have successfully created a collection with null-moniker]
     * @return a collection if it already exists; null otherwise
     */
    @Nullable
    public TsCollection getTsCollection(@Nullable TsMoniker moniker) {
        synchronized (m_collections) {
            WeakReference<TsCollection> wref = m_collections.get(moniker);
            if (wref != null) {
                TsCollection collection = wref.get();
                if (collection == null) {
                    m_collections.remove(moniker);
                }
                // cleanTSCollection();
                return collection;
            } else {
                return null;
            }
        }
    }

    /**
     *
     * @return
     */
    public boolean isClosing() {
        return m_close;
    }

    /**
     * Checks if a time series is currently loaded.
     *
     * @param moniker [FIXME: null-moniker works but is it intended?]
     * @return true if the time series is loaded; false otherwise
     */
    public boolean isTsAlive(@Nullable TsMoniker moniker) {
        if (m_close) {
            return false;
        }
        synchronized (m_ts) {
            WeakReference<Ts.Master> wref = m_ts.get(moniker);
            if (wref == null) {
                return false;
            }
            return wref.get() != null;
        }
    }

    /**
     * Checks if a collection is currently loaded.
     *
     * @param moniker [FIXME: null-moniker works but is it intended?]
     * @return true if the collection is loaded; false otherwise
     */
    public boolean isTsCollectionAlive(@Nullable TsMoniker moniker) {
        if (m_close) {
            return false;
        }
        synchronized (m_collections) {
            WeakReference<TsCollection> wref = m_collections.get(moniker);
            if (wref == null) {
                return false;
            }
            return wref.get() != null;
        }
    }

    /**
     *
     * @return
     */
    public boolean isUsingSynchronousNotifications() {
        return m_useSynchronousNotifications;
    }

    private boolean doLoad(@Nonnull Ts.Master ts, @Nonnull TsInformationType type) {
        if (ts.getMoniker().isAnonymous()) {
            return true;
        }
        TsInformation info = new TsInformation(ts.getName(), ts.getMoniker(), type);
        boolean result = fill(info);
        ts.update(info);
        notify(ts, info.type, this);
        return result;
    }

    private boolean doLoad(@Nonnull TsCollection c, @Nonnull TsInformationType type) {
        if (c.getMoniker().isAnonymous()) {
            return true;
        }
        TsCollectionInformation info = new TsCollectionInformation(c.getMoniker(), type);
        boolean result = fill(info);
        List<Ts> updated = c.update(info);
        notify(c, info.type, this);
        updated.forEach(s -> notify(s, info.type, c));
        return result;
    }

    /**
     * Loads information for the corresponding time series (it is not checked
     * that that information has already been loaded). An event is automatically
     * dispatched.
     *
     * @param ts
     * @param type the type of information to load
     * @return true if the data is loaded; false otherwise
     */
    public boolean load(@Nonnull Ts ts, @Nonnull TsInformationType type) {
        return doLoad(ts.getMaster(), type);
    }

    /**
     *
     * @param list
     * @param type
     * @return
     */
    public boolean load(Ts[] list, TsInformationType type) {
        boolean ok = false;
        for (Ts s : list) {
            if (load(s, type)) {
                ok = true;
            }
        }
        return ok;
    }

    /**
     * Loads information for the corresponding object (it is not checked that
     * that information has already been loaded). If a corresponding
     * asynchronous query is in the queue, it is removed and an event is
     * dispatch. Otherwise, the event is not automatically dispatch.
     *
     * @param c
     * @param type
     * @return true if the data is loaded; false otherwise
     */
    public boolean load(@Nonnull TsCollection c, @Nonnull TsInformationType type) {
        ITsProvider provider = getProvider(c.getMoniker().getSource());
        if (provider == null) {
            return load(c.toArray(), type);
        }
        return doLoad(c, type);
    }

    /**
     * Notifies listeners that the specified time series has been changed.
     * [FIXME: should be package protected?]
     *
     * @param s The changed time series
     * @param type Information that has been updated
     * @param sender The object that has initiated the event. Usually, this
     * object.
     */
    public void notify(Ts s, TsInformationType type, Object sender) {
        addEvent(new TsEvent(s, type, sender));
    }

    // / <remarks>For the information type, the following convention should be
    // used:
    // / when the metadata of the collection is changed, use
    // TSInformationType.metaData;
    // / when the structure of the collection is changed (time series without
    // data), use TSInformztionType.Definitiion;
    // / when all the data of the time series are loaded, use
    // TSInformationType.data,
    // /
    // / when the metadata of one time series is changed, dispatch with the
    // corresponding time series event</remarks>
    /**
     * Notifies listeners that the specified collection has been changed. A
     * temporary TSCollection (not identified by a specific provider) can be
     * changed by adding/removing time series. Such an event is NOT
     * automatically notified; users should call the Notify method to dispatch
     * that information (typically withe type equals to TSInformationType.All).
     * [FIXME: should be package protected?]
     *
     * @param s The changed collection
     * @param type Information that has been updated
     * @param sender The object that has initiated the event. Usually, this
     * object
     */
    public void notify(TsCollection s, TsInformationType type, Object sender) {
        addEvent(new TsEvent(s, type, sender));
    }

    /**
     * Query information related to a TS object. The TS object must have a valid
     * provider (no temporary time series) No event is dispatch.Be aware that
     * the function doesn't check if the TS object already contains the
     * requested information. When the information is available, the
     * corresponding event is automatically.
     *
     * @param s
     * @param type
     * @return true if the query is processed; false otherwise
     */
    public boolean query(@Nonnull Ts s, @Nonnull TsInformationType type) {
        if (type == TsInformationType.None) {
            return true;
        }
        ITsProvider provider = getProvider(s.getMoniker().getSource());
        if (provider == null) {
            return false;
        }
        synchronized (m_ts) {
            return provider.getAsyncMode() == TsAsyncMode.None
                    ? doLoad(s.getMaster(), type)
                    : provider.queryTs(s.getMoniker(), type);
        }
    }

    /**
     * Query information related to a TSCollection object. The TSCollection
     * object must have a valid provider (no temporary time series) No event is
     * dispatch. Be aware that the function doesn't check if the TSCollection
     * object already contains the requested information. When the information
     * is available, the corresponding event is automatically dispatched.
     *
     * @param c
     * @param type
     * @return true if the query is processed
     */
    public boolean query(@Nonnull TsCollection c, @Nonnull TsInformationType type) {
        if (type == TsInformationType.None) {
            return true;
        }
        ITsProvider provider = getProvider(c.getMoniker().getSource());
        if (provider == null) {
            Ts[] s = c.toArray();
            for (int i = 0; i < s.length; ++i) {
                query(s[i], type);
            }
            return true;
        }
        synchronized (m_collections) {
            return provider.getAsyncMode() == TsAsyncMode.None
                    ? doLoad(c, type)
                    : provider.queryTsCollection(c.getMoniker(), type);
        }
    }

    /**
     *
     * @param module
     * @param providerclass
     * @return
     */
    public ITsProvider register(String module, String providerclass) {
        try {
            ITsProvider iprovider = (ITsProvider) InterfaceLoader.create(
                    module, Class.forName("ec.tstoolkit.timeseries.ts.ITSProvider"),
                    providerclass);
            if (iprovider == null) {
                return null;
            }

            String name = iprovider.getSource();
            m_providers.put(name, iprovider);
            return iprovider;
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    /**
     *
     * @param name
     */
    public void remove(String name) {
        if (m_providers.containsKey(name)) {
            ITsProvider provider = m_providers.get(name);
            if (provider instanceof HasDataSourceList) {
                ((HasDataSourceList) provider).removeDataSourceListener(reloadListener);
            }
            provider.dispose();
            m_providers.remove(name);
        }
    }

    /**
     *
     * @param classname
     * @return
     */
    @Nullable
    public ITsProvider search(String classname) {
        for (ITsProvider provider : m_providers.values()) {
            if (provider.getClass().getName().equals(classname)) {
                return provider;
            }
        }
        return null;
    }

    private void start() {
        this.cleaner.start();
        this.notifications.start();
    }

    /**
     *
     * @param info
     */
    public void update(@Nonnull TsCollectionInformation info) {
        synchronized (m_collections) {
            TsCollection c = getTsCollection(info.moniker);
            if (c != null) {
                List<Ts> updated = c.update(info);
                notify(c, info.type, null);
                updated.forEach(s -> notify(s, info.type, c));
            } else {
                // the collection has been destroyed, but the series could be alive...
                info.items.forEach(sinfo -> update(sinfo));
            }
        }
    }

    /**
     * Should be called by tsproviders in the context of asynchronous queries.
     *
     * @param info
     */
    public void update(@Nonnull TsInformation info) {
        synchronized (m_ts) {
            Ts.Master s = (Ts.Master) getTs(info.moniker);
            if (s == null) // the series has been destroyed
            {
                return;
            }
            s.update(info);
            notify(s, info.type, null);
        }
    }

    /**
     *
     * @param value
     */
    public void useSynchronousNotifications(boolean value) {
        m_useSynchronousNotifications = value;
    }

    @Nonnull
    public static Collector<Ts, ?, TsCollection> toTsCollection() {
        return Collector.<Ts, List<Ts>, TsCollection>of(ArrayList::new, List::add, (l, r) -> {
            l.addAll(r);
            return l;
        }, o -> TsFactory.instance.createTsCollection(null, null, null, o));
    }

    private final class ReloadListener implements IDataSourceListener {

        @Override
        public void changed(DataSource dataSource) {
            ITsProvider p = getProvider(dataSource.getProviderName());
            if (p instanceof IDataSourceProvider) {
                IDataSourceProvider o = (IDataSourceProvider) p;
                reloadTSCollection(o, dataSource);
                reloadTS(o, dataSource);
            }
        }

        private void reloadTSCollection(IDataSourceProvider p, DataSource dataSource) {
            Stream.of(lookupTsCollection(p, dataSource)).forEach(getTsCollectionReloader(p));
        }

        private void reloadTS(IDataSourceProvider p, DataSource dataSource) {
            Stream.of(lookupTs(p, dataSource)).forEach(getTsReloader(p));
        }

        private TsCollection[] lookupTsCollection(IDataSourceProvider p, DataSource dataSource) {
            synchronized (m_collections) {
                return m_collections.entrySet().stream()
                        .filter(o -> isRelatedTo(p, dataSource, o.getKey()))
                        .map(o -> o.getValue().get())
                        .filter(Objects::nonNull)
                        .toArray(TsCollection[]::new);
            }
        }

        private Ts.Master[] lookupTs(IDataSourceProvider p, DataSource dataSource) {
            synchronized (m_collections) {
                return m_ts.entrySet().stream()
                        .filter(o -> isRelatedTo(p, dataSource, o.getKey()))
                        .map(o -> o.getValue().get())
                        .filter(Objects::nonNull)
                        .toArray(Ts.Master[]::new);
            }
        }

        private boolean isRelatedTo(IDataSourceProvider p, DataSource dataSource, TsMoniker moniker) {
            if (p.getSource().equals(moniker.getSource())) {
                DataSet dataSet = p.toDataSet(moniker);
                return dataSet != null && dataSet.getDataSource().equals(dataSource);
            }
            return false;
        }

        private Consumer<TsCollection> getTsCollectionReloader(IDataSourceProvider p) {
            return p.getAsyncMode() == TsAsyncMode.None
                    ? o -> doLoad(o, o.getInformationType())
                    : o -> p.queryTsCollection(o.getMoniker(), o.getInformationType());
        }

        private Consumer<Ts.Master> getTsReloader(IDataSourceProvider p) {
            return p.getAsyncMode() == TsAsyncMode.None
                    ? o -> doLoad(o, o.getInformationType())
                    : o -> p.queryTs(o.getMoniker(), o.getInformationType());
        }
    }

    private final class TsCallback implements Ts.FactoryCallback {

        @Override
        public boolean load(Ts ts, TsInformationType type) {
            return TsFactory.this.load(ts, type);
        }

        @Override
        public boolean query(Ts s, TsInformationType type) {
            return TsFactory.this.query(s, type);
        }

        @Override
        public void notify(Ts s, TsInformationType type, Object sender) {
            TsFactory.this.notify(s, type, sender);
        }

        @Override
        public Ts createTs(String name, MetaData md, TsData d) {
            return TsFactory.this.createTs(name, md, d);
        }

        @Override
        public Ts createTs(String name, TsMoniker moniker, TsInformationType type) {
            return TsFactory.this.createTs(name, moniker, type);
        }
    }

    private final class TsCollectionCallback implements TsCollection.FactoryCallback {

        @Override
        public boolean load(TsCollection c, TsInformationType type) {
            return TsFactory.this.load(c, type);
        }

        @Override
        public boolean query(TsCollection c, TsInformationType type) {
            return TsFactory.this.query(c, type);
        }

        @Override
        public void notify(TsCollection s, TsInformationType type, Object sender) {
            TsFactory.this.notify(s, type, sender);
        }

        @Override
        public Ts getTs(TsMoniker moniker) {
            return TsFactory.this.getTs(moniker);
        }

        @Override
        public Ts createTs(TsInformation info) {
            return TsFactory.this.createTs(info);
        }

        @Override
        public TsCollection createTsCollection(String name, TsMoniker moniker, MetaData md, Iterable<Ts> ts) {
            return TsFactory.this.createTsCollection(name, moniker, md, ts);
        }

        @Override
        public TsCollection createTsCollection(String name) {
            return TsFactory.this.createTsCollection(name);
        }
    }

    @Internal
    Ts.FactoryCallback getTsCallback() {
        return tsCallback;
    }

    @Internal
    TsCollection.FactoryCallback getTsCollectionCallback() {
        return tsCollectionCallback;
    }
}
