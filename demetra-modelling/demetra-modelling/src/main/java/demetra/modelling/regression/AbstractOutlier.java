/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */

package demetra.modelling.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import java.time.LocalDateTime;
import java.time.Period;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public abstract class AbstractOutlier extends BaseOutlier implements IRegularOutlier {


    protected AbstractOutlier(LocalDateTime pos, String name) {
        super(pos, name);
    }

    @Override
    public void data(TsPeriod start, DataBlock buffer) {
        long outlierPos = start.idAt(getPosition()) - start.getId();
        data((int) outlierPos, buffer);
    }

    protected abstract void data(int pos, DataBlock buffer);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription(TsDomain context) {
        return IOutlier.defaultName(getCode(), position, context);
    }

    // / <summary>Position of the outlier</summary>
    @Override
    public LocalDateTime getPosition() {
        return position;
    }

}
