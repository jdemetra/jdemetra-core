/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.regsarima;

import demetra.arima.SarimaSpecification;
import jdplus.regarima.IRegArimaProcessor;
import jdplus.regarima.RegArimaEstimation;
import jdplus.regarima.RegArimaModel;
import jdplus.regarima.internal.ConcentratedLikelihoodComputer;
import jdplus.regsarima.internal.HannanRissanenInitializer;
import jdplus.sarima.SarimaModel;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
public class FastSarimaProcessor implements IRegArimaProcessor<SarimaModel> {

    private final IRegArimaProcessor<SarimaModel> fallBack;

    public FastSarimaProcessor(final IRegArimaProcessor<SarimaModel> fallBack) {
        this.fallBack = fallBack;
    }

    @Override
    public RegArimaEstimation<SarimaModel> optimize(RegArimaModel<SarimaModel> regs) {
        RegArimaEstimation<SarimaModel> fast = fastProcess(regs);
        if (fast == null)
            return fallBack.optimize(regs);
        else
            return fast;
    }

    @Override
    public RegArimaEstimation<SarimaModel> process(RegArimaModel<SarimaModel> regs) {
        RegArimaEstimation<SarimaModel> fast = fastProcess(regs);
        if (fast == null)
            return fallBack.process(regs);
        else
            return fast;
    }

    private RegArimaEstimation<SarimaModel> fastProcess(RegArimaModel<SarimaModel> regs) {
        SarimaSpecification curSpec = regs.arima().specification();
        if (curSpec.getParametersCount() == 0) {
            return new RegArimaEstimation<>(regs,
                    ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(regs), 0);
        }
        HannanRissanenInitializer initializer = HannanRissanenInitializer.builder().build();
        SarimaModel stm = initializer.initialize(regs.differencedModel());
        if (! stm.isStable(true))
            return null;
        SarimaModel m = SarimaModel.builder(curSpec)
                .parameters(stm.parameters())
                .build();
        RegArimaModel<SarimaModel> nmodel = RegArimaModel.of(regs, m);

        return new RegArimaEstimation(nmodel, ConcentratedLikelihoodComputer.DEFAULT_COMPUTER.compute(nmodel),
                curSpec.getParametersCount());
                
    }

}
