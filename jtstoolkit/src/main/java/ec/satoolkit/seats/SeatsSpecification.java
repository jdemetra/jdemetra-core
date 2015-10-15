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
package ec.satoolkit.seats;

import ec.tstoolkit.algorithm.IProcSpecification;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.utilities.Jdk6;
import java.util.Map;
import java.util.Objects;

/**
 * @author Jean Palate & BAYENSK
 */
@Development(status = Development.Status.Preliminary)
public class SeatsSpecification implements IProcSpecification, Cloneable {

    public static final double DEF_EPSPHI = 2, DEF_RMOD = .5, DEF_SMOD = .5, DEF_XL = .95;

    public static final String ADMISS = "admiss",
            METHOD = "method",
            EPSPHI = "epsphi",
            RMOD = "rmod",
            SMOD = "smod",
            XL = "xl";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, ADMISS), Boolean.class);
        dic.put(InformationSet.item(prefix, METHOD), String.class);
        dic.put(InformationSet.item(prefix, EPSPHI), Double.class);
        dic.put(InformationSet.item(prefix, RMOD), Double.class);
        dic.put(InformationSet.item(prefix, SMOD), Double.class);
        dic.put(InformationSet.item(prefix, XL), Double.class);
    }

    public static enum ApproximationMode {

        None, Legacy, Noisy
    };

    public static enum EstimationMethod {

        Burman, KalmanSmoother, McElroyMatrix
    }

    private double xl_ = DEF_XL, rmod_ = DEF_RMOD, epsPhi_ = DEF_EPSPHI, smod = DEF_SMOD;
    private ApproximationMode changeModel_ = ApproximationMode.Legacy;
    private EstimationMethod method_ = EstimationMethod.Burman;
    private boolean log = false;
    private SarimaComponent arima;

    public double getXlBoundary() {
        return xl_;
    }

    public void setXlBoundary(double value) {
        if (value < 0.9 || value > 1) {
            throw new SeatsException("XL should belong to [0.9, 1]");
        }
        xl_ = value;
    }

    public double getSeasTolerance() {
        return epsPhi_;
    }

    public void setSeasTolerance(double value) {
        if (value < 0 || value > 10) {
            throw new SeatsException("EPSPHI (expressed in degrees) should belong to [0, 10]");
        }
        epsPhi_ = value;
    }

    public double getTrendBoundary() {
        return rmod_;
    }

    public void setTrendBoundary(double value) {
        if (value < 0 || value > 1) {
            throw new SeatsException("RMOD should belong to [0, 1]");
        }
        rmod_ = value;
    }

    public double getSeasBoundary() {
        return smod;
    }

    public void setSeasBoundary(double value) {
        if (value < 0 || value > 1) {
            throw new SeatsException("SMOD should belong to [0, 1]");
        }
        smod = value;
    }

    public boolean isDefault() {
        return epsPhi_ == DEF_EPSPHI && xl_ == DEF_XL && rmod_ == DEF_RMOD && smod == DEF_SMOD
                && changeModel_ == ApproximationMode.Legacy && method_ == EstimationMethod.Burman;
    }

    @Override
    public SeatsSpecification clone() {
        try {
            SeatsSpecification spec = (SeatsSpecification) super.clone();
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof SeatsSpecification && equals((SeatsSpecification) obj));
    }

    private boolean equals(SeatsSpecification spec) {
        return spec.getApproximationMode() == getApproximationMode()
                && spec.log == log
                && spec.method_ == method_
                && Objects.equals(spec.getArima(), getArima())
                && spec.epsPhi_ == epsPhi_
                && spec.rmod_ == rmod_
                && spec.smod == smod
                && spec.xl_ == xl_;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Jdk6.Double.hashCode(this.xl_);
        hash = 23 * hash + Jdk6.Double.hashCode(this.rmod_);
        hash = 23 * hash + Jdk6.Double.hashCode(this.epsPhi_);
        hash = 23 * hash + (this.isLog() ? 1 : 0);
        hash = 23 * hash + Objects.hashCode(this.getArima());
        return hash;
    }

    /**
     * @return the changeModel
     */
    public ApproximationMode getApproximationMode() {
        return changeModel_;
    }

    /**
     * @param changeModel the changeModel to set
     */
    public void setApproximationMode(ApproximationMode changeModel) {
        this.changeModel_ = changeModel;
    }

    /**
     * @return the wkEstimates
     */
    public EstimationMethod getMethod() {
        return method_;
    }

    /**
     * @param method
     */
    public void setMethod(EstimationMethod method) {
        this.method_ = method;
    }

    /**
     * @return the log
     */
    public boolean isLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    /**
     * @return the arima
     */
    public SarimaComponent getArima() {
        return arima;
    }

    /**
     * @param arima the arima to set
     */
    public void setArima(SarimaComponent arima) {
        this.arima = arima;
    }

    //////////////////////////////////////////////////////////////////////////
    @Override
    public InformationSet write(boolean verbose) {
        if (!verbose && isDefault()) {
            return null;
        }
        InformationSet info = new InformationSet();
        if (verbose || epsPhi_ != DEF_EPSPHI) {
            info.add(EPSPHI, epsPhi_);
        }
        if (verbose || rmod_ != DEF_RMOD) {
            info.add(RMOD, rmod_);
        }
        if (verbose || smod != DEF_SMOD) {
            info.add(SMOD, smod);
        }
        if (verbose || xl_ != DEF_XL) {
            info.add(XL, xl_);
        }
        if (verbose || changeModel_ != ApproximationMode.Legacy) {
            info.add(ADMISS, changeModel_.name());
        }
        if (verbose || method_ != EstimationMethod.Burman) {
            info.add(METHOD, method_.name());
        }
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            Double eps = info.get(EPSPHI, Double.class);
            if (eps != null) {
                epsPhi_ = eps;
            }
            Double rmod = info.get(RMOD, Double.class);
            if (rmod != null) {
                rmod_ = rmod;
            }
            Double smod = info.get(SMOD, Double.class);
            if (smod != null) {
                smod = smod;
            }
            Double xl = info.get(XL, Double.class);
            if (xl != null) {
                xl_ = xl;
            }
            String admiss = info.get(ADMISS, String.class);
            if (admiss != null) {
                changeModel_ = ApproximationMode.valueOf(admiss);
            }
            String method = info.get(METHOD, String.class);
            if (method != null) {
                method_ = EstimationMethod.valueOf(method);
            }

            return true;
        } catch (Exception err) {
            return false;
        }
    }

}
