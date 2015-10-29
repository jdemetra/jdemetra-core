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

package ec.tss.formatters.sa;

import ec.satoolkit.seats.SeatsSpecification;
import ec.satoolkit.seats.SeatsSpecification.ApproximationMode;
import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.modelling.arima.tramo.ArimaSpec;
import ec.tstoolkit.modelling.arima.tramo.AutoModelSpec;
import ec.tstoolkit.modelling.arima.tramo.CalendarSpec;
import ec.tstoolkit.modelling.arima.tramo.EasterSpec;
import ec.tstoolkit.modelling.arima.tramo.EstimateSpec;
import ec.tstoolkit.modelling.arima.tramo.OutlierSpec;
import ec.tstoolkit.modelling.arima.tramo.RegressionSpec;
import ec.tstoolkit.modelling.arima.tramo.TransformSpec;
import ec.tstoolkit.timeseries.PeriodSelectorType;
import ec.tstoolkit.timeseries.regression.OutlierDefinition;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import java.util.Formatter;
import java.util.Locale;

/**
 *
 * @author Jean Palate
 */
public class TramoSeatsConverter {

    private static enum Item {

        MQ,
        // transform
        LAM, FCT,
        // model
        INIT,
        IMEAN, P, D, Q, BP, BD, BQ,
        PHI, BPHI, TH, BTH, JPR, JPS, JQR, JQS,
        // auto-modelling
        INIC, IDIF, CANCEL, UB1, UB2, TSIG, PC, PCR,
        // calendar
        ITRAD, IEAST, IDUR,
        // outliers
        IATIP, AIO, VA, IMVX, INT1, INT2,
        // estimate
        TOL, UBP,
        // seats
        SEATS, NOADMISS, EPSPHI, RMOD,
        // regression
        IREG, IUSER, NSER,
        // missing
        INTERP,
        // Demetra specific
        pos, type
    }

    public static enum Format {

        Demetra, TramoSeats
    };
    private Format m_fmt;
    private static final String DM_INPUT = "TRAMO/SEATS SAIP:", TS_INPUT = "INPUT ", DM_REG = "REG:", TS_REG = "REG ";
    private static final String DFMT = "%6g";
    private char m_sep;
    private boolean m_closed;
    private StringBuilder m_builder;

    private void openDocument() {
        m_builder = new StringBuilder();
        m_closed = false;
        if (m_fmt == Format.Demetra) {
            openSection(DM_INPUT);
        } else {
            openSection(TS_INPUT);
        }
    }

    private void openReg() {
        if (m_fmt == Format.Demetra) {
            openSection(DM_REG);
        } else {
            openSection(TS_REG);
        }
    }

    private void openSection(String section) {
        if (m_fmt == Format.Demetra) {
            m_builder.append('[').append(section);
        } else {
            if (!m_closed) {
                m_builder.append('$');
            }
            m_builder.append(section);
        }
        m_closed = false;
    }

    void closeSection() {
        if (m_fmt == Format.Demetra) {
            m_builder.append(']');
        } else if (!m_closed) {
            m_builder.append('$');
        }
        m_closed = true;
    }

    public TramoSeatsConverter(Format fmt) {
        m_fmt = fmt;
        m_sep = fmt == Format.TramoSeats ? ' ' : ',';
    }

    public String encode(TsDomain domain, TramoSeatsSpecification tspec) {
        openDocument();
        m_builder.append(Item.SEATS).append("=2").append(m_sep);
        write(tspec.getTramoSpecification().getTransform());
        write(tspec.getTramoSpecification().getArima());
        write(tspec.getTramoSpecification().getAutoModel());
        write(tspec.getTramoSpecification().getRegression());
        write(domain, tspec.getTramoSpecification().getOutliers());
        writeMissingSpec();
        write(tspec.getTramoSpecification().getEstimate());
        write(tspec.getSeatsSpecification());
        int nregs = regsCount(tspec.getTramoSpecification().getRegression());
        if (nregs > 0) {
            write(Item.IREG, nregs);
            closeSection();
            write(domain, tspec.getTramoSpecification().getRegression());
        } else {
            closeSection();
        }
        return m_builder.toString();
    }

    private int regsCount(RegressionSpec spec) {
        if (spec == null) {
            return 0;
        }
        int n = 0;
        n += spec.getRampsCount();
        n += spec.getOutliersCount();
        n += spec.getUserDefinedVariablesCount();
        return n;
    }

    private void write(Item item, int value) {
        m_builder.append(item).append('=').append(value).append(m_sep);
    }

