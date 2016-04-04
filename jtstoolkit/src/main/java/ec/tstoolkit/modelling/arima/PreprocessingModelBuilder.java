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

package ec.tstoolkit.modelling.arima;

import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.design.Development;
import ec.tstoolkit.modelling.RegStatus;
import ec.tstoolkit.timeseries.regression.EasterVariable;
import ec.tstoolkit.timeseries.regression.JulianEasterVariable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Preliminary)
public class PreprocessingModelBuilder {

    public static boolean updateCalendar(ModelDescription model, boolean usecalendar) {
        if (model.getCalendars().isEmpty()) {
            return false;
        }
        boolean changed = false;
        if (usecalendar) {
            for (Variable cal : model.getCalendars()) {
                if (cal.status.needTesting()) {
                    if (!cal.status.isSelected()) {
                        changed = true;
                    }
                    cal.status = RegStatus.Accepted;
                }
            }
        } else {
            for (Variable cal : model.getCalendars()) {
                if (cal.status.needTesting()) {
                    if (cal.status.isSelected()) {
                        changed = true;
                    }
                    cal.status = RegStatus.Rejected;
                }
            }
        }
        if (changed && model.getPreadjustmentType() == PreadjustmentType.Auto) {
            model.invalidateData();
        }
        return changed;
    }

    public static boolean updateEaster(ModelDescription model, int duration) {
        boolean changed = false;
        for (Variable mh : model.getMovingHolidays()) {
            if (mh.status.needTesting() && mh.getVariable() instanceof EasterVariable) {
                if (duration == 0) {
                    if (mh.status.isSelected()) {
                        changed = true;
                    }
                    mh.status = RegStatus.Rejected;
                } else {
                    EasterVariable old = (EasterVariable) mh.getVariable();
                    if (old != null) {
                        if (old.getDuration() != duration) {
                            EasterVariable easter = new EasterVariable();
                            easter.setType(old.getType());
                            easter.setDuration(duration);
                            mh.setVariable(easter);
                            mh.status = RegStatus.Accepted;
                            changed = true;
                        } else {
                            changed = !mh.status.isSelected();
                        }
                        mh.status = RegStatus.Accepted;
                    }
                }
            }
            else if (mh.status.needTesting() && mh.getVariable() instanceof JulianEasterVariable) {
                if (duration == 0) {
                    if (mh.status.isSelected()) {
                        changed = true;
                    }
                    mh.status = RegStatus.Rejected;
                } else {
                    JulianEasterVariable old = (JulianEasterVariable) mh.getVariable();
                    if (old != null) {
                        if (old.getDuration() != duration) {
                            JulianEasterVariable easter = new JulianEasterVariable();
                            easter.setDuration(duration);
                            mh.setVariable(easter);
                            mh.status = RegStatus.Accepted;
                            changed = true;
                        } else {
                            changed = !mh.status.isSelected();
                        }
                        mh.status = RegStatus.Accepted;
                    }
                }
            }
        }
        return changed;
    }
//    public static boolean updateTransformation(ModelDescription model, boolean log, boolean lpadjust) {
//        model.setTransformation(log ? TransformationFunction.Log : TransformationFunction.None);
//        if (log && lpadjust) {
//            // remove leap year
//            if (model.getCalendars() != null) {
//                for (Variable var : model.getCalendars()) {
//                    ITsVariable root = var.getRootVariable();
//                    if (root instanceof ILengthOfPeriodVariable) {
//                        var.status = RegStatus.Excluded;
//                        model.setTransformation(((ILengthOfPeriodVariable) root).getType());
//                        return true;
//                    }
//                }
//            }
//        }
//        return false;
//    }
}
