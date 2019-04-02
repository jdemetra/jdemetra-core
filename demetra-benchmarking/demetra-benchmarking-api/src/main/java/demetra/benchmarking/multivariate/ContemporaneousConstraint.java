/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package demetra.benchmarking.multivariate;

import demetra.design.Development;
import demetra.util.WeightedItem;
import demetra.util.WildCards;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

/**
 * Description of a contemporaneous constraint. The constraint may be binding y
 * = w1*x1+...+wn*xn or free constant = w1*x1+...+wn*xn
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder
public class ContemporaneousConstraint {

    private double constant;
    private String constraint;
    @lombok.Singular
    @lombok.NonNull
    private List<WeightedItem<String>> components;

    public boolean hasWildCards() {
        for (WeightedItem<String> ws : components) {
            if (ws.getItem().contains("?") || ws.getItem().contains("*")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Expands a constraint with wild cards
     *
     * @param input
     * @return
     */
    public ContemporaneousConstraint expand(Collection<String> input) {
        ContemporaneousConstraintBuilder builder = builder()
                .constant(constant)
                .constraint(constraint);
        for (WeightedItem<String> ws : components) {
            double w = ws.getWeight();
            if (ws.getItem().contains("*") || ws.getItem().contains("?")) {
                WildCards wc = new WildCards(ws.getItem());
                for (String i : input) {
                    if (!i.equals(constraint) && wc.match(i)) {
                        builder.component(new WeightedItem<>(i, w));;
                    }
                }
            } else {
                builder.component(ws);
            }
        }
        return builder.build();
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

    /**
     * Parse a string to a contemporaneous constraint The string should have the
     * following form - "y" = [w1*]"x1"+...+[wn*]"xn" - constant =
     * [w1*]"x1"+...+[wn*]"xn"
     *
     * (+ can be replaced by -)
     *
     * @param s
     * @return
     */
    public static ContemporaneousConstraint parse(String s) {
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
            List<WeightedItem<String>> cmps = new ArrayList<>();
            if (!parseComponents(n, cmps)) {
                return null;
            }
            return ContemporaneousConstraint.builder()
                    .constant(dcnt)
                    .constraint(scnt)
                    .components(cmps)
                    .build();
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
                        cmps.add(new WeightedItem<>(cur.trim(), plus ? 1 : -1));
                    }
                }
                pos = npos;
            }
        }
        return true;
    }
}
