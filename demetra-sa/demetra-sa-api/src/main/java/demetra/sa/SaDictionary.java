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

package demetra.sa;

import nbbrd.design.Development;

/**
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public final class SaDictionary  {
    
    
    /**
     * Subdivision of a SA processing
     * Pre-processing of the series (usually reg-arima), decomposition of the linearized series, final decomposition,
     * optional benchmarking and diagnostics
     * 
     */
    public final String PREPROCESSING = "preprocessing", DECOMPOSITION = "decomposition", FINAL = "final",
             BENCHMARKING = "benchmarking", DIAGNOSTICS = "diagnostics";
    
    /**
     * Default components: series, trend, seasonal, seasonally adjusted, irregular, si-ratio, undefined 
     */
    public final String Y="y", T = "t", S = "s", SA = "sa", I = "i", SI = "si", U="u";
    
    /**
     * Default suffixes
     * Linearized series are series after transformation and removal of the deterministic components.
     * Components are series after removal of the deterministic components, but not (log-)transformed
     */
    public final String FORECAST="_f", FORECASTERROR="_ef", BACKCAST="_b", BACKCASTERROR="_eb", 
            LINEARIZED="_lin", COMPONENT="_cmp"; 
    
    /**
     * Default prefixes
     * Outliers, other regression effects and complete deterministic effects (=out+reg)
     */
    public final String OUT="out_", REG="reg_", DET="det_";
    
    
///////////////////////////////////////////////////////////////////////////////
    
    /**
     * Decomposition mode of the series
     */
    public static final String MODE = "mode";
   
    public static final String Y_LIN = "y_lin", T_LIN = "t_lin", S_LIN = "s_lin", SA_LIN = "sa_lin", I_LIN = "i_lin", SI_LIN = "si_lin";
    public static final String Y_CMP="y_cmp", T_CMP = "t_cmp", S_CMP = "s_cmp", SA_CMP = "sa_cmp", I_CMP = "i_cmp", SI_CMP = "si_cmp";
    public static final String OUT_I = "out_i", OUT_S = "out_s", OUT_T = "out_t";
    public static final String REG_I = "reg_i", REG_S = "reg_s", REG_T = "reg_t", REG_SA = "reg_sa",
            REG_Y = "reg_y", REG_U="reg_u";
    public static final String DET_I = "det_i", DET_S = "det_s", DET_T = "det_t", DET_SA = "det_sa",
            DET_Y = "det_y", DET_U="det_u";
    
}
