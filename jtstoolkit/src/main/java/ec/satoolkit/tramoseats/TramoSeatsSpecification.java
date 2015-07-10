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
package ec.satoolkit.tramoseats;

import ec.satoolkit.AbstractSaSpecification;
import ec.satoolkit.IDefaultSeriesDecomposer;
import ec.satoolkit.ISaSpecification;
import ec.satoolkit.algorithm.implementation.TramoSeatsProcessingFactory;
import ec.satoolkit.benchmarking.SaBenchmarkingSpec;
import ec.satoolkit.seats.SeatsSpecification;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.modelling.arima.IPreprocessor;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.TradingDaysSpec;
import ec.tstoolkit.modelling.arima.tramo.TramoSpecification;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsSpecification extends AbstractSaSpecification implements ISaSpecification, Cloneable {

    private static final String SMETHOD = "TS";
    // Dictionary
    public static final String TRAMO = "tramo", SEATS = "seats", BENCH = "benchmarking", RSA = "method";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        TramoSpecification.fillDictionary(InformationSet.item(prefix, TRAMO), dic);
        SeatsSpecification.fillDictionary(InformationSet.item(prefix, SEATS), dic);
        SaBenchmarkingSpec.fillDictionary(InformationSet.item(prefix, BENCH), dic);
    }

    public static final TramoSeatsSpecification RSA0, RSA1, RSA2, RSA3, RSA4, RSA5, RSAfull;

    static {
        SeatsSpecification sdef = new SeatsSpecification();
        RSA0 = new TramoSeatsSpecification(TramoSpecification.TR0, sdef);
        RSA1 = new TramoSeatsSpecification(TramoSpecification.TR1, sdef);
        RSA2 = new TramoSeatsSpecification(TramoSpecification.TR2, sdef);
        RSA3 = new TramoSeatsSpecification(TramoSpecification.TR3, sdef);
        RSA4 = new TramoSeatsSpecification(TramoSpecification.TR4, sdef);
        RSA5 = new TramoSeatsSpecification(TramoSpecification.TR5, sdef);
        RSAfull = new TramoSeatsSpecification(TramoSpecification.TRfull, sdef);
    }
    
    public static final TramoSeatsSpecification[] allSpecifications(){
        return new TramoSeatsSpecification[]{RSA0, RSA1, RSA2, RSA3, RSA4, RSA5, RSAfull};
    }

    public TramoSeatsSpecification() {
        tramoSpec_ = new TramoSpecification();
        seatsSpec_ = new SeatsSpecification();
        benchSpec_ = new SaBenchmarkingSpec();
    }

    public TramoSeatsSpecification(TramoSpecification tramoSpec, SeatsSpecification seatsSpec) {
        this.tramoSpec_ = tramoSpec;
        this.seatsSpec_ = seatsSpec;
        benchSpec_ = new SaBenchmarkingSpec();
    }

    @Override
    public TramoSeatsSpecification clone() {
        try {
            TramoSeatsSpecification spec = (TramoSeatsSpecification) super.clone();
            spec.tramoSpec_ = tramoSpec_.clone();
            spec.seatsSpec_ = seatsSpec_.clone();
            spec.benchSpec_ = benchSpec_.clone();
            return spec;
        } catch (CloneNotSupportedException err) {
            throw new AssertionError();
        }
    }
    private TramoSpecification tramoSpec_;
    private SeatsSpecification seatsSpec_;
    private SaBenchmarkingSpec benchSpec_;

    /**
     * @return the tramoSpec_
     */
    public TramoSpecification getTramoSpecification() {
        return tramoSpec_;
    }

    /**
     * @param tramoSpec the tramoSpec to set
     */
    public void setTramoSpecification(TramoSpecification tramoSpec) {
        if (tramoSpec == null) {
            throw new java.lang.IllegalArgumentException(TRAMO);
        }
        this.tramoSpec_ = tramoSpec;
    }

    /**
     * @return the seatsSpec_
     */
    public SeatsSpecification getSeatsSpecification() {
        return seatsSpec_;
    }

    /**
     * @param seatsSpec the seatsSpec to set
     */
    public void setSeatsSpecification(SeatsSpecification seatsSpec) {
        if (seatsSpec == null) {
            throw new java.lang.IllegalArgumentException(SEATS);
        }
        this.seatsSpec_ = seatsSpec;
    }

    public SaBenchmarkingSpec getBenchmarkingSpecification() {
        return benchSpec_;
    }

    public void setBenchmarkingSpecification(SaBenchmarkingSpec benchSpec) {
        if (benchSpec == null) {
            throw new java.lang.IllegalArgumentException(BENCH);
        }
        this.benchSpec_ = benchSpec;
    }

    public IPreprocessor buildPreprocessor(ProcessingContext context) {
        return tramoSpec_.build(context);
    }

    public IDefaultSeriesDecomposer buildDecomposer() {
        return new SeatsDecomposer(seatsSpec_);
    }

    @Override
    protected void checkContext(ProcessingContext context) {
        if (!tramoSpec_.getRegression().isUsed()) {
            return;
        }
        int nregs = tramoSpec_.getRegression().getUserDefinedVariablesCount();
        if (nregs > 0) {
            if (!checkVariables(tramoSpec_.getRegression().getUserDefinedVariables(), context)) {
                throw new TramoSeatsException(TramoSeatsException.ERR_CONTEXT);
            }
        }

        CalendarSpec cspec = tramoSpec_.getRegression().getCalendar();
        if (!cspec.isUsed()) {
            return;
        }

        TradingDaysSpec td = cspec.getTradingDays();
        if (!td.isUsed()) {
            return;
        }
        if (!checkVariables(td.getUserVariables(), context)) {
            throw new TramoSeatsException(TramoSeatsException.ERR_CONTEXT);
        }
        if (!checkCalendar(td.getHolidays(), context)) {
            throw new TramoSeatsException(TramoSeatsException.ERR_CONTEXT);
        }
    }

    public boolean isSystem() {
        return this == RSA0 || this == RSA1 || this == RSA2
                || this == RSA3 || this == RSA4 || this == RSA5 || this == RSAfull;
    }

    public TramoSeatsSpecification matchSystem() {
        if (isSystem()) {
            return this;
        }
        if (equals(RSA0)) {
            return RSA0;
        }
        if (equals(RSA1)) {
            return RSA1;
        } else if (equals(RSA2)) {
            return RSA2;
        } else if (equals(RSA3)) {
            return RSA3;
        } else if (equals(RSA4)) {
            return RSA4;
        } else if (equals(RSA5)) {
            return RSA5;
        } else if (equals(RSAfull)) {
            return RSAfull;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (this == RSA0) {
            return "RSA0";
        }
        if (this == RSA1) {
            return "RSA1";
        }
        if (this == RSA2) {
            return "RSA2";
        }
        if (this == RSA3) {
            return "RSA3";
        }
        if (this == RSA4) {
            return "RSA4";
        }
        if (this == RSA5) {
            return "RSA5";
        }
        if (this == RSAfull) {
            return "RSAfull";
        }
        if (equals(RSA0)) {
            return "RSA0";
        }
        if (equals(RSA1)) {
            return "RSA1";
        }
        if (equals(RSA2)) {
            return "RSA2";
        }
        if (equals(RSA3)) {
            return "RSA3";
        }
        if (equals(RSA4)) {
            return "RSA4";
        }
        if (equals(RSA5)) {
            return "RSA5";
        }
        if (equals(RSAfull)) {
            return "RSAfull";
        }
        return SMETHOD;
    }

    @Override
    public String toLongString() {
        String s = toString();
        if (SMETHOD.equals(s)) {
            return s;
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("TS[").append(s).append(']');
            return builder.toString();
        }
    }

    public static TramoSeatsSpecification fromString(String name) {
        if (name.equals("RSA0")) {
            return RSA0;
        }
        if (name.equals("RSA1")) {
            return RSA1;
        }
        if (name.equals("RSA2")) {
            return RSA2;
        }
        if (name.equals("RSA3")) {
            return RSA3;
        }
        if (name.equals("RSA4")) {
            return RSA4;
        }
        if (name.equals("RSA5")) {
            return RSA5;
        }
        if (name.equals("RSAfull")) {
            return RSAfull;
        }
        return new TramoSeatsSpecification();
    }

//    @Override
//    public List<String> summary() {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
    @Override
    public InformationSet write(boolean verbose) {
        InformationSet specInfo = new InformationSet();
        specInfo.add(ALGORITHM, TramoSeatsProcessingFactory.DESCRIPTOR);
        InformationSet tinfo = tramoSpec_.write(verbose);
        if (tinfo != null) {
            specInfo.add(TRAMO, tinfo);
        }
        InformationSet sinfo = seatsSpec_.write(verbose);
        if (sinfo != null) {
            specInfo.add(SEATS, sinfo);
        }
        InformationSet binfo = benchSpec_.write(verbose);
        if (binfo != null) {
            specInfo.add(BENCH, binfo);
        }
        return specInfo;
    }

    @Override
    public boolean read(InformationSet info) {
        InformationSet tinfo = info.getSubSet(TRAMO);
        if (tinfo != null) {
            boolean tok = tramoSpec_.read(tinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet sinfo = info.getSubSet(SEATS);
        if (sinfo != null) {
            boolean tok = seatsSpec_.read(sinfo);
            if (!tok) {
                return false;
            }
        }
        InformationSet binfo = info.getSubSet(BENCH);
        if (binfo != null) {
            boolean tok = benchSpec_.read(binfo);
            if (!tok) {
                return false;
            }
        }
        return true;
    }

    private boolean getrsa(InformationSet pinfo) {
        String rsa = pinfo.get(RSA, String.class);
        if (rsa != null) {
            TramoSpecification.Default option = TramoSpecification.Default.valueOf(rsa);
            switch (option) {
                case TR0:
                    tramoSpec_ = TramoSpecification.TR0.clone();
                    return true;
                case TR1:
                    tramoSpec_ = TramoSpecification.TR1.clone();
                    return true;
                case TR2:
                    tramoSpec_ = TramoSpecification.TR2.clone();
                    return true;
                case TR3:
                    tramoSpec_ = TramoSpecification.TR3.clone();
                    return true;
                case TR4:
                    tramoSpec_ = TramoSpecification.TR4.clone();
                    return true;
                case TR5:
                    tramoSpec_ = TramoSpecification.TR5.clone();
                    return true;
                case TRfull:
                    tramoSpec_ = TramoSpecification.TRfull.clone();
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof TramoSeatsSpecification && equals((TramoSeatsSpecification) obj));
    }

    private boolean equals(TramoSeatsSpecification other) {
        return Objects.equals(other.tramoSpec_, tramoSpec_)
                && Objects.equals(other.seatsSpec_, seatsSpec_)
                && Objects.equals(other.benchSpec_, benchSpec_);
    }
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.tramoSpec_);
        hash = 13 * hash + Objects.hashCode(this.seatsSpec_);
        return hash;
    }
}
