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

import ec.tstoolkit.design.AlgorithmDefinition;

/**
 *
 * @author Jean Palate
 */
@AlgorithmDefinition
public interface IOutliersDetectionModule extends IPreprocessingModule {

    boolean reduceSelectivity();

    void setSelectivity(int level);

    int getSelectivity();

    public static interface ICriticalValueComputer {

        double compute(int len);

        public static ICriticalValueComputer defaultComputer() {
            return len -> {
                double cv;
                if (len <= 50) {
                    cv = 3.3;
                } else if (len < 450) {
                    cv = 3.3 + 0.0025 * (len - 50);
                } else {
                    cv = 4.3;
                }
                return cv;
            };
        }
    }
}
