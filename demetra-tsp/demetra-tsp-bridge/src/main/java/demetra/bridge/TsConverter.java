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
package demetra.bridge;

import demetra.timeseries.*;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.util.ObsFormat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsConverter {

    //<editor-fold defaultstate="collapsed" desc="TsUnit / TsFrequency">
    public @NonNull TsUnit toTsUnit(ec.tstoolkit.timeseries.simplets.@NonNull TsFrequency o) {
        switch (o) {
            case BiMonthly:
                return TsUnit.of(2, ChronoUnit.MONTHS);
            case HalfYearly:
                return TsUnit.HALF_YEAR;
            case Monthly:
                return TsUnit.MONTH;
            case QuadriMonthly:
                return TsUnit.of(4, ChronoUnit.MONTHS);
            case Quarterly:
                return TsUnit.QUARTER;
            case Undefined:
                return TsUnit.UNDEFINED;
            case Yearly:
                return TsUnit.YEAR;
            default:
                throw ConverterException.notPossible(o);
        }
    }

    public ec.tstoolkit.timeseries.simplets.@NonNull TsFrequency fromTsUnit(@NonNull TsUnit o) throws ConverterException {
        if (o.equals(TsUnit.of(2, ChronoUnit.MONTHS))) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.BiMonthly;
        }
        if (o.equals(TsUnit.HALF_YEAR)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.HalfYearly;
        }
        if (o.equals(TsUnit.MONTH)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
        }
        if (o.equals(TsUnit.of(4, ChronoUnit.MONTHS))) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.QuadriMonthly;
        }
        if (o.equals(TsUnit.QUARTER)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly;
        }
        if (o.equals(TsUnit.UNDEFINED)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;
        }
        if (o.equals(TsUnit.YEAR)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Yearly;
        }
        throw ConverterException.of(TsUnit.class, ec.tstoolkit.timeseries.simplets.TsFrequency.class, o);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="LocalDateTime / Day">
    public @NonNull LocalDateTime toDateTime(ec.tstoolkit.timeseries.@NonNull Day o) {
        return LocalDateTime.ofInstant(o.getTime().toInstant(), ZoneId.systemDefault());
    }

    public ec.tstoolkit.timeseries.@NonNull Day fromDateTime(@NonNull LocalDateTime o) {
        return new ec.tstoolkit.timeseries.Day(o.getYear(), ec.tstoolkit.timeseries.Month.valueOf(o.getMonthValue() - 1), o.getDayOfMonth() - 1);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsPeriod">
    public @NonNull TsPeriod toTsPeriod(ec.tstoolkit.timeseries.simplets.@NonNull TsPeriod o) {
        return TsPeriod.of(toTsUnit(o.getFrequency()), toDateTime(o.firstday()));
    }

    public ec.tstoolkit.timeseries.simplets.@NonNull TsPeriod fromTsPeriod(@NonNull TsPeriod o) throws ConverterException {
        return new ec.tstoolkit.timeseries.simplets.TsPeriod(fromTsUnit(o.getUnit()), fromDateTime(o.start()));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsDomain">
    public @NonNull TsDomain toTsDomain(ec.tstoolkit.timeseries.simplets.@NonNull TsDomain o) {
        return TsDomain.of(toTsPeriod(o.getStart()), o.getLength());
    }

    public ec.tstoolkit.timeseries.simplets.@NonNull TsDomain fromTsDomain(@NonNull TsDomain o) throws ConverterException {
        return new ec.tstoolkit.timeseries.simplets.TsDomain(fromTsPeriod(o.getStartPeriod()), o.getLength());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsData / OptionalTsData">
    public @NonNull TsData toTsData(ec.tss.tsproviders.utils.@NonNull OptionalTsData o) {
        if (o.isPresent()) {
            ec.tstoolkit.timeseries.simplets.TsData data = o.get();
            return TsData.ofInternal(toTsPeriod(data.getStart()), data.internalStorage());
        }
        return TsData.empty(o.getCause());
    }

    public ec.tss.tsproviders.utils.@NonNull OptionalTsData fromTsData(@NonNull TsData o) throws ConverterException {
        return !o.isEmpty()
                ? ec.tss.tsproviders.utils.OptionalTsData.present(new ec.tstoolkit.timeseries.simplets.TsData(fromTsPeriod(o.getStart()), o.getValues().toArray(), false))
                : ec.tss.tsproviders.utils.OptionalTsData.absent(o.getCause());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="DataSource">
    public @NonNull DataSource toDataSource(ec.tss.tsproviders.@NonNull DataSource o) {
        return DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }

    public ec.tss.tsproviders.@NonNull DataSource fromDataSource(@NonNull DataSource o) {
        return ec.tss.tsproviders.DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="DataSet">
    public @NonNull DataSet toDataSet(ec.tss.tsproviders.@NonNull DataSet o) {
        return DataSet.builder(toDataSource(o.getDataSource()), toKind(o.getKind())).putAll(o.getParams()).build();
    }

    public ec.tss.tsproviders.@NonNull DataSet fromDataSet(@NonNull DataSet o) {
        return ec.tss.tsproviders.DataSet.builder(fromDataSource(o.getDataSource()), fromKind(o.getKind())).putAll(o.getParams()).build();
    }

    public DataSet.@NonNull Kind toKind(ec.tss.tsproviders.DataSet.@NonNull Kind o) {
        switch (o) {
            case COLLECTION:
                return DataSet.Kind.COLLECTION;
            case DUMMY:
                return DataSet.Kind.DUMMY;
            case SERIES:
                return DataSet.Kind.SERIES;
            default:
                throw ConverterException.notPossible(o);
        }
    }

    public ec.tss.tsproviders.DataSet.@NonNull Kind fromKind(DataSet.@NonNull Kind o) {
        switch (o) {
            case COLLECTION:
                return ec.tss.tsproviders.DataSet.Kind.COLLECTION;
            case DUMMY:
                return ec.tss.tsproviders.DataSet.Kind.DUMMY;
            case SERIES:
                return ec.tss.tsproviders.DataSet.Kind.SERIES;
            default:
                throw ConverterException.notPossible(o);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsMoniker">
    private static final String ANONYMOUS_PREFIX = "anonymous:";

    @SuppressWarnings("null")
    public @NonNull TsMoniker toTsMoniker(ec.tss.@NonNull TsMoniker o) {
        switch (o.getType()) {
            case ANONYMOUS:
                return TsMoniker.of("", ANONYMOUS_PREFIX + ec.tss.TsBypass.uuid(o));
            case DYNAMIC:
                return TsMoniker.of("", ec.tss.TsBypass.uuid(o).toString());
            case PROVIDED:
                String source = o.getSource();
                if (source == null) {
                    throw new ConverterException("Unexpected null source");
                }
                String id = o.getId();
                if (id == null) {
                    throw new ConverterException("Unexpected null id");
                }
                return TsMoniker.of(source, id);
            default:
                throw ConverterException.notPossible(o.getType());
        }
    }

    public ec.tss.@NonNull TsMoniker fromTsMoniker(@NonNull TsMoniker o) {
        if (o.getSource().isEmpty() && !o.getId().isEmpty()) {
            return o.getId().startsWith(ANONYMOUS_PREFIX)
                    ? ec.tss.TsBypass.moniker(false, UUID.fromString(o.getId().substring(ANONYMOUS_PREFIX.length())))
                    : ec.tss.TsBypass.moniker(true, UUID.fromString(o.getId()));
        }
        return ec.tss.TsMoniker.createProvidedMoniker(o.getSource(), o.getId());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsInformationType">
    public @NonNull TsInformationType toType(ec.tss.@NonNull TsInformationType o) {
        switch (o) {
            case All:
                return TsInformationType.All;
            case BaseInformation:
                return TsInformationType.BaseInformation;
            case Data:
                return TsInformationType.Data;
            case Definition:
                return TsInformationType.Definition;
            case MetaData:
                return TsInformationType.MetaData;
            case None:
                return TsInformationType.None;
            case UserDefined:
                return TsInformationType.UserDefined;
            default:
                throw ConverterException.notPossible(o);
        }
    }

    public ec.tss.@NonNull TsInformationType fromType(@NonNull TsInformationType o) {
        switch (o) {
            case All:
                return ec.tss.TsInformationType.All;
            case BaseInformation:
                return ec.tss.TsInformationType.BaseInformation;
            case Data:
                return ec.tss.TsInformationType.Data;
            case Definition:
                return ec.tss.TsInformationType.Definition;
            case MetaData:
                return ec.tss.TsInformationType.MetaData;
            case None:
                return ec.tss.TsInformationType.None;
            case UserDefined:
                return ec.tss.TsInformationType.UserDefined;
            default:
                throw ConverterException.notPossible(o);
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Map / MetaData">
    public @NonNull Map<String, String> toMeta(ec.tstoolkit.@Nullable MetaData o) {
        return o != null ? Collections.unmodifiableMap(o) : Collections.emptyMap();
    }

    public ec.tstoolkit.@NonNull MetaData fromMeta(@NonNull Map<String, String> o) {
        return new ec.tstoolkit.MetaData(o);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Ts + Builder/Info">
    public void fillTsInformation(@NonNull TsResource<TsData> from, ec.tss.@NonNull TsInformation to) {
        to.moniker = fromTsMoniker(from.getMoniker());
        to.type = fromType(from.getType());
        to.name = from.getName();
        to.metaData = fromMeta(from.getMeta());
        ec.tss.tsproviders.utils.OptionalTsData data = fromTsData(from.getData());
        if (data.isPresent()) {
            to.data = data.get();
            to.invalidDataCause = null;
        } else {
            to.data = null;
            to.invalidDataCause = data.getCause();
        }
    }

    public ec.tss.@NonNull TsInformation fromTsBuilder(@NonNull TsResource<TsData> o) {
        ec.tss.TsInformation result = new ec.tss.TsInformation();
        fillTsInformation(o, result);
        return result;
    }

    public Ts.@NonNull Builder toTsBuilder(ec.tss.@NonNull TsInformation o) {
        return Ts.builder()
                .name(o.name)
                .moniker(toTsMoniker(o.moniker))
                .type(toType(o.type))
                .meta(toMeta(o.metaData))
                .data(toTsData(o.invalidDataCause != null ? ec.tss.tsproviders.utils.OptionalTsData.absent(o.invalidDataCause) : ec.tss.tsproviders.utils.OptionalTsData.present(o.data)));
    }

    public ec.tss.@NonNull Ts fromTs(@NonNull TsResource<TsData> o) {
        ec.tss.tsproviders.utils.OptionalTsData data = fromTsData(o.getData());
        ec.tss.Ts result = ec.tss.TsBypass.series(o.getName(), fromTsMoniker(o.getMoniker()), fromMeta(o.getMeta()), data.orNull());
        if (!data.isPresent()) {
            result.setInvalidDataCause(data.getCause());
        }
        return result;
    }

    public @NonNull Ts toTs(ec.tss.@NonNull Ts o) {
        return Ts.builder()
                .name(o.getName())
                .moniker(toTsMoniker(o.getMoniker()))
                .type(toType(o.getInformationType()))
                .meta(toMeta(o.getMetaData()))
                .data(toTsData(o.hasData().equals(ec.tss.TsStatus.Valid) ? ec.tss.tsproviders.utils.OptionalTsData.present(o.getTsData()) : ec.tss.tsproviders.utils.OptionalTsData.absent(o.getInvalidDataCause() != null ? o.getInvalidDataCause() : "")))
                .build();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsCollection + Builder/Info">
    public void fillTsCollectionInformation(@NonNull TsResource<List<Ts>> from, ec.tss.@NonNull TsCollectionInformation to) {
        to.moniker = fromTsMoniker(from.getMoniker());
        to.type = fromType(from.getType());
        to.name = from.getName();
        to.metaData = fromMeta(from.getMeta());
        from.getData().forEach(x -> to.items.add(fromTsBuilder(x)));
    }

    public ec.tss.@NonNull TsCollectionInformation fromTsCollectionBuilder(@NonNull TsResource<List<Ts>> o) {
        ec.tss.TsCollectionInformation result = new ec.tss.TsCollectionInformation();
        fillTsCollectionInformation(o, result);
        return result;
    }

    public TsCollection.@NonNull Builder toTsCollectionBuilder(ec.tss.@NonNull TsCollectionInformation o) {
        return TsCollection.builder()
                .name(o.name)
                .moniker(toTsMoniker(o.moniker))
                .type(toType(o.type))
                .meta(toMeta(o.metaData))
                .data(o.items.stream().map(TsConverter::toTsBuilder).map(Ts.Builder::build).collect(Collectors.toList()));
    }

    public ec.tss.@NonNull TsCollection fromTsCollection(@NonNull TsResource<List<Ts>> o) {
        return ec.tss.TsBypass.col(o.getName(), fromTsMoniker(o.getMoniker()), fromMeta(o.getMeta()), o.getData().stream().map(TsConverter::fromTs).collect(Collectors.toList()));
    }

    public @NonNull TsCollection toTsCollection(ec.tss.@NonNull TsCollection o) {
        return TsCollection.builder()
                .name(o.getName())
                .moniker(toTsMoniker(o.getMoniker()))
                .type(toType(o.getInformationType()))
                .meta(toMeta(o.getMetaData()))
                .data(o.stream().map(TsConverter::toTs).collect(Collectors.toList()))
                .build();
    }
    //</editor-fold>

    public ec.tss.tsproviders.utils.@NonNull DataFormat fromObsFormat(@NonNull ObsFormat o) {
        return ec.tss.tsproviders.utils.DataFormat.of(o.getLocale(), o.getDateTimePattern(), o.getNumberPattern());
    }

    public @NonNull ObsFormat toObsFormat(ec.tss.tsproviders.utils.@NonNull DataFormat o) {
        return ObsFormat.of(o.getLocale(), o.getDatePattern(), o.getNumberPattern());
    }
}
