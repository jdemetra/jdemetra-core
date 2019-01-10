/*
 * Copyright 2016 National Bank of Belgium
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
package demetra.benchmarking.multivariate;

import demetra.algorithms.AlgorithmDescriptor;
import demetra.data.AggregationType;
import demetra.util.Comparator;
import demetra.util.WeightedItem;
import demetra.util.WildCards;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import demetra.processing.ProcSpecification;

/**
 *
 * @author Jean Palate
 */
@lombok.Data
public class MultivariateCholetteSpecification implements ProcSpecification {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "multivariatecholette", null);

    /**
     * Description of a contemporaneous constraint. The constraint may be
     * binding y = w1*x1+...+wn*xn or free constant = w1*x1+...+wn*xn
     */
    public final static class ContemporaneousConstraintDescriptor {

        public ContemporaneousConstraintDescriptor(double cnt) {
            constant = cnt;
            constraint = null;
        }

        public ContemporaneousConstraintDescriptor(String cnt) {
            constant = 0;
            constraint = cnt;
        }

        private ContemporaneousConstraintDescriptor(double d, String s) {
            constant = d;
            constraint = s;
        }

        public void add(String cmp, double w) {
            this.components.add(new WeightedItem<>(cmp, w));
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof ContemporaneousConstraintDescriptor && equals((ContemporaneousConstraintDescriptor) obj));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 23 * hash + (int) (Double.doubleToLongBits(this.constant) ^ (Double.doubleToLongBits(this.constant) >>> 32));
            hash = 23 * hash + (this.constraint != null ? this.constraint.hashCode() : 0);
            return hash;
        }

        public boolean equals(ContemporaneousConstraintDescriptor c) {
            if (!this.constraint.equals(c.constraint)) {
                return false;
            }
            if (this.constant != c.constant) {
                return false;
            }
            return Comparator.equals(this.components, c.components);
        }
        public final double constant;
        public final String constraint;
        public final List<WeightedItem<String>> components
                = new ArrayList<>();

        public boolean hasWildCards() {
            for (WeightedItem<String> ws : components) {
                if (ws.getItem().contains("?") || ws.getItem().contains("*")) {
                    return true;
                }
            }
            return false;
        }

        public ContemporaneousConstraintDescriptor expand(Collection<String> input) {
            ContemporaneousConstraintDescriptor ndesc = new ContemporaneousConstraintDescriptor(constant, constraint);
            for (WeightedItem<String> ws : components) {
                double w = ws.getWeight();
                if (ws.getItem().contains("*") || ws.getItem().contains("?")) {
                    WildCards wc = new WildCards(ws.getItem());
                    for (String i : input) {
                        if (!i.equals(constraint) && wc.match(i)) {
                            ndesc.components.add(new WeightedItem<>(i, w));
                        }
                    }
                } else {
                    ndesc.components.add(ws);
                }
            }
            return ndesc;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (constraint != null) {
                builder.append(constraint);
            } else {
                builder.append(constant);
            }
            builder.append('=');

            boolean first = true;
            for (WeightedItem<String> ws : components) {
                double w = ws.getWeight();
                if (w == 0) {
                    continue;
                }
                double aw = Math.abs(w);
                if (w < 0) {
                    builder.append('-');
                } else if (!first) {
                    builder.append('+');
                }
                if (aw != 1) {
                    builder.append(aw).append('*');
                }
                builder.append(ws.getItem());
                first = false;
            }
            return builder.toString();
        }

        public static ContemporaneousConstraintDescriptor parse(String s) {
            try {
                Scanner scanner = new Scanner(s).useDelimiter("\\s*=\\s*");
                String n = scanner.next();
                // try first double
                double dcnt = 0;
                String scnt = null;
                try {
                    dcnt = Double.parseDouble(n);
                } catch (NumberFormatException err) {
                    scnt = n;
                }
                n = scanner.next();
                ContemporaneousConstraintDescriptor desc = new ContemporaneousConstraintDescriptor(dcnt, scnt);
                if (!parseComponents(n, desc.components)) {
                    return null;
                }
                return desc;
            } catch (Exception err) {
                return null;
            }
        }

        private static boolean parseComponents(String str, List<WeightedItem<String>> cmps) {
            int pos = 0;
            int ppos = 0, mpos = 0;
            while (pos < str.length()) {
                if (Character.isWhitespace(str.charAt(pos))) {
                    ++pos;
                } else {
                    if (ppos >= 0) {
                        ppos = str.indexOf('+', pos + 1);
                    }
                    if (mpos >= 0) {
                        mpos = str.indexOf('-', pos + 1);
                    }
                    int npos;
                    if (ppos < 0) {
                        npos = mpos;
                    } else if (mpos < 0) {
                        npos = ppos;
                    } else {
                        npos = Math.min(ppos, mpos);
                    }
                    if (npos < 0) {
                        npos = str.length();
                    }
                    char c = str.charAt(pos);
                    boolean plus = c != '-';
                    if (c == '-' || c == '+') {
                        ++pos;
                    }
                    String cur = str.substring(pos, npos);
                    int wpos = cur.indexOf('*');
                    if (wpos < 0) {
                        String cmp = cur.trim();
                        cmps.add(new WeightedItem<>(cmp, plus ? 1 : -1));
                    } else {
                        String cmp = cur.substring(wpos + 1).trim();
                        try {
                            double w = Double.parseDouble(cur.substring(0, wpos));
                            cmps.add(new WeightedItem<>(cmp, plus ? w : -w));
                        } catch (NumberFormatException err) {
                            return false;
                        }
                    }
                    pos = npos;
                }
            }
            return true;
        }
    }

    public final static class TemporalConstraintDescriptor {

        public final String aggregate, detail;

        public TemporalConstraintDescriptor(final String constraint, final String component) {
            this.aggregate = constraint;
            this.detail = component;
        }

        public static TemporalConstraintDescriptor parse(String s) {
            try {
                Scanner scanner = new Scanner(s).useDelimiter("\\s*=\\s*");
                String n = scanner.next();
                String cnt = n;
                n = scanner.next();
                String[] fn = function(n);
                if (fn == null) {
                    return null;
                }
                if (!fn[0].equalsIgnoreCase("sum")) {
                    return null;
                }
                String cmp = fn[1];
                return new TemporalConstraintDescriptor(cnt, cmp);
            } catch (Exception err) {
                return null;
            }
        }

        public static String[] function(String str) {
            try {
                String s = str.trim();
                int a0 = s.indexOf('(', 0);
                if (a0 <= 0) {
                    return null;
                }
                int a1 = s.indexOf('(', a0 + 1);
                if (a1 >= 0) {
                    return null;
                }
                a1 = s.indexOf(')', a0 + 1);
                if (a1 != s.length() - 1) {
                    return null;
                }
                return new String[]{s.substring(0, a0), s.substring(a0 + 1, a1)};
            } catch (Exception err) {
                return null;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(aggregate).append("=sum(")
                    .append(detail).append(')');
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj instanceof TemporalConstraintDescriptor && equals((TemporalConstraintDescriptor) obj));
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 67 * hash + this.aggregate.hashCode();
            hash = 67 * hash + this.detail.hashCode();
            return hash;
        }

        public boolean equals(TemporalConstraintDescriptor c) {
            return this.aggregate.equals(c.aggregate) && this.detail.equals(c.detail);
        }
    }

    public static double DEF_LAMBDA = 1, DEF_RHO = 1;
    private double rho = DEF_RHO;
    private double lambda = DEF_LAMBDA;
    @lombok.NonNull
    private AggregationType aggregationType = AggregationType.Sum;
    private final List<ContemporaneousConstraintDescriptor> contemporaneousConstraints
            = new ArrayList<>();
    private final List<TemporalConstraintDescriptor> temporalConstraints
            = new ArrayList<>();
    
    public void add(ContemporaneousConstraintDescriptor cdesc){
        contemporaneousConstraints.add(cdesc);
    }

    public void add(TemporalConstraintDescriptor cdesc){
        temporalConstraints.add(cdesc);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public MultivariateCholetteSpecification makeCopy() {
        MultivariateCholetteSpecification spec = new MultivariateCholetteSpecification();
        spec.rho = rho;
        spec.lambda = lambda;
        spec.aggregationType=aggregationType;
        spec.contemporaneousConstraints.addAll(contemporaneousConstraints);
        spec.temporalConstraints.addAll(temporalConstraints);
        return spec;
    }

}
