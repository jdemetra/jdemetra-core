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

import ec.benchmarking.simplets.Calendarization.PeriodObs;
import ec.tss.disaggregation.processors.CalendarizationProcessor;
import ec.tss.documents.ActiveDocument;
import ec.tss.documents.DocumentManager;
import ec.tstoolkit.algorithm.AlgorithmDescriptor;
import ec.tstoolkit.algorithm.ProcessingContext;
import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;
import ec.tstoolkit.timeseries.Day;
import ec.tstoolkit.utilities.StringFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Workspace document containing all data, specification and results of the calendarization
 * @author Mats Maggi
 */
public class CalendarizationDocument extends ActiveDocument<CalendarizationSpecification, List<PeriodObs>, CalendarizationResults> implements Cloneable {

    private final CalendarizationProcessor factory_;
    public static final String START = "start", END = "end", VALUE = "value";

    public CalendarizationDocument() {
        super(CalendarizationProcessor.DESCRIPTOR.name);
        factory_ = CalendarizationProcessor.instance;
        setSpecification(new CalendarizationSpecification());
    }

    public CalendarizationDocument(ProcessingContext context) {
        super(CalendarizationProcessor.DESCRIPTOR.name, context);
        factory_ = CalendarizationProcessor.instance;
        setSpecification(new CalendarizationSpecification());
    }
    
    @Override
    public String getDescription() {
        return factory_.getInformation().name;
    }
    
    protected void updateLinks() {
        DocumentManager.instance.update(this);
    }

    @Override
    protected CalendarizationResults recalc(CalendarizationSpecification spec, List<PeriodObs> input) {
        return factory_.generateProcessing(spec, null).process(input);
    }

    @Override
    public void setInput(List<PeriodObs> input) {
        if (input == null || (getInput() != null && getInput().equals(input))) {
            return;
        }
        super.setInput(input);
    }

    @Override
    public InformationSet write(boolean verbose) {
        InformationSet info = super.write(verbose);
        info.add(ALGORITHM, factory_.getInformation());
        List<PeriodObs> obs = getInput();
        InformationSet inputs = info.subSet(INPUT);
        for (int i = 0; i < obs.size(); i++) {
            InformationSet observation = inputs.subSet("Obs(" + i + ")");
            observation.add(START, StringFormatter.convert(obs.get(i).start));
            observation.add(END, StringFormatter.convert(obs.get(i).end));
            observation.add(VALUE, obs.get(i).value);
        }

        info.set(SPEC, this.getSpecification().write(verbose));
        return info;
    }

    @Override
    public boolean read(InformationSet info) {
        if (!super.read(info)) {
            return false;
        }
        
        AlgorithmDescriptor algorithm = info.get(ALGORITHM, AlgorithmDescriptor.class);
        if (algorithm == null || !factory_.getInformation().isCompatible(algorithm)) {
            return false;
        }

        InformationSet input = info.getSubSet(INPUT);
        if (input != null) {
            List<Information<InformationSet>> sets = input.select(InformationSet.class);
            if (sets != null) {
                List<PeriodObs> obs = new ArrayList<>();
                for (Information<InformationSet> s : sets) {
                    Day start = StringFormatter.convertDay(s.value.get(START, String.class));
                    Day end = StringFormatter.convertDay(s.value.get(END, String.class));
                    double value = s.value.get(VALUE, Double.class);

                    obs.add(new PeriodObs(start, end, value));
                }
                super.setInput(obs, true);
            }
        }

        InformationSet ispec = info.getSubSet(SPEC);
        if (ispec != null) {
            CalendarizationSpecification spec = new CalendarizationSpecification();
            if (spec.read(ispec)) {
                setSpecification(spec, true);
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
}
