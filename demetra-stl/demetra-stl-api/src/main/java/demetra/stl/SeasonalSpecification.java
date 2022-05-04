/*
 * Copyright 2022 National Bank of Belgium
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
package demetra.stl;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder(toBuilder=true, builderClassName="Builder")
public class SeasonalSpecification {
    
    public SeasonalSpecification(){
       this.period=12;
        this.seasonalSpec=LoessSpecification.defaultSeasonal(11);
        this.lowPassSpec=LoessSpecification.defaultLowPass(period);
    }
    
    private final int period;
    private final LoessSpecification seasonalSpec;
    private final LoessSpecification lowPassSpec;
    
    public SeasonalSpecification(int period, int swindow){
        this.period=period;
        this.seasonalSpec=LoessSpecification.defaultSeasonal(swindow);
        this.lowPassSpec=LoessSpecification.defaultLowPass(period);
    }
    
    public SeasonalSpecification(int period, LoessSpecification sspec, LoessSpecification lspec){
        this.period=period;
        this.seasonalSpec=sspec;
        this.lowPassSpec=lspec;
    }
    
    @Override
    public String toString(){
        return "seas-"+period;
    }
}
