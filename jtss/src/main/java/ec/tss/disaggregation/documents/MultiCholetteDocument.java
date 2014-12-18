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
package ec.tss.disaggregation.documents;

import ec.tss.disaggregation.processors.MultiCholetteProcessor;
import ec.tss.documents.ActiveDocument;
import ec.tss.documents.DocumentManager;
import ec.tss.documents.MultiTsDocument;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.regression.TsVariables;
import ec.tstoolkit.utilities.DefaultNameValidator;

/**
 *
 * @author Jean Palate
 */
public class MultiCholetteDocument extends ActiveDocument<MultiCholetteSpecification, TsVariables, MultiBenchmarkingResults> implements Cloneable {

    private final MultiCholetteProcessor factory_;

    public MultiCholetteDocument() {
        super(MultiCholetteProcessor.DESCRIPTOR.name);
        factory_ = MultiCholetteProcessor.instance;
        setSpecification(new MultiCholetteSpecification());
    }

    public MultiCholetteDocument(ProcessingContext context) {
        super(MultiCholetteProcessor.DESCRIPTOR.name, context);
        factory_ = MultiCholetteProcessor.instance;
        setSpecification(new MultiCholetteSpecification());
    }
    
    public void setVariables(TsVariables vars) {
        super.setInput(vars);
    }

    @Override
    public MultiCholetteDocument clone() {
        MultiCholetteDocument cl = (MultiCholetteDocument) super.clone();
        return cl;
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = super.write(verbose);
        info.set(INPUT, this.getInput().write(verbose));
        info.set(SPEC, this.getSpecification().write(verbose));
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (!super.read(info)) {
            return false;
        }
        InformationSet sinfo = info.getSubSet(SPEC);
        if (sinfo == null) {
            return false;
        }
        MultiCholetteSpecification spec = new MultiCholetteSpecification();
        if (!spec.read(sinfo)) {
            return false;
        }
        this.setSpecification(spec);
        InformationSet iinfo = info.getSubSet(INPUT);
        if (iinfo == null) {
            return false;
        }
        TsVariables var = new TsVariables("s", new DefaultNameValidator("+-*=.;"));
        if (!var.read(iinfo)) {
            return false;
        }
        super.setInput(var, true);

        return true;
    }

    @Override
    protected MultiBenchmarkingResults recalc(MultiCholetteSpecification spec, TsVariables input) {
        return factory_.generateProcessing(spec, null).process(input);
    }

    @Override
    public String getDescription() {
        return factory_.getInformation().name;
    }
}
