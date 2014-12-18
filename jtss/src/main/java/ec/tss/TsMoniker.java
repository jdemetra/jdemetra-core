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

import com.google.common.primitives.Ints;
import ec.tstoolkit.design.Immutable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class TsMoniker implements Comparable<TsMoniker> {

    private static final AtomicInteger g_id = new AtomicInteger(0);
    private final String m_source;
    private final String m_id;
    private final int hash;

    @Nonnull
    public static TsMoniker create(@Nullable String source, @Nullable String id) throws IllegalArgumentException {
        if (source == null && id == null) {
            return new TsMoniker();
        } else if (source != null && id != null) {
            return new TsMoniker(source, id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Creates an anonymous moniker.
     */
    public TsMoniker() {
        m_source = null;
        m_id = null;
        hash = g_id.getAndIncrement();
    }

    public static TsMoniker createDynamicMoniker() {
        return new TsMoniker(Ts.DYNAMIC);
    }

    private TsMoniker(String source) {
        m_source = source;
        m_id = null;
        hash = g_id.getAndIncrement();
    }

    /**
     * Creates a regular moniker.
     *
     * @param source
     * @param id
     */
    public TsMoniker(@Nonnull String source, @Nonnull String id) throws IllegalArgumentException {
        if (source == null || id == null) {
            throw new IllegalArgumentException("source and id cannot be null");
        }
        m_source = source;
        m_id = id;
        hash = 0 ^ m_source.hashCode() ^ m_id.hashCode();
    }

    public boolean isAnonymous() {
        return m_id == null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsMoniker && equals((TsMoniker) obj));
    }

    private boolean equals(TsMoniker other) {
        return !isAnonymous() ? (m_source.equals(other.m_source) && m_id.equals(other.m_id)) : (hash == other.hash);
    }

    /**
     *
     * @return
     */
    @Nullable
    public String getId() {
        return m_id;
    }

    /**
     *
     * @return
     */
    @Nullable
    public String getSource() {
        return m_source;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return !isAnonymous() ? (m_source + "<@>" + m_id) : String.valueOf(hash);
    }

    @Override
    public int compareTo(TsMoniker o) {
        if (this.isAnonymous()) {
            if (o.isAnonymous()) {
                return Ints.compare(this.hash, o.hash);
            } else {
                return -1;
            }
        } else {
            if (o.isAnonymous()) {
                return 1;
            } else {
                int r0 = this.m_source.compareTo(o.m_source);
                return r0 != 0 ? r0 : this.m_id.compareTo(o.m_id);
            }
        }
    }
}
