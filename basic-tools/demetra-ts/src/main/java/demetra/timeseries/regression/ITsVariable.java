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

package demetra.timeseries.regression;

import demetra.data.DataBlock;
import demetra.design.Development;
import demetra.timeseries.TsDomain;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * Basic interface for regression variable. The variable may be a single
 * regression variable or a variables group, which have to be considered
 * together.
 *
 * @author Jean Palate
 * @param <D>
 */
@Development(status = Development.Status.Release)
public interface ITsVariable<D extends TsDomain> {

    static final LocalDateTime EPOCH=LocalDate.ofEpochDay(0).atStartOfDay();

    /**
     * Returns in a buffer the data corresponding to a given domain
     *
     * @param domain The time domain for which the data have to be rendered.
     * @param data The buffers that will contain the data.
     * @since 1.5.2
     */
    void data(D domain, List<DataBlock> data);

    /**
     * Returns the supported time span. When it is defined, the definition
     * domain should have the definition period.
     *
     * @return The supported time span is returned or null if the variable is
     * able to support any time span.
     */
    D getDefinitionDomain();

    /**
     * Returns the supported period.
     *
     * @return The supported period is returned or null if
     * the variable is able to support any period
     */
    Period getDefinitionPeriod();

    /**
     * Description of this variable
     *
     * @param context Domain of definition of the variable. Could be null
     * @return Short description of this variable. Should never be null.
     */
    String getDescription(D context);
    
    /**
     * Dimension (number of actual regression variables) of this variable
     * (group).
     *
     * @return The number of variables provided by this (group of)regression
     * variable(s). 1 in most cases.
     */
    int getDim();

    /**
     * Description of a variable of this variable group.
     *
     * @param idx. The index of the variable. Must belong to [0, getDim()[.
     * @param context Context of the variable. Could be null
     * @return The description of the considered regression variable. When
     * getDim() = 1, getDescription and getItemDescription(0) will often return
     * the same description.
     */
    String getItemDescription(int idx, D context);

    /**
     * Checks that this regression variable may be used for a given domain. The
     * exact meaning of this condition is left to the implementor. Usually, a
     * variable is not significant if it is 0 (or a constant) on the whole
     * domain.
     *
     * @param domain The considered domain. It must be in the definition domain
     * or compatible with the definition frequency when they are defined.
     * @return True if the variable is significant on the given domain, false
     * otherwise
     */
    boolean isSignificant(D domain);
    
    String getName();
    
    ITsVariable<D> rename(String name);

}
