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

import ec.tstoolkit.design.Immutable;
import ec.tstoolkit.design.Internal;
import java.util.Objects;
import java.util.UUID;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Jean Palate
 */
@Immutable
public final class TsMoniker implements Comparable<TsMoniker> {

    /**
     * @since 2.2.0
     */
    public enum Type {
        ANONYMOUS, DYNAMIC, PROVIDED
    }

    @NonNull
    public static TsMoniker create(@Nullable String source, @Nullable String id) throws IllegalArgumentException {
        if (source == null && id == null) {
            return createAnonymousMoniker();
        } else if (source != null && id != null) {
            return createProvidedMoniker(source, id);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @return a non-null moniker
     * @since 2.2.2
     */
    @NonNull
    public static TsMoniker createAnonymousMoniker() {
        return new TsMoniker(null, null, UUID.randomUUID());
    }

    @NonNull
    public static TsMoniker createDynamicMoniker() {
        return new TsMoniker(Ts.DYNAMIC, null, UUID.randomUUID());
    }

    /**
     *
     * @param source
     * @param id
     * @return a non-null moniker
     * @since 2.2.2
     */
    @NonNull
    public static TsMoniker createProvidedMoniker(@NonNull String source, @NonNull String id) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(id, "id");
        return new TsMoniker(source, id, null);
    }

    /**
     * Internal factory to ease v3 migration. DO NOT USE.
     *
     * @since 2.2.2
     */
    @Internal
    static TsMoniker ofInternal(boolean dynamic, @NonNull UUID uuid) {
        Objects.requireNonNull(uuid, "uuid");
        return new TsMoniker(dynamic ? Ts.DYNAMIC : null, null, uuid);
    }

    @Nullable
    private final String m_source;

    @Nullable
    private final String m_id;

    @Nullable
    private final UUID uuid;

    private TsMoniker(String m_source, String m_id, UUID uuid) {
        this.m_source = m_source;
        this.m_id = m_id;
        this.uuid = uuid;
    }

    /**
     * @deprecated use {@link #createAnonymousMoniker()} instead
     */
    @Deprecated
    public TsMoniker() {
        this(null, null, UUID.randomUUID());
    }

    /**
     * @param source
     * @param id
     * @deprecated use
     * {@link #createProvidedMoniker(java.lang.String, java.lang.String)}
     * instead
     */
    public TsMoniker(@NonNull String source, @NonNull String id) throws IllegalArgumentException {
        if (source == null || id == null) {
            throw new IllegalArgumentException("source and id cannot be null");
        }
        m_source = source;
        m_id = id;
        uuid = null;
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

    /**
     * Internal property to ease v3 migration. DO NOT USE.
     *
     * @since 2.2.2
     */
    @Internal
    UUID getUuid() {
        return uuid;
    }

    @NonNull
    public Type getType() {
        if (m_source == null) {
            return Type.ANONYMOUS;
        }
        if (m_source.equals(Ts.DYNAMIC)) {
            return Type.DYNAMIC;
        }
        return Type.PROVIDED;
    }

    @Deprecated
    public boolean isAnonymous() {
        return m_id == null;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TsMoniker && equals((TsMoniker) obj));
    }

    private boolean equals(TsMoniker other) {
        return Objects.equals(m_source, other.m_source)
                && Objects.equals(m_id, other.m_id)
                && Objects.equals(uuid, other.uuid);
    }

    @Override
    public int hashCode() {
        switch (getType()) {
            case ANONYMOUS:
            case DYNAMIC:
                return uuid.hashCode();
            case PROVIDED:
                return 0 ^ m_source.hashCode() ^ m_id.hashCode();
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public String toString() {
        switch (getType()) {
            case ANONYMOUS:
            case DYNAMIC:
                return uuid.toString();
            case PROVIDED:
                return m_source + "<@>" + m_id;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public int compareTo(TsMoniker o) {
        if (this.uuid != null) {
            if (o.uuid != null) {
                return this.uuid.compareTo(o.uuid);
            } else {
                return -1;
            }
        } else if (o.uuid != null) {
            return 1;
        } else {
            int r0 = this.m_source.compareTo(o.m_source);
            return r0 != 0 ? r0 : this.m_id.compareTo(o.m_id);
        }
    }
}