    private void write(Item item, int idx, int value) {
        m_builder.append(item).append('(').append(idx).append(")=").append(value).append(m_sep);
    }

    private void write(Item item, int idx, double value) {
        m_builder.append(item).append('(').append(idx).append(")=").append(new Formatter().format(DFMT, Locale.ROOT, value)).append(m_sep);
    }

    private void write(Item item, double value) {
        m_builder.append(item).append('=').append(new Formatter().format(DFMT, Locale.ROOT, value)).append(m_sep);
    }

    private void write(Item item, double value, String fmt) {
        m_builder.append(item).append('=').append(new Formatter().format(fmt, Locale.ROOT, value)).append(m_sep);
    }

    private void write(Item item, TsPeriod p) {
        m_builder.append(item).append('=').append(p.getYear()).append('.').append(new Formatter().format("D2", p.getPosition() + 1)).append(m_sep);
    }

    private void write(Item item, int idx, TsPeriod p) {
        m_builder.append(item).append('(').append(idx).append(")=").append(p.getYear()).append('.').append(new Formatter().format("D2", p.getPosition() + 1));
    }

    private void write(SeatsSpecification spec) {
        if (spec == null) {
            return;
        }
        if (spec.getApproximationMode() != ApproximationMode.None) {
            write(Item.NOADMISS, 1);
        }
        if (spec.getTrendBoundary() != SeatsSpecification.DEF_RMOD) {
            write(Item.RMOD, spec.getTrendBoundary());
        }
        if (spec.getSeasTolerance() != SeatsSpecification.DEF_EPSPHI) {
            write(Item.EPSPHI, spec.getSeasTolerance());
        }
    }

    private void write(EstimateSpec spec) {
        if (spec == null) {
            return;
        }
        if (spec.getTol() != EstimateSpec.DEF_TOL) {
            write(Item.TOL, spec.getTol());
        }
        if (spec.getUbp() != EstimateSpec.DEF_UBP) {
            write(Item.UBP, spec.getUbp());
        }
    }

    private void writeMissingSpec() {
        write(Item.INTERP, 2);
    }

    private void write(TsDomain domain, OutlierSpec spec) {
        if (spec == null || !spec.isUsed()) {
            return;
        }
        write(Item.IATIP, 1);
        write(Item.AIO, spec.getAIO());
        if (spec.getCriticalValue() != 0) {
            write(Item.VA, spec.getCriticalValue());
        }
        if (spec.isEML()) {
            write(Item.IMVX, 1);
        }
        if (spec.getSpan().getType() != PeriodSelectorType.All) {
            TsDomain ndom = domain.select(spec.getSpan());
            if (!ndom.isEmpty()) {
                if (m_fmt == Format.TramoSeats) {
                    write(Item.INT1, domain.getLength() + 1);
                } else {
                    write(Item.INT1, ndom.getEnd());
                }
            } else {
                if (m_fmt == Format.TramoSeats) {
                    write(Item.INT1, 1 + (ndom.getStart().minus(domain.getStart())));
                    write(Item.INT2, ndom.getEnd().minus(domain.getStart()));
                } else {
                    write(Item.INT1, ndom.getStart());
                    write(Item.INT2, ndom.getLast());
                }
            }
        }
    }

    private void write(TsDomain domain, RegressionSpec spec) {
        if (spec == null) {
            return;
        }
        write(spec.getCalendar());
        write(domain, spec.getOutliers());
        //write(domain, spec.getRamps());
    }

    private void write(RegressionSpec spec) {
        if (spec == null) {
            return;
        }
        write(spec.getCalendar());
    }

//        private void write(TsDomain domain, CalendarSpec spec)
//        {
//            if (spec == null)
//                return;
//        }
//
    private void write(CalendarSpec spec) {
        if (spec == null) {
            return;
        }
        int itrad = spec.getTradingDays().getTradingDaysType().getVariablesCount();
        if (itrad != 0 && spec.getTradingDays().isTest()) {
            itrad = -itrad;
        }
        if (itrad != 0) {
            write(Item.ITRAD, itrad);
        }
        int ieast = spec.getEaster().getOption() != EasterSpec.Type.Unused ? 1 : 0;
        if (ieast != 0 && spec.getEaster().isTest()) {
            ieast = -ieast;
        }
        if (ieast != 0) {
            write(Item.IEAST, ieast);
        }
        int idur = spec.getEaster().getDuration();
        if (idur != EasterSpec.DEF_IDUR) {
            write(Item.IDUR, idur);
        }
    }

