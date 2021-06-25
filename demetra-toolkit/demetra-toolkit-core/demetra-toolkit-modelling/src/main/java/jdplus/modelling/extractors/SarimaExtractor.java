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
package jdplus.modelling.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.sarima.SarimaModel;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(InformationExtractor.class)
public class SarimaExtractor extends InformationMapping<SarimaModel> {

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
            PERIOD = "period";

    public SarimaExtractor() {
        set(P, Integer.class, source -> source.getP());
        set(D, Integer.class, source -> source.getD());
        set(Q, Integer.class, source -> source.getQ());
        set(BP, Integer.class, source -> source.getBp());
        set(BQ, Integer.class, source -> source.getBq());
        set(BD, Integer.class, source -> source.getBd());
        set(PARAMETERS, double[].class, source -> parameters(source, true));
        set(PARAMETERS2, double[].class, source -> parameters(source, false));
        set(PHI, double[].class, source -> source.getPhi());
        set(BPHI, double[].class, source -> source.getBphi());
        set(THETA, double[].class, source -> source.getTheta());
        set(BTHETA, double[].class, source -> source.getBtheta());
    }

    private double[] parameters(SarimaModel source, boolean trueSigns) {
        double[] phi = source.getPhi(), bphi = source.getBphi(), th = source.getTheta(), bth = source.getBtheta();
        int n = phi.length + bphi.length + th.length + bth.length;
        int pos = 0;
        double[] all = new double[n];
        for (int i = 0; i < phi.length; ++i) {
            if (trueSigns) {
                all[pos++] = phi[i];
            } else {
                all[pos++] = -phi[i];
            }
        }
        for (int i = 0; i < bphi.length; ++i) {
            if (trueSigns) {
                all[pos++] = bphi[i];
            } else {
                all[pos++] = -bphi[i];
            }
        }
        for (int i = 0; i < th.length; ++i) {
            all[pos++] = th[i];
        }
        for (int i = 0; i < bth.length; ++i) {
            all[pos++] = bth[i];
        }
        return all;

    }

    @Override
    public Class getSourceClass() {
        return SarimaModel.class;
    }

}
