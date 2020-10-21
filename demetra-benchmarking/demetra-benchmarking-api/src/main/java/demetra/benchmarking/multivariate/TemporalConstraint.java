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

import nbbrd.design.Development;
import java.util.Scanner;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
public class TemporalConstraint {

    @lombok.NonNull
    private final String aggregate, detail;

    public static TemporalConstraint parse(String s) {
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
            return new TemporalConstraint(cnt, cmp);
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

}
