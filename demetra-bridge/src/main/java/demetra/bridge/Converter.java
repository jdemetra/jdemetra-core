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
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class Converter {

    public TsUnit toTsUnit(ec.tstoolkit.timeseries.simplets.TsFrequency o) {
        switch (o) {
            case BiMonthly:
                return TsUnit.BI_MONTHLY;
            case HalfYearly:
                return TsUnit.HALF_YEARLY;
            case Monthly:
                return TsUnit.MONTHLY;
            case QuadriMonthly:
                return TsUnit.QUADRI_MONTHLY;
            case Quarterly:
                return TsUnit.QUARTERLY;
            case Undefined:
                return TsUnit.UNDEFINED;
            case Yearly:
                return TsUnit.YEARLY;
            default:
                throw ConverterException.of(ec.tstoolkit.timeseries.simplets.TsFrequency.class, TsUnit.class, o);
        }
    }

    public ec.tstoolkit.timeseries.simplets.TsFrequency fromTsUnit(TsUnit o) {
        if (o.equals(TsUnit.BI_MONTHLY)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.BiMonthly;
        }
        if (o.equals(TsUnit.HALF_YEARLY)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.HalfYearly;
        }
        if (o.equals(TsUnit.MONTHLY)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Monthly;
        }
        if (o.equals(TsUnit.QUADRI_MONTHLY)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.QuadriMonthly;
        }
        if (o.equals(TsUnit.QUARTERLY)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Quarterly;
        }
        if (o.equals(TsUnit.UNDEFINED)) {
            return ec.tstoolkit.timeseries.simplets.TsFrequency.Undefined;
        }
        if (o.equals(TsUnit.YEARLY)) {
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
}
