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
package demetra.sarima;

import demetra.design.Development;
import demetra.design.IBuilder;
import demetra.design.Immutable;
import javax.annotation.Nonnull;
import demetra.data.DoubleReader;
import demetra.data.DoubleSequence;

/**
 * Box-Jenkins seasonal arima model AR(B)* SAR(B)*D(B)*SD(B) y(t) =
 * MA(B)*SMA(B)e(t), e~N(0, var) AR(B) = 1+a(1)B+...+a(p)B^p, regular
 * auto-regressive polynomial SAR(B) = 1+b(1)B^s+...+b(bp)B^s*bp, seasonal
 * auto-regressive polynomial D(B) = 1+e(1)B+...+e(d)B^d, regula differencing
 * polynomial SD(B) = 1+f(1)B^s+...+f(bd)B^s*bd, seasonal differencing
 * polynomial MA(B) = 1+c(1)B+...+c(q)B^q, regular moving average polynomial
 * SMA(B) = 1+d(1)B^s+...+d(bq)B^s*bq, seasonal moving average polynomial
 *
 * @author Jeremy Demortier, Jean Palate
 */
@Development(status = Development.Status.Alpha)
@Immutable
public class SarimaModelType  {

    public static class Builder implements IBuilder<SarimaModelType> {

        private static final double[] E = new double[0];

        private final int s;
        private int d, bd;
        private double[] phi, bphi, th, bth;
  
        private Builder(SarimaSpecification spec) {
            s = spec.frequency;
            d = spec.D;
            bd = spec.BD;
            phi = (spec.P > 0) ? new double[spec.P] : E;
            bphi = (spec.BP > 0) ? new double[spec.BP] : E;
            th = (spec.Q > 0) ? new double[spec.Q] : E;
            bth = (spec.BQ > 0) ? new double[spec.BQ] : E;
        }
        
        private Builder(SarmaSpecification spec) {
            s = spec.frequency;
            d = 0;
            bd = 0;
            phi = (spec.P > 0) ? new double[spec.P] : E;
            bphi = (spec.BP > 0) ? new double[spec.BP] : E;
            th = (spec.Q > 0) ? new double[spec.Q] : E;
            bth = (spec.BQ > 0) ? new double[spec.BQ] : E;
        }
        
        private double[] clone(double[] c){
            if (c.length == 0)
                return E;
            else
                return c.clone();
        }
        
        private Builder(SarimaModelType model) {
            s = model.s;
            d = model.d;
            bd = model.bd;
            phi = clone(model.phi);
            bphi = clone(model.bphi);
            th = clone(model.th);
            bth = clone(model.bth);
        }

        public Builder setDefault() {
            return setDefault(-0.1, -0.2);
        }

        public Builder setDefault(double ar, double ma) {
            for (int i=0; i<phi.length; ++i)
                phi[i]=ar;
            for (int i=0; i<bphi.length; ++i)
                bphi[i]=ar;
            for (int i=0; i<th.length; ++i)
                th[i]=ma;
            for (int i=0; i<bth.length; ++i)
                bth[i]=ma;
            return this;
        }
        
        public Builder parameters(DoubleSequence p){
            DoubleReader reader = p.reader();
            for (int i=0; i<phi.length; ++i)
                phi[i]=reader.next();
            for (int i=0; i<bphi.length; ++i)
                bphi[i]=reader.next();
            for (int i=0; i<th.length; ++i)
                th[i]=reader.next();
            for (int i=0; i<bth.length; ++i)
                bth[i]=reader.next();
            return this;
        }

        public Builder phi(int lag, double val) {
            phi[lag - 1] = val;
            return this;
        }

        public Builder bphi(int lag, double val) {
            bphi[lag - 1] = val;
            return this;
        }

        public Builder theta(int lag, double val) {
            th[lag - 1] = val;
            return this;
        }

        public Builder btheta(int lag, double val) {
            bth[lag - 1] = val;
            return this;
        }

        public Builder phi(@Nonnull double... val) {
            System.arraycopy(val, 0, phi, 0, phi.length);
            return this;
        }

        public Builder bphi(@Nonnull double... val) {
            System.arraycopy(val, 0, bphi, 0, bphi.length);
            return this;
        }

        public Builder theta(@Nonnull double... val) {
            System.arraycopy(val, 0, th, 0, th.length);
            return this;
        }