    private void write(TsDomain domain, OutlierDefinition[] spec) {
        if (spec == null || spec.length == 0) {
            return;
        }
        openReg();
        write(Item.IUSER, 2);
        write(Item.NSER, spec.length);
        if (m_fmt == Format.TramoSeats) {
            for (int i = 0; i < spec.length; ++i) {
                m_builder.append((new TsPeriod(domain.getFrequency(), spec[i].position).minus(domain.getStart())) + 1).append(m_sep).append(spec[i].type);
                if (i < spec.length - 1) {
                    m_builder.append(m_sep);
                }
            }
        } else {
            for (int i = 0; i < spec.length; ++i) {
                TsPeriod p = new TsPeriod(domain.getFrequency(), spec[i].position);
                m_builder.append(Item.pos).append('(').append(i + 1).append(")=").append(p.getYear()).append('.').append(new Formatter().format("D2", p.getPosition() + 1));
                m_builder.append(m_sep).append(Item.type).append('(').append(i + 1).append(")=").append(spec[i].type.name().toLowerCase());
                if (i < spec.length - 1) {
                    m_builder.append(m_sep);
                }
            }
        }

        closeSection();
    }

//        private void write(TsDomain domain, Ramp[] spec)
//        {
//            if (spec == null)
//                return;
//        }
//
    private void write(AutoModelSpec spec) {
        if (spec == null || !spec.isEnabled()) {
            return;
        }
        write(Item.INIC, 3);
        write(Item.IDIF, 3);
        if (spec.getCancel() != AutoModelSpec.DEF_CANCEL) {
            write(Item.CANCEL, spec.getCancel());
        }
        if (spec.getUb1() != AutoModelSpec.DEF_UB1) {
            write(Item.UB1, spec.getUb1());
        }
        if (spec.getUb2() != AutoModelSpec.DEF_UB2) {
            write(Item.UB2, spec.getUb2());
        }
        if (spec.getTsig() != AutoModelSpec.DEF_TSIG) {
            write(Item.TSIG, spec.getTsig());
        }
        if (spec.getPc() != AutoModelSpec.DEF_PC) {
            write(Item.PC, spec.getPc());
        }
        if (spec.getPcr() != AutoModelSpec.DEF_PCR) {
            write(Item.PCR, spec.getPcr());
        }
    }

    private void write(ArimaSpec spec) {
        if (spec == null) {
            return;
        }
        write(Item.IMEAN, spec.isMean() ? 1 : 0);
        write(Item.P, spec.getP());
        write(Item.D, spec.getD());
        write(Item.Q, spec.getQ());
        write(Item.BP, spec.getBP());
        write(Item.BD, spec.getBD());
        write(Item.BQ, spec.getBQ());
        if (spec.hasFreeParameters()) {
            write(Item.PHI, Item.JPR, spec.getPhi());
            write(Item.BPHI, Item.JPS, spec.getBPhi());
            write(Item.TH, Item.JQR, spec.getTheta());
            write(Item.BTH, Item.JQS, spec.getBTheta());
        } else {
            write(Item.INIT, 2);
            write(Item.PHI, spec.getPhi());
            write(Item.BPHI, spec.getBPhi());
            write(Item.TH, spec.getTheta());
            write(Item.BTH, spec.getBTheta());

        }
    }

    private void write(Item p, Parameter[] parameter) {
        if (parameter == null || parameter.length == 0) {
            return;
        }
        for (int i = 0; i < parameter.length; ++i) {
            write(p, i + 1, parameter[i].getValue());
        }
    }

    private void write(Item p, Item f, Parameter[] parameter) {
        if (parameter == null || parameter.length == 0) {
            return;
        }
        for (int i = 0; i < parameter.length; ++i) {
            write(p, i + 1, parameter[i].getValue());
        }
        for (int i = 0; i < parameter.length; ++i) {
            //if (parameter[i].Type == TSToolkit.TimeSeries.SimpleTS.Regression.ParameterType.Fixed)
            write(f, i + 1, 1);

        }
    }

    private void write(TransformSpec spec) {
        if (spec == null) {
            return;
        }

        int lam = 0;
        switch (spec.getFunction()) {
            case Auto:
                lam = -1;
                break;
            case None:
                lam = 1;
                break;
        }

        m_builder.append(Item.LAM).append('=').append(lam).append(m_sep);
        if (spec.getFct() != TransformSpec.DEF_FCT) {
            write(Item.FCT, spec.getFct());
        }
    }
}
