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
package demetra.modelling;

/**
 *
 * @author palatej
 */
public class ModellingDictionary {
    public static final String
            Y="y",      // original series
            YC="yc",    // interpolated series. Untransformed
            L="l", // linearized series (series without pre-adjustment and regression effects). l=yc-/det. Untransformed
            Y_LIN="y_lin", // linearized series (transformed series without pre-adjustment and regression effects). Transformed
            CAL="cal",     // all calendar effects (including pre-adjustments). cal=tde+*mhe. Untransformed       
            YCAL="ycal",  // series corrected for calendar effects: y_cal = yc-/cal. Untransformed
            DET="det",   // all deterministic effects (including pre-adjustment). Untransformed
            TDE="tde",  // trading days effects (including leap year/length of period, includeing pre-adjustments). Untransformed
            EE="ee", // Easter effects. Untransformed
            RMDE="rmde", // Ramadan effects. Untransformed
            OMHE="omhe", // Other mothing holidays effects. Untransformed
            MHE="mhe", // All moving holidays effects. mhe=ee+*rmde+*omhe. Untransformed
            OUT="out", // All outliers effects. Untransformed
            REG="reg", // All other regression effects (outside outliers and calendars). Untransformed
            FULL_RES="full_res" // full residuals. L^-1*([log]yc-[log]det)
            ;
}
