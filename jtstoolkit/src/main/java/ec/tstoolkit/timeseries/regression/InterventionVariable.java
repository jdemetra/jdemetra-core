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
package ec.tstoolkit.timeseries.regression;

import ec.tstoolkit.data.DataBlock;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.information.InformationSetSerializable;
import ec.tstoolkit.maths.polynomials.Polynomial;
import ec.tstoolkit.maths.polynomials.RationalFunction;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import ec.tstoolkit.timeseries.simplets.TsPeriod;
import ec.tstoolkit.utilities.Comparator;
import ec.tstoolkit.utilities.Jdk6;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public class InterventionVariable extends AbstractSingleTsVariable implements IUserTsVariable, Cloneable, InformationSetSerializable {

    public static final String NAME = "name",
            DELTA = "delta",
            DELTAS = "deltas",
            SEQS = "sequences";

    public static void fillDictionary(String prefix, Map<String, Class> dic) {
        dic.put(InformationSet.item(prefix, SEQS), String[].class);
        dic.put(InformationSet.item(prefix, NAME), String.class);
        dic.put(InformationSet.item(prefix, DELTA), Double.class);
        dic.put(InformationSet.item(prefix, DELTAS), Double.class);
    }

    private static final String DESC = "Intervention variable";
    private String desc_ = null;
    private double delta_, deltas_;
    private ArrayList<Sequence> seqs_ = new ArrayList<>();

    @Override
    public InterventionVariable clone() {
        try {
            InterventionVariable spec = (InterventionVariable) super.clone();
            spec.seqs_ = new ArrayList<>();
            for (Sequence seq : seqs_) {
                spec.seqs_.add(seq.clone());
            }
            return spec;
        } catch (CloneNotSupportedException ex) {
            throw new AssertionError();
        }
    }

    @Override
    public void data(TsPeriod start, DataBlock data) {
        int dcount = data.getLength();
        data.set(0);
        //double[] rslt = new double[dcount];
        if (seqs_.isEmpty()) {
            return;
        }
        // first, generates the 0/1
        TsPeriod pstart = new TsPeriod(start.getFrequency()), pend = new TsPeriod(
                start.getFrequency());
        // search the Start / End of the sequences
        pstart.set(seqs_.get(0).start);
        pend.set(seqs_.get(0).end);

        for (int i = 1; i < seqs_.size(); ++i) {
            if (pstart.isAfter(seqs_.get(i).start)) {
                pstart.set(seqs_.get(i).start);
            }
            if (pend.isBefore(seqs_.get(i).end)) {
                pend.set(seqs_.get(i).end);
            }
        }

        // period of estimation : Start->domain[last]
        int n = dcount + start.minus(pstart);
        if (n < 0) {
            return;
        }

        double[] tmp = new double[n];
        TsPeriod curstart = new TsPeriod(start.getFrequency()), curend = new TsPeriod(
                start.getFrequency());
        for (int i = 0; i < seqs_.size(); ++i) {
            curstart.set(seqs_.get(i).start);
            curend.set(seqs_.get(i).end);

            int istart = curstart.minus(pstart);
            int iend = 1 + curend.minus(pstart);
            if (iend > n) {
                iend = n;
            }
            for (int j = istart; j < iend; ++j) {
                tmp[j] += 1;
            }
        }

        if (delta_ != 0 || deltas_ != 0) {
            // construct the filter
            Polynomial num = Polynomial.ONE;
            Polynomial d = Polynomial.valueOf(1, -delta_);
            if (start.getFrequency() != TsFrequency.Yearly) {
                double[] ds = Polynomial.Doubles.fromDegree(start.getFrequency().intValue());
                ds[0] = 1;
                ds[start.getFrequency().intValue()] = -deltas_;
                d = d.times(Polynomial.of(ds));
            }
            RationalFunction rf = new RationalFunction(num, d);
            double[] w = rf.coefficients(n);

            // apply the filter
            double[] ftmp = new double[n];
            for (int i = 0; i < ftmp.length; ++i) {
                if (tmp[i] != 0) {
                    for (int j = 0; j < ftmp.length - i; ++j) {
                        ftmp[i + j] += tmp[i] * w[j];
                    }
                }
            }
            tmp = ftmp;
        }
        // copy in rslt
        int di = pstart.minus(start);
        if (di > 0) {
            data.drop(di, 0).copyFrom(tmp, 0);
        } else {
            data.copyFrom(tmp, -di);
        }
    }

    @Override
    public String getDescription(TsFrequency context) {
        return desc_ == null ? toString() : desc_;
    }

    @Override
    public boolean isSignificant(TsDomain domain) {
        // TODO
        // should be improved...
        DataBlock s = new DataBlock(domain.getLength());
        data(domain.getStart(), s);
        return !s.isConstant();
    }

    public void add(Day start, Day end) {
        seqs_.add(new Sequence(start, end));
    }

    public void clear() {
        seqs_.clear();
    }

    public int getCount() {
        return seqs_.size();
    }

    public Sequence getSequence(int idx) {
        return seqs_.get(idx);
    }

    public double getDelta() {
        return delta_;
    }

    public void setDelta(double value) {
        delta_ = value;
    }

    public double getDeltaS() {
        return deltas_;
    }

    public void setDeltaS(double value) {
        deltas_ = value;
    }

    public boolean getD1DS() {
        return delta_ == 1 && deltas_ == 1;
    }

    public void setD1DS(boolean value) {
        if (value) {
            delta_ = 1;
            deltas_ = 1;
        } else {
            delta_ = 0;
            deltas_ = 0;
        }
    }

    public Sequence[] getSequences() {
        return Jdk6.Collections.toArray(seqs_, Sequence.class);
    }

    public void setSequences(Sequence[] seqs) {
        seqs_.clear();

        if (seqs != null) {
            Collections.addAll(seqs_, seqs);
        }
    }

   public void setDescription(String desc) {
        desc_ = desc;
    }

   @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof InterventionVariable && equals((InterventionVariable) obj));
    }

    private boolean equals(InterventionVariable other) {
        return other.delta_ == delta_ && other.deltas_ == deltas_
                && Comparator.equals(other.seqs_, seqs_);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Jdk6.Double.hashCode(this.delta_);
        hash = 71 * hash + Jdk6.Double.hashCode(this.deltas_);
        hash = 71 * hash + Arrays.hashCode(this.seqs_.toArray());
        return hash;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = new InformationSet();
        if (verbose || delta_ != 0) {
            info.add(DELTA, delta_);
        }
        if (verbose || deltas_ != 0) {
            info.add(DELTAS, deltas_);
        }
        if (desc_ != null) {
            info.add(NAME, desc_);
        }
        String[] seqs = new String[seqs_.size()];
        for (int i = 0; i < seqs.length; ++i) {
            seqs[i] = seqs_.get(i).toString();
        }
        info.add(SEQS, seqs);
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        try {
            Double delta = info.get(DELTA, Double.class);
            if (delta != null) {
                delta_ = delta;
            }
            Double deltas = info.get(DELTAS, Double.class);
            if (deltas != null) {
                deltas_ = deltas;
            }
            String desc = info.get(NAME, String.class);
            if (desc != null) {
                desc_ = desc;
            }
            String[] seqs = info.get(SEQS, String[].class);
            if (seqs != null) {
                for (int i = 0; i < seqs.length; ++i) {
                    Sequence cur = Sequence.fromString(seqs[i]);
                    if (cur == null) {
                        return false;
                    }
                    seqs_.add(cur);
                }
            }
            return true;
        } catch (Exception err) {
            return false;
        }
    }

    @Override
    public String toString() {
        if (seqs_.isEmpty()) {
            return "I:{}";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("I:");
        builder.append('{');
        builder.append(seqs_.get(0));
        for (int i = 1; i < seqs_.size(); ++i) {
            builder.append(';').append(seqs_.get(i));
        }
        builder.append('}');
        appendDetails(builder);
        return builder.toString();
    }

    private void appendDetails(StringBuilder builder) {
        int nd = (delta_ == 0 ? 0 : 1) + (deltas_ == 0 ? 0 : 1);
        if (nd > 0) {
            DecimalFormat fmt = new DecimalFormat();
            fmt.setMaximumFractionDigits(2);
            builder.append('(');
            if (delta_ != 0) {
                builder.append("delta=").append(fmt.format(delta_));
                if (nd == 2) {
                    builder.append(", ");
                }
            }
            if (deltas_ != 0) {
                builder.append("deltas=").append(fmt.format(deltas_));
            }
            builder.append(')');
        }

    }

    public String toString(TsFrequency freq) {
        if (seqs_.isEmpty()) {
            return "I:{}";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("I:");
        builder.append('{');
        builder.append(seqs_.get(0).toString(freq));
        for (int i = 1; i < seqs_.size(); ++i) {
            builder.append(';').append(seqs_.get(i).toString(freq));
        }
        builder.append('}');
        appendDetails(builder);
        return builder.toString();
    }
    
    @Override
    public String getName(){
        return desc_ == null ? toString().replace('.','$') : desc_;
    }
}