        public Builder btheta(@Nonnull double... val) {
            System.arraycopy(val, 0, bth, 0, bth.length);
            return this;
        }

        public Builder differencing(int d, int bd) {
            this.d = d;
            this.bd = bd;
            return this;
        }

        private void adjust() {
            double[] nphi = adjust(phi);
            if (nphi != null) {
                phi = nphi;
            }
            double[] nbphi = adjust(bphi);
            if (nbphi != null) {
                bphi = nbphi;
            }
            double[] nth = adjust(th);
            if (nth != null) {
                th = nth;
            }
            double[] nbth = adjust(bth);
            if (nbth != null) {
                bth = nbth;
            }
        }

        private double[] adjust(double[] p) {
            int l = p.length;
            for (int i = l - 1; i >= 0; --i) {
                if (Math.abs(p[i]) < EPS) {
                    --l;
                } else {
                    break;
                }
            }
            if (l != p.length) {
                double[] np = new double[l];
                if (l > 0) {
                    System.arraycopy(p, 0, np, 0, l);
                }
                return np;
            } else {
                return null;
            }
        }

        @Override
        public SarimaModelType build() {
            adjust();
            return new SarimaModelType(this);
        }
    }
    
    public static Builder builder(SarimaSpecification spec){
        return new Builder(spec);
    }

    public static Builder builder(SarmaSpecification spec){
        return new Builder(spec);
    }

    private static final double EPS = 1e-6;

    private final int s;
    private int d, bd;
    private double[] phi, bphi, th, bth;

    /**
     *
     */
    private SarimaModelType(Builder builder) {
        this.s = builder.s;
        this.d = builder.d;
        this.bd = builder.bd;
        this.phi = builder.phi;
        this.bphi = builder.bphi;
        this.th = builder.th;
        this.bth = builder.bth;
    }

    public DoubleSequence parameters() {
        double[] p = new double[phi.length + bphi.length + th.length + bth.length];
        int pos = 0;
        if (phi.length > 0) {
            System.arraycopy(phi, 0, p, pos, phi.length);
            pos += phi.length;
        }
        if (bphi.length > 0) {
            System.arraycopy(bphi, 0, p, pos, bphi.length);
            pos += bphi.length;
        }
        if (th.length > 0) {
            System.arraycopy(th, 0, p, pos, th.length);
            pos += th.length;
        }
        if (bth.length > 0) {
            System.arraycopy(bth, 0, p, pos, bth.length);
        }
        return DoubleSequence.ofInternal(p);
    }

    /**
     *
     * @param lag
     * @return
     */
    public double phi(final int lag) {
        return phi[lag - 1];
    }

    /**
     *
     * @param lag
     * @return
     */
    public double bphi(final int lag) {
        return bphi[lag - 1];
    }

    /**
     *
     * @param lag
     * @return
     */
    public double theta(final int lag) {
        return th[lag - 1];
    }
    /**
     *
     * @param lag
     * @return
     */
    public double btheta(final int lag) {
        return bth[lag - 1];
    }

    /**
     *
     * @return
     */
    public int getDifferencingOrder() {
        int n = d;
        if (s > 1) {
            n += s * bd;
        }
        return n;
    }

    /**
     *
     * @return
     */
    public int getFrequency() {
        return s;
    }

    /**
     *
     * @return
     */
    public int getParametersCount() {
        return phi.length + bphi.length + th.length + bth.length;
    }

    /**
     *
     * @return
     */
    public int getRegularDifferenceOrder() {
        return d;
    }

    public int getRegularAROrder() {
        return phi.length;
    }

    public int getRegularMAOrder() {
        return th.length;
    }

    /**
     *
     * @return
     */
    public int getSeasonalDifferenceOrder() {
        return bd;
    }

    public int getSeasonalAROrder() {
        return bphi.length;
    }

    public int getSeasonalMAOrder() {
        return bth.length;
    }

    /**
     *
     * @return
     */
    public SarimaSpecification specification() {
        SarimaSpecification spec = new SarimaSpecification(s);
        spec.D = d;
        spec.BD = bd;
        spec.P = phi.length;
        spec.BP = bphi.length;
        spec.Q = th.length;
        spec.BQ = bth.length;
        return spec.clone();
    }


}
