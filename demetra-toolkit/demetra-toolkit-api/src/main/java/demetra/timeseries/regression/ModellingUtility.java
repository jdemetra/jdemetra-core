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
package demetra.timeseries.regression;

import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IMovingHolidayVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.IUserVariable;
import demetra.timeseries.regression.ModifiedTsVariable;
import demetra.timeseries.regression.Variable;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ModellingUtility {

    public final String AMI = "ami", AMI_PREVIOUS = "ami_previous";

    public boolean isAutomaticallyIdentified(Variable var) {
        return var.hasAttribute(AMI);
    }

    public boolean isOutlier(Variable var) {
        return var.getCore() instanceof IOutlier;
    }

    /**
     * Gets automatic or prespecified outliers
     *
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
        if (var.getCore() instanceof IMovingHolidayVariable) {
            return true;
        }
        if ((var.getCore() instanceof ModifiedTsVariable)) {
            ModifiedTsVariable mvar = (ModifiedTsVariable) var.getCore();
            return mvar.getVariable() instanceof IMovingHolidayVariable;
        }
        return false;
    }

    public boolean isEaster(Variable var) {
        if (var.getCore() instanceof IEasterVariable) {
            return true;
        }
        if ((var.getCore() instanceof ModifiedTsVariable)) {
            ModifiedTsVariable mvar = (ModifiedTsVariable) var.getCore();
            return mvar.getVariable() instanceof IEasterVariable;
        }
        return false;
    }

    public boolean isTradingDays(Variable var) {
        if (var.getCore() instanceof ITradingDaysVariable) {
            return true;
        }
        if ((var.getCore() instanceof ModifiedTsVariable)) {
            ModifiedTsVariable mvar = (ModifiedTsVariable) var.getCore();
            return mvar.getVariable() instanceof ITradingDaysVariable;
        }
        return false;
    }

    public boolean isLengthOfPeriod(Variable var) {
        if (var.getCore() instanceof ILengthOfPeriodVariable) {
            return true;
        }
        if ((var.getCore() instanceof ModifiedTsVariable)) {
            ModifiedTsVariable mvar = (ModifiedTsVariable) var.getCore();
            return mvar.getVariable() instanceof ILengthOfPeriodVariable;
        }
        return false;
    }

    public boolean isDaysRelated(Variable var) {
        return isTradingDays(var) || isLengthOfPeriod(var);
    }

    public boolean isCalendar(Variable var) {
        return isTradingDays(var) || isLengthOfPeriod(var) || isMovingHoliday(var);
    }

    public boolean isUser(Variable var) {
        if (var.getCore() instanceof IUserVariable) {
            return true;
        }
        if ((var.getCore() instanceof ModifiedTsVariable)) {
            ModifiedTsVariable mvar = (ModifiedTsVariable) var.getCore();
            return mvar.getVariable() instanceof IUserVariable;
        }
        return false;
    }

}
