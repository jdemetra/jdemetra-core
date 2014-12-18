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
package ec.tss.sa.revisions;

import ec.tss.Ts;
import ec.tss.TsCollection;
import ec.tss.TsCollectionInformation;
import ec.tss.TsFactory;
import ec.tss.TsInformation;
import ec.tss.TsInformationType;
import ec.tss.documents.ActiveDocument;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Document containing input data, specifications and processing them to provide
 * results for the revision analysis.
 *
 * @author Mats Maggi
 */
public class RevisionAnalysisDocument extends ActiveDocument<RevisionAnalysisSpec, TsCollection, CompositeResults> {

    private final RevisionAnalysisProcessor processor;

    public RevisionAnalysisDocument() {
        super(RevisionAnalysisProcessor.DESCRIPTOR.name);
        processor = RevisionAnalysisProcessor.instance;
        setSpecification(new RevisionAnalysisSpec());
    }

    public RevisionAnalysisDocument(ProcessingContext context) {
        super(RevisionAnalysisProcessor.DESCRIPTOR.name, context);
        processor = RevisionAnalysisProcessor.instance;
        setSpecification(new RevisionAnalysisSpec());
    }

    /**
     * Sets the TsCollection used as input data
     * @param coll TsCollection input data
     */
    public void setTsCollection(TsCollection coll) {
        super.setInput(coll);
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = super.write(verbose);
        info.add(ALGORITHM, processor.getInformation());
        TsCollectionInformation tsCollInfo = new TsCollectionInformation(getInput(), TsInformationType.All);
        info.add(INPUT, tsCollInfo);
        info.set(SPEC, getSpecification().write(verbose));
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (!super.read(info)) {
            return false;
        }

        AlgorithmDescriptor algorithm = info.get(ALGORITHM, AlgorithmDescriptor.class);
        if (algorithm == null || !processor.getInformation().isCompatible(algorithm)) {
            return false;
        }

        TsCollectionInformation inputs = info.get(INPUT, TsCollectionInformation.class);
        if (inputs != null) {
            List<Ts> allTs = new ArrayList<>();
            List<TsInformation> allTsInfo = inputs.items;
            for (int i = 0; i < allTsInfo.size(); i++) {
                TsInformation tsinfo = allTsInfo.get(i);
                if (tsinfo.data != null) {
                    allTs.add(TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, tsinfo.metaData, tsinfo.data));
                } else {
                    allTs.add(TsFactory.instance.createTs(tsinfo.name, tsinfo.moniker, TsInformationType.None));
                }
            }

            super.setInput(TsFactory.instance.createTsCollection(inputs.name, inputs.moniker, inputs.metaData, allTs), true);
        }

        InformationSet ispec = info.getSubSet(SPEC);
        if (ispec != null) {
            RevisionAnalysisSpec spec = new RevisionAnalysisSpec();
            if (spec.read(ispec)) {
                setSpecification(spec, true);
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    @Override
    protected CompositeResults recalc(RevisionAnalysisSpec spec, TsCollection input) {
        return processor.generateProcessing(spec, null).process(input);
    }

    @Override
    public String getDescription() {
        return processor.getInformation().name;
    }

}
