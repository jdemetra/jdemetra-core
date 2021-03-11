/*
 * Copyright 2020 National Bank of Belgium
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
package jdplus.regarima.ami;

import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IMovingHolidayVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.Variable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingUtility {

    public final String AMI = "ami", AMI_PREVIOUS="ami_previous";

    public boolean isAutomaticallyIdentified(Variable var) {
        return var.hasAttribute(AMI);
    }

    public boolean isOutlier(Variable var) {
        return var.getCore() instanceof IOutlier;
    }

    /**
     * Gets automatic or prespecified outliers
     * @param var
     * @param ami True if you are looking for automatically identified outliers
     * @return 
     */
    public boolean isOutlier(Variable var, boolean ami) {
        if (var.getCore() instanceof IOutlier) {
            return ami == var.hasAttribute(AMI);
        } else {
            return false;
        }
    }


    public boolean isMovingHoliday(Variable var) {
        return var.getCore() instanceof IMovingHolidayVariable;
    }

    public boolean isEaster(Variable var) {
        return var.getCore() instanceof IEasterVariable;
    }

    public boolean isTradingDays(Variable var) {
        return var.getCore() instanceof ITradingDaysVariable;
    }

    public boolean isLengthOfPeriod(Variable var) {
        return var.getCore() instanceof ILengthOfPeriodVariable;
    }

    public boolean isDaysRelated(Variable var) {
        return isTradingDays(var) || isLengthOfPeriod(var);
    }

    public boolean isCalendar(Variable var) {
        return isTradingDays(var) || isLengthOfPeriod(var) || isMovingHoliday(var);
    }

    public boolean isUser(Variable var) {
        return !isCalendar(var) && !isOutlier(var);
    }

}
