/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.satoolkit.special;

import ec.tstoolkit.algorithm.IProcResults;
import ec.tstoolkit.algorithm.ProcessingInformation;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.information.InformationMapper;
import ec.tstoolkit.maths.realfunctions.IFunction;
import ec.tstoolkit.maths.realfunctions.IFunctionInstance;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.BsmMonitor;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsDomain;
import ec.tstoolkit.ucarima.UcarimaModel;
import ec.tstoolkit.ucarima.WienerKolmogorovEstimators;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
public class StmEstimation implements IProcResults{
    
    private final BsmMonitor monitor_;
    private final TsData y_;
    private final TsVariableList x_;

    public StmEstimation(TsData y, TsVariableList x, BsmMonitor monitor) {
        monitor_ = monitor;
        y_=y;
        x_=x;
    }

    @Override
    public boolean contains(String id) {
        synchronized (mapper) {
            return mapper.contains(id);
        }
    }

    @Override
    public List<ProcessingInformation> getProcessingInformation() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Map<String, Class> getDictionary() {
        // TODO
        LinkedHashMap<String, Class> map = new LinkedHashMap<>();
        mapper.fillDictionary(null, map);
        return map;
    }

    @Override
    public <T> T getData(String id, Class<T> tclass) {
        synchronized (mapper) {
            
                return (T) mapper.getData(this, id, tclass);
        }
    }


    public BasicStructuralModel getModel() {
        return monitor_.getResult();
    }
    
    public TsData getY(){
        return y_;
    }

    public TsVariableList getX(){
        return x_;
    }

    public TsData getResiduals() {
        double[] res = monitor_.getLikelihood().getResiduals();
        TsDomain edom=y_.getDomain();
        return new TsData(edom.getStart().plus(edom.getLength() - res.length), res, false);
    }

    public DiffuseConcentratedLikelihood getLikelihood() {
        return monitor_.getLikelihood();
    }

    public IFunction likelihoodFunction() {
        return monitor_.likelihoodFunction();
    }

    public IFunctionInstance maxLikelihoodFunction() {
        return monitor_.maxLikelihoodFunction();
    }


    // MAPPERS

    public static <T> void addMapping(String name, InformationMapper.Mapper<StmEstimation, T> mapping) {
        synchronized (mapper) {
            mapper.add(name, mapping);
        }
    }

    private static final InformationMapper<StmEstimation> mapper = new InformationMapper<>();

    static {
        mapper.add("residuals", new InformationMapper.Mapper<StmEstimation, TsData>(TsData.class) {

            @Override
            public TsData retrieve(StmEstimation source) {
                return source.getResiduals();
            }
        });
    }
}

