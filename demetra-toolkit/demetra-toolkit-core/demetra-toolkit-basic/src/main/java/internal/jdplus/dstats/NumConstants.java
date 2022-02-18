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
package internal.jdplus.dstats;

import nbbrd.design.Development;

/**
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
final class NumConstants {
    /**
         *
         */
    public static final double SQRPI = 1.0 / Math.sqrt(Math.PI * 2);

    /**
         *
         */
    public static final double ROOT32 = Math.sqrt(32.0);
    /**
         *
         */
    public static final double THRSH = 0.66291;
    /**
         *
         */
    public static final double EPS = Double.MIN_VALUE * 0.5;
    /**
         *
         */
    public static final double MIN = Double.NEGATIVE_INFINITY;

    private NumConstants() {
    }
}
