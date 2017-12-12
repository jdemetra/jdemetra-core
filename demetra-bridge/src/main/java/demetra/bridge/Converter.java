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

import demetra.timeseries.RegularDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.TsUnit;
import demetra.timeseries.simplets.TsData;
import demetra.tsprovider.DataSet;
import demetra.tsprovider.DataSource;
import demetra.tsprovider.OptionalTsData;
import demetra.tsprovider.Ts;
import demetra.tsprovider.TsCollection;
import demetra.tsprovider.TsInformationType;
import demetra.tsprovider.TsMoniker;
import ec.tss.TsCollectionInformation;
import ec.tss.TsInformation;
import ec.tstoolkit.MetaData;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Converter {

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
                throw ConverterException.of(ec.tstoolkit.timeseries.simplets.TsFrequency.class, TsUnit.class, o);
        }
    }

    public ec.tstoolkit.timeseries.simplets.TsFrequency fromTsUnit(TsUnit o) {
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

    public LocalDateTime toDateTime(ec.tstoolkit.timeseries.Day o) {
        return LocalDateTime.ofInstant(o.getTime().toInstant(), ZoneId.systemDefault());
    }

    public ec.tstoolkit.timeseries.Day fromDateTime(LocalDateTime o) {
        return new ec.tstoolkit.timeseries.Day(o.getYear(), ec.tstoolkit.timeseries.Month.valueOf(o.getMonthValue() - 1), o.getDayOfMonth() - 1);
    }

    public TsPeriod toTsPeriod(ec.tstoolkit.timeseries.simplets.TsPeriod o) {
        return TsPeriod.of(toTsUnit(o.getFrequency()), toDateTime(o.firstday()));
    }

    public ec.tstoolkit.timeseries.simplets.TsPeriod fromTsPeriod(TsPeriod o) {
        return new ec.tstoolkit.timeseries.simplets.TsPeriod(fromTsUnit(o.getUnit()), fromDateTime(o.start()));
    }

    public RegularDomain toRegularDomain(ec.tstoolkit.timeseries.simplets.TsDomain o) {
        return RegularDomain.of(toTsPeriod(o.getStart()), o.getLength());
    }

    public ec.tstoolkit.timeseries.simplets.TsDomain fromRegularDomain(RegularDomain o) {
        return new ec.tstoolkit.timeseries.simplets.TsDomain(fromTsPeriod(o.getStartPeriod()), o.getLength());
    }

    public TsData toTsData(ec.tstoolkit.timeseries.simplets.TsData o) {
        return TsData.ofInternal(toTsPeriod(o.getStart()), o.internalStorage());
    }

    public ec.tstoolkit.timeseries.simplets.TsData fromTsData(TsData o) {
        return new ec.tstoolkit.timeseries.simplets.TsData(fromTsPeriod(o.getStart()), o.values().toArray(), false);
    }

    public DataSource toDataSource(ec.tss.tsproviders.DataSource o) {
        return DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }

    public ec.tss.tsproviders.DataSource fromDataSource(DataSource o) {
        return ec.tss.tsproviders.DataSource.builder(o.getProviderName(), o.getVersion()).putAll(o.getParams()).build();
    }

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
                throw ConverterException.of(ec.tss.tsproviders.DataSet.Kind.class, DataSet.Kind.class, o);
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
                throw ConverterException.of(DataSet.Kind.class, ec.tss.tsproviders.DataSet.Kind.class, o);
        }
    }

    public TsMoniker toMoniker(ec.tss.TsMoniker o) {
        return new TsMoniker(o.getSource(), o.getId());
    }

    public ec.tss.TsMoniker fromMoniker(TsMoniker o) {
        return new ec.tss.TsMoniker(o.getSource(), o.getId());
    }

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
                throw ConverterException.of(ec.tss.TsInformationType.class, TsInformationType.class, o);
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
                throw ConverterException.of(TsInformationType.class, ec.tss.TsInformationType.class, o);
        }
    }

    public Map<String, String> toMeta(MetaData o) {
        return o;
    }

    public MetaData fromMeta(Map<String, String> o) {
        return new MetaData(o);
    }

    public Ts toTs(TsInformation o) {
        return Ts.builder()
                .name(o.name)
                .moniker(toMoniker(o.moniker))
                .type(toType(o.type))
                .metaData(toMeta(o.metaData))
                .data(o.invalidDataCause != null ? OptionalTsData.absent(o.invalidDataCause) : OptionalTsData.present(Converter.toTsData(o.data)))
                .build();
    }

    public TsInformation fromTs(Ts o) {
        TsInformation result = new TsInformation();
        result.name = o.getName();
        result.moniker = fromMoniker(o.getMoniker());
        result.type = fromType(o.getType());
        result.metaData = fromMeta(o.getMetaData());
        result.data = o.getData().isPresent() ? fromTsData(o.getData().get()) : null;
        result.invalidDataCause = !o.getData().isPresent() ? o.getData().getCause() : null;
        return result;
    }

    public TsCollection toTsCollection(TsCollectionInformation o) {
        return TsCollection.builder()
                .name(o.name)
                .moniker(toMoniker(o.moniker))
                .type(toType(o.type))
                .metaData(toMeta(o.metaData))
                .items(o.items.stream().map(item -> toTs(item)).collect(Collectors.toList()))
                .build();
    }
}
