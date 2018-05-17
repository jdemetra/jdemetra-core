/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
package demetra.workspace;

import ec.tstoolkit.design.Immutable;
import demetra.workspace.util.Id;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * Defines the kind of data that a workspace might deal with.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
public final class WorkspaceFamily implements Id {

    /**
     * Creates a family from a generic id.
     *
     * @param id a non-null id
     * @return a non-null family
     */
    @Nonnull
    public static WorkspaceFamily of(@Nonnull Id id) {
        if (id instanceof WorkspaceFamily) {
            return (WorkspaceFamily) id;
        }
        if (id.getCount() == 0) {
            return EMPTY;
        }
        return new WorkspaceFamily(id.toArray());
    }

    /**
     * Parses a family from a string.
     *
     * @param input a non-null string
     * @return a non-null family
     */
    @Nonnull
    public static WorkspaceFamily parse(@Nonnull String input) {
        return !input.isEmpty() ? new WorkspaceFamily(input.split("@", -1)) : EMPTY;
    }

    private static final WorkspaceFamily EMPTY = new WorkspaceFamily(new String[0]);

    private final String[] data;

    private WorkspaceFamily(String[] data) {
        this.data = data;
    }

    @Override
    public String get(int index) {
        return data[index];
    }

    @Override
    public WorkspaceFamily extend(String tail) {
        String[] result = Arrays.copyOf(data, data.length + 1);
        result[data.length] = tail;
        return new WorkspaceFamily(result);
    }

    @Override
    public WorkspaceFamily parent() {
        switch (getCount()) {
            case 0:
                return null;
            case 1:
                return EMPTY;
            default:
                return new WorkspaceFamily(Arrays.copyOf(data, data.length - 1));
        }
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof WorkspaceFamily && equals((WorkspaceFamily) obj));
    }

    private boolean equals(WorkspaceFamily that) {
        return Arrays.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        return Arrays.stream(data).collect(Collectors.joining("@"));
    }

    @Override
    public String[] toArray() {
        return data.clone();
    }

    public static final WorkspaceFamily SA_MULTI = parse("Seasonal adjustment@multi-documents");
    public static final WorkspaceFamily SA_DOC_X13 = parse("Seasonal adjustment@documents@x13");
    public static final WorkspaceFamily SA_DOC_TRAMOSEATS = parse("Seasonal adjustment@documents@tramoseats");
    public static final WorkspaceFamily SA_SPEC_X13 = parse("Seasonal adjustment@specifications@x13");
    public static final WorkspaceFamily SA_SPEC_TRAMOSEATS = parse("Seasonal adjustment@specifications@tramoseats");

    public static final WorkspaceFamily MOD_DOC_REGARIMA = parse("Modelling@documents@regarima");
    public static final WorkspaceFamily MOD_DOC_TRAMO = parse("Modelling@documents@tramo");
    public static final WorkspaceFamily MOD_SPEC_REGARIMA = parse("Modelling@specifications@regarima");
    public static final WorkspaceFamily MOD_SPEC_TRAMO = parse("Modelling@specifications@tramo");

    public static final WorkspaceFamily UTIL_CAL = parse("Utilities@Calendars");
    public static final WorkspaceFamily UTIL_VAR = parse("Utilities@Variables");
}
