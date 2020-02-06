/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jdplus.regsarima.regular.diagnostics;


/**
 *
 * @author Jean Palate
 */
@lombok.Builder
@lombok.Value
public class ResidualsDiagnosticsConfiguration{
    public static final double NBAD = .01, NUNC = .1,
        TDSEV = .001, TDBAD = .01, TDUNC = .1,
        SSEV = .001, SBAD = .01, SUNC = .1;

//    @lombok.Builder.Default
//    private double normalityBad = NBAD;
//    @lombok.Builder.Default
//    private double normalityUncertain = NUNC;
//    @lombok.Builder.Default
//    private double specTDSevere = TDSEV;
//    @lombok.Builder.Default
//    private double specTDBad = TDBAD;
//    @lombok.Builder.Default
//    private double specTDUncertain = TDUNC;
//    @lombok.Builder.Default
//    private double specSeasSevere = SSEV;
//    @lombok.Builder.Default
//    private double specSeasBad = SBAD;
//    @lombok.Builder.Default
//    private double specSeasUncertain = SUNC;
    
    private double badThresholdForNormality, uncertainThresholdForNormality;
    private double severeThresholdForTradingDaysPeak, badThresholdForTradingDaysPeak, 
            uncertainThresholdForTradingDaysPeak;
    private double severeThresholdeForSeasonalPeaks, 
            badThresholdeForSeasonalPeaks, 
            uncertainThresholdeForSeasonalPeaks;

    public static ResidualsDiagnosticsConfigurationBuilder builder(){
        ResidualsDiagnosticsConfigurationBuilder builder = new ResidualsDiagnosticsConfigurationBuilder();        
        builder.badThresholdForNormality = NBAD;
        builder.uncertainThresholdForNormality=NUNC;
        builder.severeThresholdForTradingDaysPeak=TDSEV;
        builder.badThresholdForTradingDaysPeak=TDBAD;
        builder.uncertainThresholdForTradingDaysPeak=TDUNC;
        builder.severeThresholdeForSeasonalPeaks=SSEV;
        builder.badThresholdeForSeasonalPeaks=SBAD;
        builder.uncertainThresholdeForSeasonalPeaks=SUNC;
        return builder;
    }
}
