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
package demetra.timeseries.calendars;

import nbbrd.design.Development;
import demetra.timeseries.ValidityPeriod;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
public class EasterRelatedDay implements Holiday {

    private static final Map<Integer, LocalDate> DIC = new HashMap<>();
    private static final Map<Integer, LocalDate> JDIC = new HashMap<>();

    private int offset;
    private double weight;
    private ValidityPeriod validityPeriod;
    private boolean julian;

    public static EasterRelatedDay gregorian(int offset, double weight, ValidityPeriod validityPeriod) {
        return new EasterRelatedDay(offset, weight, validityPeriod, false);
    }

    public static EasterRelatedDay gregorian(int offset) {
        return new EasterRelatedDay(offset, 1, ValidityPeriod.ALWAYS, false);
    }

    public static EasterRelatedDay julian(int offset, double weight, ValidityPeriod validityPeriod) {
        return new EasterRelatedDay(offset, weight, validityPeriod, true);
    }

    public static EasterRelatedDay julian(int offset) {
        return new EasterRelatedDay(offset, 1, ValidityPeriod.ALWAYS, true);
    }

    private EasterRelatedDay(int offset, double weight, ValidityPeriod validityPeriod, boolean julian) {
        this.weight = weight;
        this.offset = offset;
        this.validityPeriod = validityPeriod;
        this.julian = julian;
    }

    @Override
    public EasterRelatedDay reweight(double nweight) {
        if (nweight == weight) {
            return this;
        }
        return new EasterRelatedDay(offset, nweight, validityPeriod, julian);
    }

    @Override
    public EasterRelatedDay forPeriod(LocalDate start, LocalDate end) {
        if (validityPeriod.getStart().equals(start) && validityPeriod.getEnd().equals(end)) {
            return this;
        } else {
            return new EasterRelatedDay(offset, weight, ValidityPeriod.between(start, end), julian);
        }
    }

    public EasterRelatedDay plus(int ndays) {
        return new EasterRelatedDay(offset + ndays, weight, validityPeriod, julian);
    }

    public static final EasterRelatedDay SHROVEMONDAY = gregorian(-48),
            SHROVETUESDAY = gregorian(-47),
            ASHWEDNESDAY = gregorian(-46),
            EASTER = gregorian(0),
            EASTERMONDAY = gregorian(1),
            EASTERFRIDAY = gregorian(-2),
            EASTERTHURSDAY = gregorian(-3),
            ASCENSION = gregorian(39),
            PENTECOST = gregorian(49),
            WHITMONDAY = gregorian(50),
            CORPUSCHRISTI = gregorian(60),
            JULIAN_SHROVEMONDAY = julian(-48),
            JULIAN_SHROVETUESDAY = julian(-47),
            JULIAN_ASHWEDNESDAY = julian(-46),
            JULIAN_EASTER = julian(0),
            JULIAN_EASTERMONDAY = julian(1),
            JULIAN_EASTERFRIDAY = julian(-2),
            JULIAN_EASTERTHURSDAY = julian(-3),
            JULIAN_ASCENSION = julian(39),
            JULIAN_PENTECOST = julian(49),
            JULIAN_WHITMONDAY = julian(50),
            JULIAN_CORPUSCHRISTI = julian(60);

    public LocalDate calcDay(int year) {
        LocalDate d = easter(year);
        if (offset != 0) {
            d = d.plusDays(offset);
        }
        return d;
    }

    private LocalDate easter(int year) {
        return easter(year, julian);
    }

    public static LocalDate easter(int year, boolean jul) {
        if (jul) {
            synchronized (JDIC) {
                LocalDate e = JDIC.get(year);
                if (e == null) {
                    e = Easter.julianEaster(year, true);
                    JDIC.put(year, e);
                }
                return e;
            }
        } else {
            synchronized (DIC) {
                LocalDate e = DIC.get(year);
                if (e == null) {
                    e = Easter.easter(year);
                    DIC.put(year, e);
                }
                return e;
            }
        }
    }
    
    @Override
    public String display(){
        if (offset == 0)
            return "easter";
        else if (offset < 0)
            return "easter-"+(-offset);
        else
            return "easter+"+offset;
    }

}
