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
package ec.tss.sa;

import ec.satoolkit.ISaSpecification;
import ec.tss.sa.documents.SaDocument;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.IProcessingFactory;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import java.util.List;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

/**
 *
 * @author Jean Palate
 */
@ServiceDefinition(quantifier = Quantifier.MULTIPLE)
public interface ISaProcessingFactory<S extends ISaSpecification> extends
        IProcessingFactory<S, TsData, CompositeResults> {

    ISaSpecification createSpecification(InformationSet info);
        /// <summary>
    /// Creates the method descriptor, using the given estimation policy and the last estimation
    /// </summary>
    /// <param name="info"></param>
    /// <param name="policy"></param>
    /// <param name="frozenPeriod"></param>
    /// <returns></returns>

    /**
     *
     * @param doc
     * @param frozenPeriod
     * @param policy
     * @param nospan
     * @return
     */
        ISaSpecification createSpecification(SaItem doc, TsDomain frozenPeriod, EstimationPolicyType policy, boolean nospan);

        /// <summary>
    /// Updates the descriptor with a new estimation
    /// </summary>
    /// <param name="info"></param>
    /// <param name="rslts"></param>
    /// <returns></returns>
    boolean updatePointSpecification(SaItem item);

        /// <summary>
    /// Updates the descriptor with a new estimation
    /// </summary>
    /// <param name="info"></param>
    /// <param name="rslts"></param>
    /// <returns></returns>
    //boolean updateSummary(SaItem item);
    SaDocument<?> createDocument();

    List<ISaSpecification> defaultSpecifications();
}
