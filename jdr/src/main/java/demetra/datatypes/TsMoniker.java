/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.datatypes;

import ec.tstoolkit.design.Immutable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Immutable
public final class TsMoniker implements Comparable<TsMoniker> {

    public static final TsMoniker NULL = create(null, null);

    private final String m_source;
    private final String m_id;

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
    }

    private TsMoniker(String source) {
        m_source = source;
        m_id = null;
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
    }

    public boolean isAnonymous() {
        return m_id == null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsMoniker && equals((TsMoniker) obj));
    }

    private boolean equals(TsMoniker other) {
        return !isAnonymous() && (m_source.equals(other.m_source) && m_id.equals(other.m_id));
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
        return !isAnonymous()
                ? (0 ^ m_source.hashCode() ^ m_id.hashCode())
                : super.hashCode();
    }

    @Override
    public String toString() {
        return !isAnonymous()
                ? (m_source + "<@>" + m_id)
                : super.toString();
    }

    @Override
    public int compareTo(TsMoniker o) {
        if (this.isAnonymous()) {
            if (o.isAnonymous()) {
                return Integer.compare(this.hashCode(), o.hashCode());
            } else {
                return -1;
            }
        } else if (o.isAnonymous()) {
            return 1;
        } else {
            int r0 = this.m_source.compareTo(o.m_source);
            return r0 != 0 ? r0 : this.m_id.compareTo(o.m_id);
        }
    }
}
