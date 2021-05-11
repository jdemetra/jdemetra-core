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
package demetra.timeseries;

import java.util.Map;
import nbbrd.design.LombokWorkaround;
import internal.timeseries.util.TsDataBuilderUtil;
import internal.timeseries.LombokHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder(toBuilder = true)
public class Ts implements TsResource<TsData> {

    public static final String SOURCE_OLD = "tsmoniker.source", ID_OLD = "tsmoniker.id",
            DYNAMIC = "dynamic";
    // Additional metadata.
    public static final String BEG = "@beg", END = "@end", CONFIDENTIAL = "@confidential";

    @lombok.NonNull
    private TsMoniker moniker;

    @lombok.NonNull
    private TsInformationType type;

    @lombok.NonNull
    private String name;

    @lombok.NonNull
    @lombok.Singular("meta")
    private Map<String, String> meta;

    @lombok.NonNull
    private TsData data;

    @LombokWorkaround
    public static Builder builder() {
        return new Builder()
                .moniker(TsMoniker.NULL)
                .type(TsInformationType.UserDefined)
                .name("")
                .data(TsDataBuilderUtil.NO_DATA);
    }

    public static class Builder implements TsResource {

        @Override
        public TsMoniker getMoniker() {
            return moniker;
        }

        @Override
        public TsInformationType getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Map<String, String> getMeta() {
            return LombokHelper.getMap(meta$key, meta$value);
        }

        @Override
        public TsData getData() {
            return data;
        }
    }

    public Ts withName(String newName) {
        if (newName.equals(name)) {
            return this;
        }
        return new Ts(moniker, type, newName, meta, data);
    }

    public Ts withData(TsInformationType info) {
        if (type == info) {
            return this;
        }
        if (type == TsInformationType.UserDefined || info == TsInformationType.UserDefined) {
            throw new IllegalArgumentException();
        }
        if (type.encompass(info)) {
            return this;
        }
        return TsFactory.makeTs(moniker, info);
    }

    public Ts freeze() {
        if (! moniker.isProvided()) {
            return this;
        }
        Builder builder = this.toBuilder();
        putFreezeMeta(builder, moniker);
        
        return builder.moniker(TsMoniker.of())
                .type(TsInformationType.UserDefined)
                .build();
    }

    public Ts unfreeze() {
        if (moniker.isProvided())
            return this;
        TsMoniker pmoniker=getFreezeMeta(meta);
        if (pmoniker == null)
            return this;
        return TsFactory.makeTs(pmoniker, type);
    }

    private static void putFreezeMeta(@NonNull Builder builder, @NonNull TsMoniker origin) {
        builder.meta(TsFactory.SOURCE, origin.getSource());
        builder.meta(TsFactory.ID, origin.getId());
        builder.meta(TsFactory.DATE, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE));
    }

    private static boolean containsFreezeMeta(@NonNull Map<String, String> md) {
        if (md.containsKey(TsFactory.SOURCE)) {
            return true;
        }
        // legacy
        if (md.containsKey(SOURCE_OLD)) {
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    private static TsMoniker getFreezeMeta(@NonNull Map<String, String> md) {

        String source = md.get(TsFactory.SOURCE);
        // Legacy
        if (source == null) {
            source = md.get(SOURCE_OLD);
        }
        if (source == null) {
            return null;
        }

        if (source.length() == 0) {
            return TsMoniker.of();
        }
        // Legacy
        if (DYNAMIC.equals(source)) {
            return TsMoniker.of();
        }
        String id = md.get(TsFactory.ID);
        if (id == null) {
            id = md.get(ID_OLD);
        }
        if (id == null) {
            return null;
        } else {
            return TsMoniker.of(source, id);
        }
    }

}
