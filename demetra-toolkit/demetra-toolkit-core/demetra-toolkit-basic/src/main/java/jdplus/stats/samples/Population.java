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
package jdplus.stats.samples;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
@lombok.ToString
public class Population {

    public static final int UNKNOWN_SIZE = -1;
    public static final Population UNKNOWN = new Population(UNKNOWN_SIZE, Double.NaN, Double.NaN, false);
    public static final Population ZEROMEAN = new Population(UNKNOWN_SIZE, 0, Double.NaN, true);

    private final int size;
    private final double mean, variance;
    private final boolean normal;

    public static class PopulationBuilder{
    private int size=UNKNOWN_SIZE;
    private double mean, variance=Double.NaN;
    private boolean normal=true;
        
    }
}
