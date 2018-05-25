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
package demetra.regarima.regular;

/**
 * Estimates the stationary part of the arima model provided in the context.
 * On entry, the estimation should be available
 * The context should contain the new specification on exit. The fully estimated model
 * is not necessary provided
 * @author Jean Palate
 */
public interface IDifferencingModule {
    
    ProcessingResult process(RegArimaModelling context);
    
}
