/*
 * Copyright 2020 National Bank of Belgium
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
package demetra.workspace;

import demetra.information.InformationSetSerializer;
import demetra.toolkit.io.xml.legacy.IXmlConverter;
import demetra.util.Id;
import demetra.workspace.file.FileFormat;
import demetra.workspace.file.spi.FamilyHandler;
import demetra.workspace.file.util.InformationSetSupport;
import demetra.workspace.file.util.XmlConverterSupport;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import nbbrd.design.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Defines the kind of data that a workspace might deal with.
 *
 * @author Philippe Charles
 * @since 2.2.0
 */
@Immutable
public final class WorkspaceFamily implements Id {

    public static final WorkspaceFamily UTIL_CAL = demetra.workspace.WorkspaceFamily.parse("Utilities@Calendars");
    public static final WorkspaceFamily UTIL_VAR = demetra.workspace.WorkspaceFamily.parse("Utilities@Variables");
    /**
     * Creates a family from a generic id.
     *
     * @param id a non-null id
     * @return a non-null family
     */
    @NonNull
    public static WorkspaceFamily of(@NonNull Id id) {
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
    @NonNull
    public static WorkspaceFamily parse(@NonNull String input) {
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

    public static FamilyHandler informationSet(WorkspaceFamily family, InformationSetSerializer factory, String repository) {
        return InformationSetSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }

    public static FamilyHandler xmlConverter(WorkspaceFamily family, Supplier<? extends IXmlConverter> factory, String repository) {
        return XmlConverterSupport.of(factory, repository).asHandler(family, FileFormat.GENERIC);
    }



}
