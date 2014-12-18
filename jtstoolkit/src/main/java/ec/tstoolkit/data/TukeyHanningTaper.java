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

package ec.tstoolkit.data;

/**
 * TUKEY-HANNING Taper
 * @author Jean Palate
 */
public class TukeyHanningTaper implements ITaper{

    private double r_;

    public TukeyHanningTaper() {
        r_ = .1;
    }

    public TukeyHanningTaper(double r) {
        if (r < 0 || r > 1) {
            throw new IllegalArgumentException();
        }
        r_ = r;
    }

    @Override
    public void process(double[] x) {
        int l = x.length;
        int len=(int)(l*r_*.5);
        double twopi=2.0 * Math.PI;
        for (int i = 0; i < len; i++) {
            double xtap = (i+.5) / l;
            double xpi=twopi* xtap / r_;
            x[i]*=(1 - Math.cos(xpi)) / 2.0;
        }
        for (int i = x.length-len; i < x.length; i++) {
            double xtap = (i+.5) / l;
            double xpi=twopi* (1-xtap) / r_;
            x[i]*=(1 - Math.cos(xpi)) / 2.0;
        }
    }
}
