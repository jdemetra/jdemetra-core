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


package jdplus.math.functions;

import nbbrd.design.Development;


/**
 * Interface implements by classes that can provide an evaluation of their
 * objects under the form of a real value.
 * The goal of this interface is to provide, in collaboration 
 * with the IParametric interface flexible implementations 
 * of IRealFunction. More specifically, suppose that we have a class A that 
 * implements IParametric and a class B that implements IEvaluation and 
 * such that its objects contain references to objects of the class A.
 * Then, a real function can trivially be constructed (see the class 
 * "FunctionInstance" for the code). Using that approach, the code for the 
 * evaluation is separated from the code of the parameterization and we can 
 * provide several "evaluation" functions for the same parametric object.
 * 
 * @author Jean Palate
 */
@Development(status = Development.Status.Alpha)
public interface IEvaluation<T> {
    /**
     * Gets a real value that corresponds to the evaluation or "valorization"
     * of an object.
     * @return A double value. May be Double.Nan
     */
    double getValue();
    
    boolean evaluate(T obj);
}
