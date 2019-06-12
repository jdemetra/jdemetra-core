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
package demetra.modelling.regsarima;

import demetra.modelling.regression.PreadjustmentVariable;
import demetra.modelling.regression.Variable;
import demetra.timeseries.TsData;
import demetra.timeseries.calendars.LengthOfPeriodType;
import java.util.List;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.Builder
public class RegSarimaDescription {
    private TsData series;
    private boolean logTransformation;
    private LengthOfPeriodType lengthOfPeriodTransformation;
    @lombok.Singular
    private List<PreadjustmentVariable> preadjustmentVariables;
    private boolean mean;
    @lombok.Singular
    private List<Variable> variables;
    private SarimaSpec arima;
}
