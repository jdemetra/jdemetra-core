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
import ec.tstoolkit.IDocumented;
import ec.tstoolkit.MetaData;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.design.Internal;
import ec.tstoolkit.design.NewObject;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.TsPeriodSelector;
import ec.tstoolkit.timeseries.simplets.TsData;
import java.text.ParseException;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class Ts implements IDocumented, ITsIdentified {

    public static final String SOURCE_OLD = "tsmoniker.source", ID_OLD = "tsmoniker.id",
            DYNAMIC = "dynamic";
    // Additional metadata.
    public static final String BEG = "@beg", END = "@end", CONFIDENTIAL = "@confidential";

    public static enum DataFeature {

        Backcasts,
        Actual,
        Forecasts,
        Public,
        Confidential
    }
    private final String m_name;

    protected Ts(String name) {
        m_name = Strings.nullToEmpty(name);
    }

    public final String getRawName() {
        return m_name;
    }

    @Override
    public String getName() {
        return m_name;
    }

    public abstract Ts freeze();

    public abstract TsInformationType getInformationType();

    public abstract TsStatus hasMetaData();

    public abstract TsStatus hasData();

    public abstract TsData getTsData();

    public abstract boolean isFrozen();

    public abstract boolean set(MetaData md);

    public abstract boolean set(TsData data);

    public abstract boolean set(TsData data, MetaData md);

    public abstract Ts unfreeze();

    public abstract boolean load(TsInformationType type);

    public abstract boolean query(TsInformationType type);

    public abstract boolean reload(TsInformationType type);

    abstract Master getMaster();

    public abstract String getInvalidDataCause();

    public abstract void setInvalidDataCause(String message);

    /**
     * Converts this time series to a TsInformation.
     *
     * @param type
     * @return a non-null TsInformation
     */
    @Nonnull
    public abstract TsInformation toInfo(@Nonnull TsInformationType type);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName());
        TsData data = getTsData();
        if (data != null) {
            builder.append(System.lineSeparator());
            builder.append(data.toString());
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof Ts && equals((Ts) obj));
    }

    @Override
    public int hashCode() {
        return getMoniker().hashCode();
    }

    private boolean equals(Ts other) {
        return other == null ? false : getMoniker().equals(other.getMoniker());
    }

    public Ts rename(String newName) {
        return new Proxy(newName, getMaster());
    }

    public TsPeriodSelector getSelector(DataFeature feature) {
        TsPeriodSelector sel = new TsPeriodSelector();
        Day d0, d1;
        switch (feature) {
            case Backcasts:
                d0 = search(BEG);
                if (d0 != null) {
                    d0 = d0.minus(1);
                    sel.to(d0);
                } else {
                    sel.none();
                }
                break;
            case Actual:
                d0 = search(BEG);
                d1 = search(END);
                if (d0 != null && d1 != null) {
                    sel.between(d0, d1);
                } else if (d0 != null) {
                    sel.from(d0);
                } else if (d1 != null) {
                    sel.to(d1);
                }
                break;
            case Forecasts:
                d1 = search(END);
                if (d1 != null) {
                    d1 = d1.plus(1);
                    sel.from(d1);
                } else {
                    sel.none();
                }
                break;
            case Public:
                d1 = search(CONFIDENTIAL);
                if (d1 != null) {
                    d1 = d1.minus(1);
                    sel.to(d1);
                }
                break;
            case Confidential:
                d1 = search(CONFIDENTIAL);
                if (d1 != null) {
                    sel.from(d1);
                }
        }
        return sel;
    }

    private Day search(String info) {
        MetaData md = getMetaData();
        if (md == null) {
            return null;
        }
        String d = md.get(info);
        if (d == null) {
            return null;
        }
        try {
            return Day.fromString(d);
        } catch (ParseException ex) {
            return null;
        }
    }

    public boolean isFeature(DataFeature feature) {
        switch (feature) {
            case Backcasts:
                return search(BEG) != null;
            case Forecasts:
                return search(END) != null;
            case Confidential:
            case Public:
                return search(CONFIDENTIAL) != null;
            default: //Actual
                return true;
        }
    }

    @Internal
    interface FactoryCallback {

        boolean load(@Nonnull Ts ts, @Nonnull TsInformationType type);

        boolean query(@Nonnull Ts s, @Nonnull TsInformationType type);

        void notify(Ts s, TsInformationType type, Object sender);

        @Nonnull
        @NewObject
        Ts createTs(@Nullable String name, @Nullable MetaData md, @Nullable TsData d);

        @Nonnull
        Ts createTs(@Nullable String name, @Nonnull TsMoniker moniker, @Nonnull TsInformationType type);
    }

    @Internal
    static final class Master extends Ts {

        // safe
        private final FactoryCallback factory;
        private final TsMoniker m_moniker;

        // unsafe
        private MetaData m_metadata;
        private TsData m_data;
        private volatile TsInformationType m_info = TsInformationType.None;
        private String m_invalidDataMessage;

        Master(FactoryCallback factory, String name) {
            super(name);
            this.factory = factory;
            m_moniker = TsMoniker.createAnonymousMoniker();
            m_info = TsInformationType.UserDefined;
        }

        Master(FactoryCallback factory, String name, TsMoniker moniker) {
            super(name);
            this.factory = factory;
            m_moniker = moniker;
            if (moniker.getId() == null) {
                m_info = TsInformationType.UserDefined;
            }
        }

        Master(FactoryCallback factory, String name, TsMoniker moniker, MetaData md, TsData d) {
            super(name);
            this.factory = factory;
            m_moniker = moniker;
            m_metadata = md;
            m_data = d;
            if (md != null || d != null) {
                m_info = TsInformationType.UserDefined;
            }
        }

        @Override
        public TsMoniker getMoniker() {
            return m_moniker;
        }

        @Override
        public String getName() {
            if (isFrozen()) {
                return getRawName() + " [frozen]";
            } else {
                return getRawName();
            }
        }

        // create a frozen version of this series
        // source and identifier are set in the metadata, with the timestamp of this
        // operation
        /**
         *
         * @return
         */
        @Override
        public Ts freeze() {
            if (isFrozen()) {
                return this;
            }

            TsData copyOfData;
            MetaData copyOfMeta;
            synchronized (m_moniker) {
                load(TsInformationType.All);
                copyOfData = m_data != null ? m_data.clone() : null;
                copyOfMeta = m_metadata != null ? m_metadata.clone() : new MetaData();
            }

            putFreezeMeta(copyOfMeta, m_moniker);

            return factory.createTs(getRawName(), copyOfMeta, copyOfData);
        }

        /**
         *
         * @return
         */
        @Override
        public TsInformationType getInformationType() {
            synchronized (m_moniker) {
                return m_info;
            }
        }

        /**
         *
         * @return
         */
        @Override
        public MetaData getMetaData() {
            synchronized (m_moniker) {
                return m_metadata;
            }
        }

        /**
         *
         * @return
         */
        @Override
        public TsData getTsData() {
            synchronized (m_moniker) {
                return m_data;
            }
        }

        /**
         *
         * @return
         */
        @Override
        public TsStatus hasData() {
            synchronized (m_moniker) {
                if (m_info == TsInformationType.All
                        || m_info == TsInformationType.Data
                        || m_info == TsInformationType.UserDefined) {
                    return m_data == null ? TsStatus.Invalid : TsStatus.Valid;
                } else {
                    return TsStatus.Undefined;
                }
            }
        }

        /**
         *
         * @return
         */
        @Override
        public TsStatus hasMetaData() {
            synchronized (m_moniker) {
                if (m_info == TsInformationType.All
                        || m_info == TsInformationType.MetaData
                        || m_info == TsInformationType.UserDefined) {
                    return m_metadata == null ? TsStatus.Invalid : TsStatus.Valid;
                } else {
                    return TsStatus.Undefined;
                }
            }
        }

        // try to unfreeze a series that was previously frozem
        /**
         *
         * @return
         */
        @Override
        public boolean isFrozen() {
            if (m_moniker.getSource() != null) {
                return false;
            }
            synchronized (m_moniker) {
                return containsFreezeMeta(m_metadata);
            }
        }

        /**
         *
         * @param type
         * @return
         */
        @Override
        public boolean load(TsInformationType type) {
            // check if the information is available...
            if (m_info.encompass(type)) {
                return true;
            }
            return factory.load(this, type);
        }

        /**
         *
         * @param type
         * @return
         */
        @Override
        public boolean query(TsInformationType type) {
            // check if the information is available...
            if (m_info.encompass(type)) {
                return true;
            }
            return factory.query(this, type);
        }

        /**
         *
         * @param type
         * @return
         */
        @Override
        public boolean reload(TsInformationType type) {
            return factory.load(this, type);
        }

        /**
         *
         * @param md
         * @return
         */
        @Override
        public boolean set(MetaData md) {
            synchronized (m_moniker) {
                if (m_info != TsInformationType.UserDefined) {
                    return false;
                }
                m_metadata = md;
            }

            factory.notify(this, TsInformationType.MetaData, this);
            return true;
        }

        /**
         *
         * @param data
         * @return
         */
        @Override
        public boolean set(TsData data) {
            synchronized (m_moniker) {
                if (m_info != TsInformationType.UserDefined) {
                    return false;
                }
                m_data = data;
            }

            factory.notify(this, TsInformationType.Data, this);
            return true;
        }

        /**
         *
         * @param data
         * @param md
         * @return
         */
        @Override
        public boolean set(TsData data, MetaData md) {
            synchronized (m_moniker) {
                if (m_info != TsInformationType.UserDefined) {
                    return false;
                }
                m_data = data;
                m_metadata = md;
            }

            factory.notify(this, TsInformationType.All, this);
            return true;
        }

        // / <summary>
        // / Try to unfreeze a series that has been previously frozen
        // / </summary>
        // / <returns>A refreshed version of the series</returns>
        // / <remarks>If the series is not a frozen series, this object is
        // returned</remarks>
        /**
         *
         * @return
         */
        @Override
        public Ts unfreeze() {
            if (m_moniker.getSource() != null || m_moniker.getId() != null) {
                return this;
            }

            // Three cases:
            // 1. no meta: origin == null
            // 2. dynamic meta: origin != null && source == dynamic
            // 3. normal meta: origin != null && source != dynamic
            TsMoniker origin;
            TsData copyOfData = null;
            synchronized (m_moniker) {
                origin = getFreezeMeta(m_metadata);
                if (origin != null && TsMoniker.Type.DYNAMIC.equals(origin.getType())) {
                    copyOfData = m_data == null ? null : m_data.clone();
                }
            }

            if (origin == null) {
                return this;
            }
            if (TsMoniker.Type.DYNAMIC.equals(origin.getType())) {
                return factory.createTs(getRawName(), null, copyOfData);
            }
            return factory.createTs(getRawName(), origin, TsInformationType.None);
        }

        void update(TsInformation info) {
            synchronized (m_moniker) {
                if (info.hasData()) {
                    m_data = info.data;
                }
                if (info.hasMetaData()) {
                    m_metadata = info.metaData;
                }
                m_info = m_info.union(info.type);
                m_invalidDataMessage = info.invalidDataCause;
            }
        }

        @Override
        Master getMaster() {
            return this;
        }

        @Override
        public void setInvalidDataCause(String message) {
            synchronized (m_moniker) {
                this.m_invalidDataMessage = message;
            }
        }

        @Override
        public String getInvalidDataCause() {
            synchronized (m_moniker) {
                return m_invalidDataMessage;
            }
        }

        @Override
        public TsInformation toInfo(TsInformationType type) {
            TsInformation result = new TsInformation(getRawName(), m_moniker, type);
            load(type);

            synchronized (m_moniker) {
                if (result.hasData()) {
                    result.data = m_data;
                }
                if (result.hasMetaData()) {
                    result.metaData = m_metadata;
                }
                result.invalidDataCause = m_invalidDataMessage;
            }

            return result;
        }

        private static void putFreezeMeta(@Nonnull MetaData md, @Nonnull TsMoniker origin) {
            if (origin.getSource() != null) {
                md.put(MetaData.SOURCE, origin.getSource());
            }
            if (origin.getId() != null) {
                md.put(MetaData.ID, origin.getId());
            }
            md.put(MetaData.DATE, new Date().toString());
        }

        private static boolean containsFreezeMeta(@Nullable MetaData md) {
            if (md == null) {
                return false;
            }
            if (md.containsKey(MetaData.SOURCE)) {
                return true;
            }
            if (md.containsKey(SOURCE_OLD)) {
                return true;
            } else {
                return false;
            }
        }

        @Nullable
        private static TsMoniker getFreezeMeta(@Nullable MetaData md) {
            if (md == null) {
                return null;
            }

            String source = md.get(MetaData.SOURCE);
            if (source == null) {
                source = md.get(SOURCE_OLD);
            }

            if (DYNAMIC.equals(source)) {
                return TsMoniker.createDynamicMoniker();
            }

            String id = md.get(MetaData.ID);
            if (id == null) {
                id = md.get(ID_OLD);
            }

            if (source == null || id == null) {
                return null;
            } else {
                return TsMoniker.createProvidedMoniker(source, id);
            }
        }
    }

    @Internal
    static final class Proxy extends Ts {

        private final Master master;

        private Proxy(String name, Master master) {
            super(name);
            this.master = master;
        }

        @Override
        public Ts freeze() {
            if (master.isFrozen()) {
                return this;
            }
            Ts frozen = master.freeze();
            return new Proxy(getName() + " [frozen]", frozen.getMaster());
        }

        @Override
        public TsInformationType getInformationType() {
            return master.getInformationType();
        }

        @Override
        public TsStatus hasMetaData() {
            return master.hasMetaData();
        }

        @Override
        public boolean isFrozen() {
            return master.isFrozen();
        }

        @Override
        public boolean set(MetaData md) {
            return master.set(md);
        }

        @Override
        public boolean set(TsData data) {
            return master.set(data);
        }

        @Override
        public boolean set(TsData data, MetaData md) {
            return master.set(data, md);
        }

        @Override
        public Ts unfreeze() {
            return master.unfreeze();
        }

        @Override
        public MetaData getMetaData() {
            return master.getMetaData();
        }

        @Override
        public boolean load(TsInformationType type) {
            return master.load(type);
        }

        @Override
        Master getMaster() {
            return master;
        }

        @Override
        public TsStatus hasData() {
            return master.hasData();
        }

        @Override
        public TsData getTsData() {
            return master.getTsData();
        }

        @Override
        public boolean query(TsInformationType type) {
            return master.query(type);
        }

        @Override
        public boolean reload(TsInformationType type) {
            return master.reload(type);
        }

        @Override
        public TsMoniker getMoniker() {
            return master.getMoniker();
        }

        @Override
        public String getInvalidDataCause() {
            return master.getInvalidDataCause();
        }

        @Override
        public void setInvalidDataCause(String message) {
            master.setInvalidDataCause(message);
        }

        @Override
        public TsInformation toInfo(TsInformationType type) {
            TsInformation result = master.toInfo(type);
            result.name = getName();
            return result;
        }
    }
}
