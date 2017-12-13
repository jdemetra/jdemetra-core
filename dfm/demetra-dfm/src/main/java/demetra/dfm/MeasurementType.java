/*
 * Copyright 2013-2014 National Bank of Belgium
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
package demetra.dfm;

/**
 *
 * @author Jean Palate
 */
public enum MeasurementType {

    /**
     * Level: z*(1 0 0 0 0 0 0 0 0 0 0 0)
     */
    M,
    /**
     * Cumulated variations: z*(1 2 3 2 1 0 0 0 0 0 0 0)
     */
    Q,
    /**
     * Cumul: z*(1 1 1 1 1 1 1 1 1 1 1 1)
     */
    YoY;
    
   /**
     * Gets the default measurement for a given type.
     *
     * @param type The type of the measurement
     * @return For type C, returns (1...1) (length=12); for type CD, returns (1
     * 2 3 2 1); for type L, returns (1)
     */
    public static IDfmMeasurement measurement(final MeasurementType type) {
        switch (type) {
            case YoY:
                return CumulMeasurement.MC12;
            case Q:
                return CumulatedVariationsMeasurement.MCD3;
            case M:
                return LevelMeasurement.ML;
            default:
                return null;
        }
    }

    public static MeasurementType getMeasurementType(final IDfmMeasurement m) {
        if (m instanceof CumulMeasurement) {
            return MeasurementType.YoY;
        } else if (m instanceof CumulatedVariationsMeasurement) {
            return MeasurementType.Q;
        } else if (m instanceof LevelMeasurement) {
            return MeasurementType.M;
        } else {
            return null;
        }
    }

     
}
