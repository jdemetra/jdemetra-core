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

import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.TsData;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.TsMoniker;
import demetra.tsprovider.TsResource;
import ec.tss.TsFactoryBypass;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tss.TsStatus;
import ec.tss.tsproviders.utils.OptionalTsData;
import ec.tstoolkit.MetaData;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TsConverter {

    //<editor-fold defaultstate="collapsed" desc="TsUnit / TsFrequency">
    public TsUnit toTsUnit(ec.tstoolkit.timeseries.simplets.TsFrequency o) {
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
                throw new RuntimeException();
        }
    }

    public ec.tstoolkit.timeseries.simplets.TsFrequency fromTsUnit(TsUnit o) throws ConverterException {
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
    public LocalDateTime toDateTime(ec.tstoolkit.timeseries.Day o) {
        return LocalDateTime.ofInstant(o.getTime().toInstant(), ZoneId.systemDefault());
    }

    public ec.tstoolkit.timeseries.Day fromDateTime(LocalDateTime o) {
        return new ec.tstoolkit.timeseries.Day(o.getYear(), ec.tstoolkit.timeseries.Month.valueOf(o.getMonthValue() - 1), o.getDayOfMonth() - 1);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsPeriod">
    public TsPeriod toTsPeriod(ec.tstoolkit.timeseries.simplets.TsPeriod o) {
        return TsPeriod.of(toTsUnit(o.getFrequency()), toDateTime(o.firstday()));
    }

    public ec.tstoolkit.timeseries.simplets.TsPeriod fromTsPeriod(TsPeriod o) throws ConverterException {
        return new ec.tstoolkit.timeseries.simplets.TsPeriod(fromTsUnit(o.getUnit()), fromDateTime(o.start()));
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsDomain">
    public TsDomain toTsDomain(ec.tstoolkit.timeseries.simplets.TsDomain o) {
        return TsDomain.of(toTsPeriod(o.getStart()), o.getLength());
    }

    public ec.tstoolkit.timeseries.simplets.TsDomain fromTsDomain(TsDomain o) throws ConverterException {
        return new ec.tstoolkit.timeseries.simplets.TsDomain(fromTsPeriod(o.getStartPeriod()), o.getLength());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsData / OptionalTsData">
    public TsData toTsData(OptionalTsData o) {
        if (o.isPresent()) {
            ec.tstoolkit.timeseries.simplets.TsData data = o.get();
            return TsData.ofInternal(toTsPeriod(data.getStart()), data.internalStorage());
        }
        return TsData.empty(o.getCause());
    }

    public OptionalTsData fromTsData(TsData o) throws ConverterException {
        if (!o.isEmpty()) {
            return OptionalTsData.present(new ec.tstoolkit.timeseries.simplets.TsData(fromTsPeriod(o.getStart()), o.getValues().toArray(), false));
        }
        return OptionalTsData.absent(o.getCause());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="DataSource">
    public DataSource toDataSource(ec.tss.tsproviders.DataSource o) {
        return DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }

    public ec.tss.tsproviders.DataSource fromDataSource(DataSource o) {
        return ec.tss.tsproviders.DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="DataSet">
    public DataSet toDataSet(ec.tss.tsproviders.DataSet o) {
        return DataSet.builder(toDataSource(o.getDataSource()), toKind(o.getKind())).putAll(o.getParams()).build();
    }

    public ec.tss.tsproviders.DataSet fromDataSet(DataSet o) {
        return ec.tss.tsproviders.DataSet.builder(fromDataSource(o.getDataSource()), fromKind(o.getKind())).putAll(o.getParams()).build();
    }

    public DataSet.Kind toKind(ec.tss.tsproviders.DataSet.Kind o) {
        switch (o) {
            case COLLECTION:
                return DataSet.Kind.COLLECTION;
            case DUMMY:
                return DataSet.Kind.DUMMY;
            case SERIES:
                return DataSet.Kind.SERIES;
            default:
                throw new RuntimeException();
        }
    }

    public ec.tss.tsproviders.DataSet.Kind fromKind(DataSet.Kind o) {
        switch (o) {
            case COLLECTION:
                return ec.tss.tsproviders.DataSet.Kind.COLLECTION;
            case DUMMY:
                return ec.tss.tsproviders.DataSet.Kind.DUMMY;
            case SERIES:
                return ec.tss.tsproviders.DataSet.Kind.SERIES;
            default:
                throw new RuntimeException();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsMoniker">
    private static final Map<TsMoniker, ec.tss.TsMoniker> MONIKERS = Collections.synchronizedMap(new WeakHashMap<>());

    public TsMoniker toTsMoniker(ec.tss.TsMoniker o) {
        String source = o.getSource();
        String id = o.getId();
        if (source != null && id != null) {
            return TsMoniker.of(source, id);
        }
        TsMoniker result = TsMoniker.of("TsFactory", String.valueOf(o.hashCode()));
        MONIKERS.put(result, o);
        return result;
    }

    public ec.tss.TsMoniker fromTsMoniker(TsMoniker o) {
        ec.tss.TsMoniker result = MONIKERS.get(o);
        return result != null ? result : ec.tss.TsMoniker.create(o.getSource(), o.getId());
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsInformationType">
    public TsInformationType toType(ec.tss.TsInformationType o) {
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
                throw new RuntimeException();
        }
    }

    public ec.tss.TsInformationType fromType(TsInformationType o) {
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
                throw new RuntimeException();
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Map / MetaData">
    public Map<String, String> toMeta(MetaData o) {
        return o != null ? Collections.unmodifiableMap(o) : Collections.emptyMap();
    }

    public MetaData fromMeta(Map<String, String> o) {
        return new MetaData(o);
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Ts + Builder/Info">
    public void fillTsInformation(TsResource<TsData> from, TsInformation to) {
        to.moniker = fromTsMoniker(from.getMoniker());
        to.type = fromType(from.getType());
        to.name = from.getName();
        to.metaData = fromMeta(from.getMeta());
        OptionalTsData data = fromTsData(from.getData());
        if (data.isPresent()) {
            to.data = data.get();
            to.invalidDataCause = null;
        } else {
            to.data = null;
            to.invalidDataCause = data.getCause();
        }
    }

    public TsInformation fromTsBuilder(TsResource<TsData> o) {
        TsInformation result = new TsInformation();
        fillTsInformation(o, result);
        return result;
    }

    public Ts.Builder toTsBuilder(TsInformation o) {
        return Ts.builder()
                .name(o.name)
                .moniker(toTsMoniker(o.moniker))
                .type(toType(o.type))
                .meta(toMeta(o.metaData))
                .data(toTsData(o.invalidDataCause != null ? OptionalTsData.absent(o.invalidDataCause) : OptionalTsData.present(o.data)));
    }

    public ec.tss.Ts fromTs(TsResource<TsData> o) {
        OptionalTsData data = fromTsData(o.getData());
        ec.tss.Ts result = TsFactoryBypass.series(o.getName(), fromTsMoniker(o.getMoniker()), fromMeta(o.getMeta()), data.orNull());
        if (!data.isPresent()) {
            result.setInvalidDataCause(data.getCause());
        }
        return result;
    }

    public Ts toTs(ec.tss.Ts o) {
        return Ts.builder()
                .name(o.getName())
                .moniker(toTsMoniker(o.getMoniker()))
                .type(toType(o.getInformationType()))
                .meta(toMeta(o.getMetaData()))
                .data(toTsData(o.hasData().equals(TsStatus.Valid) ? OptionalTsData.present(o.getTsData()) : OptionalTsData.absent(o.getInvalidDataCause() != null ? o.getInvalidDataCause() : "")))
                .build();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="TsCollection + Builder/Info">
    public void fillTsCollectionInformation(TsResource<List<Ts>> from, TsCollectionInformation to) {
        to.moniker = fromTsMoniker(from.getMoniker());
        to.type = fromType(from.getType());
        to.name = from.getName();
        to.metaData = fromMeta(from.getMeta());
        from.getData().forEach(x -> to.items.add(fromTsBuilder(x)));
    }

    public TsCollectionInformation fromTsCollectionBuilder(TsResource<List<Ts>> o) {
        TsCollectionInformation result = new TsCollectionInformation();
        fillTsCollectionInformation(o, result);
        return result;
    }

    public TsCollection.Builder toTsCollectionBuilder(TsCollectionInformation o) {
        return TsCollection.builder()
                .name(o.name)
                .moniker(toTsMoniker(o.moniker))
                .type(toType(o.type))
                .meta(toMeta(o.metaData))
                .data(o.items.stream().map(TsConverter::toTsBuilder).map(Ts.Builder::build).collect(Collectors.toList()));
    }

    public ec.tss.TsCollection fromTsCollection(TsResource<List<Ts>> o) {
        return TsFactoryBypass.col(o.getName(), fromTsMoniker(o.getMoniker()), fromMeta(o.getMeta()), o.getData().stream().map(TsConverter::fromTs).collect(Collectors.toList()));
    }

    public TsCollection toTsCollection(ec.tss.TsCollection o) {
        return TsCollection.builder()
                .name(o.getName())
                .moniker(toTsMoniker(o.getMoniker()))
                .type(toType(o.getInformationType()))
                .meta(toMeta(o.getMetaData()))
                .data(o.stream().map(TsConverter::toTs).collect(Collectors.toList()))
                .build();
    }
    //</editor-fold>
}
