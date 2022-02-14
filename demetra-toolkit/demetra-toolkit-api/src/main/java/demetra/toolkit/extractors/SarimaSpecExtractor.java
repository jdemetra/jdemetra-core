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
package demetra.toolkit.extractors;

import demetra.data.Parameter;
import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.arima.SarimaSpec;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class SarimaSpecExtractor extends InformationMapping<SarimaSpec> {

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
            PERIOD = "period";

    public SarimaSpecExtractor() {
        set(P, Integer.class, source -> source.getP());
        set(D, Integer.class, source -> source.getD());
        set(Q, Integer.class, source -> source.getQ());
        set(PERIOD, Integer.class, source -> source.getPeriod());
        set(BP, Integer.class, source -> source.getBp());
        set(BD, Integer.class, source -> source.getBd());
        set(BQ, Integer.class, source -> source.getBq());
        set(PHI, double[].class, source -> Parameter.values(source.getPhi()));
        set(BPHI, double[].class, source -> Parameter.values(source.getBphi()));
        set(THETA, double[].class, source -> Parameter.values(source.getTheta()));
        set(BTHETA, double[].class, source -> Parameter.values(source.getBtheta()));
        setArray(PHI, 1, 4, Double.class, (source, i) -> {
            Parameter[] p = source.getPhi();
            if (i > p.length) {
                return null;
            } else {
                return p[i-1].getValue();
            }
        });
        setArray(BPHI, 1, 2, Double.class, (source, i) -> {
            Parameter[] p = source.getBphi();
            if (i > p.length) {
                return null;
            } else {
                return p[i-1].getValue();
            }
        });
        setArray(THETA, 1, 4, Double.class, (source, i) -> {
            Parameter[] p = source.getTheta();
            if (i > p.length) {
                return null;
            } else {
                return p[i-1].getValue();
            }
        });
        setArray(BTHETA, 1, 2, Double.class, (source, i) -> {
            Parameter[] p = source.getBtheta();
            if (i > p.length) {
                return null;
            } else {
                return p[i-1].getValue();
            }
        });
    }

    @Override
    public Class<SarimaSpec> getSourceClass() {
        return SarimaSpec.class;
    }

}
