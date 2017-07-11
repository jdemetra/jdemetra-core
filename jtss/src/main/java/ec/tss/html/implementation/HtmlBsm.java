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
package ec.tss.html.implementation;

import ec.satoolkit.special.StmEstimation;
import ec.tss.html.AbstractHtmlElement;
import ec.tss.html.HtmlStream;
import ec.tss.html.HtmlStyle;
import ec.tss.html.HtmlTable;
import ec.tss.html.HtmlTableCell;
import ec.tss.html.HtmlTag;
import ec.tss.html.IHtmlElement;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.eco.DiffuseConcentratedLikelihood;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.structural.BasicStructuralModel;
import ec.tstoolkit.structural.ModelSpecification;
import ec.tstoolkit.structural.Component;
import ec.tstoolkit.structural.ComponentUse;
import ec.tstoolkit.structural.SeasonalModel;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.IUserTsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.Formatter;

/**
 *
 * @author Jean Palate
 */
public class HtmlBsm extends AbstractHtmlElement implements IHtmlElement {

    private static boolean write(HtmlStream stream, boolean first, String cmp,
            ComponentUse use) throws IOException {
        if (use == ComponentUse.Unused) {
            return false;
        }
        if (!first) {
            stream.write(" + ");
        }
        if (use == ComponentUse.Free) {
            stream.write(cmp);
        } else {
            stream.write(cmp).write("(fixed)");
        }
        return true;
    }

    private static boolean write(HtmlStream stream, boolean first, String cmp,
            SeasonalModel use) throws IOException {
        if (use == SeasonalModel.Unused) {
            return false;
        }
        if (!first) {
            stream.write(" + ");
        }
        if (use != SeasonalModel.Fixed) {
            stream.write(cmp);
        } else {
            stream.write(cmp).write("(fixed)");
        }
        return true;
    }

    /**
     *
     * @param stream
     * @param spec
     * @throws IOException
     */
    public static void writeSpec(HtmlStream stream, ModelSpecification spec)
            throws IOException {
        stream.write(HtmlTag.HEADER1, h1, "Model");
        stream.newLine();
        boolean first = true;
        first = !write(stream, first, "level", spec.getLevelUse());
        first = !write(stream, first, "slope", spec.getSlopeUse()) && first;
        first = !write(stream, first, "cycle", spec.getCycleUse()) && first;
        first = !write(stream, first, "seasonal", spec.getSeasonalModel())
                && first;
        write(stream, first, "noise", spec.getNoiseUse());
        stream.newLines(2);
    }

    final BasicStructuralModel bsm;
    final StmEstimation rslts;
    final TsVariableList x;

    /**
     *
     * @param stm
     */
    public HtmlBsm(StmEstimation stm) {
        this.rslts = stm;
        this.bsm = stm.getModel();
        this.x = stm.getX();
    }

    /**
     *
     * @param stream
     * @throws IOException
     */
    @Override
    public void write(HtmlStream stream) throws IOException {
        ModelSpecification spec = bsm.getSpecification();
        writeSpec(stream, spec);
        writeLikelihood(stream);
        writeModel(stream);
        writeRegressionModel(stream);
    }

    private void writeModel(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Estimated variance of the components");
        Component[] cmp = bsm.getComponents();
        stream.open(new HtmlTable(0, 300));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Component", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("Variance", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("Q-Ratio", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);

        double sig = rslts.getLikelihood().getSigma();
        String fmt;
        if (sig > 10000) {
            fmt = "%.1f";
        } else if (sig > 100) {
            fmt = "%.2f";
        } else if (sig > 1) {
            fmt = "%.4f";
        } else {
            fmt = "%.6f";
        }

        for (int i = 0; i < cmp.length; ++i) {
            double var = bsm.getVariance(cmp[i]);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(cmp[i].name()));
            stream.write(new HtmlTableCell(new Formatter().format(fmt, sig * var).toString()));
            stream.write(new HtmlTableCell(new Formatter().format("%.4f", var).toString()));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);

        if (bsm.getSpecification().hasCycle()) {
            stream.newLine();
            stream.write(HtmlTag.HEADER3, h3, "Cycle");
            stream.write("Average length (in years): ");
            double len = bsm.getCyclicalPeriod() / bsm.freq;
            stream.write(new Formatter().format("%.1f", len).toString());
            stream.newLine();
            stream.write("Dumping factor: ");
            stream.write(new Formatter().format("%.3f", bsm.getCyclicalDumpingFactor()).toString());
        }
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeLikelihood(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER2, h2, "Diffuse likelihood statistics");
        DiffuseConcentratedLikelihood ll = rslts.getLikelihood();
        stream.write("Number of effective observations = ").write(
                ll.getN() - ll.getD()).newLine();
        int np = bsm.getSpecification().getParametersCount();
        stream.write("Number of estimated parameters = ").write(
                np + 1 + (ll.getB() != null ? ll.getB().length : 0)).newLines(2);
        //stream.write("BIC = ").write(stats.BIC).newLine();
        stream.write("Diffuse likelihood = ").write(ll.getLogLikelihood()).newLine();
        stream.write("\"Uncorrected\" likelihood = ").write(ll.getUncorrectedLogLikelihood()).newLine();
        stream.write("AIC = ").write(ll.AIC(np)).newLine();
        stream.newLines(2);
        stream.write(HtmlTag.LINEBREAK);
    }

    private void writeRegressionModel(HtmlStream stream) throws IOException {
        if (!x.isEmpty()) {
            stream.write(HtmlTag.HEADER2, h2, "Regression model");
            stream.newLine();
            writeRegressionItems(stream, "Trading days", ITradingDaysVariable.class);
            writeRegressionItems(stream, "Leap year", ILengthOfPeriodVariable.class);
            writeRegressionItems(stream, "Moving holidays", IMovingHolidayVariable.class);
            writeRegressionItems(stream, "Outliers", IOutlierVariable.class);
            writeRegressionItems(stream, "Ramps", Ramp.class);
            writeRegressionItems(stream, "Intervention variables", InterventionVariable.class);
            writeRegressionItems(stream, "User variables", IUserTsVariable.class);
            stream.write(HtmlTag.LINEBREAK);
        }
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, String title, Class<V> tclass) throws IOException {
        TsFrequency context = TsFrequency.valueOf(bsm.freq);
        TsVariableSelection<ITsVariable> regs = x.selectCompatible(tclass);
        if (regs.isEmpty()) {
            return;
        }
        T t = new T();
        DiffuseConcentratedLikelihood ll = rslts.getLikelihood();
        int nhp = bsm.getCmpsCount();
        t.setDegreesofFreedom(ll.getDegreesOfFreedom(true, nhp));
        double[] b = ll.getB();
        boolean simple = true;
        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
            if (reg.variable.getDim() > 1) {
                simple = false;
                break;
            }
        }
        if (!simple) {
            for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
                stream.write(HtmlTag.HEADER3, h3, reg.variable.getDescription(context));
                stream.open(new HtmlTable(0, 400));
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell("", 100));
                stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
                stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
                stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
                stream.close(HtmlTag.TABLEROW);
                for (int j = 0; j < reg.variable.getDim(); ++j) {
                    stream.open(HtmlTag.TABLEROW);
                    if (reg.variable.getDim() > 1) {
                        stream.write(new HtmlTableCell(reg.variable.getItemDescription(j, context), 100));
                    } else {
                        stream.write(new HtmlTableCell("", 100));
                    }
                    stream.write(new HtmlTableCell(df4.format(b[j + reg.position]), 100));
                    double tval = ll.getTStat(j + reg.position, true, nhp);
                    stream.write(new HtmlTableCell(formatT(tval), 100));
                    double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                    stream.write(new HtmlTableCell(df4.format(prob), 100));
                    stream.close(HtmlTag.TABLEROW);
                }
                stream.close(HtmlTag.TABLE);
                stream.newLine();
                int nvars = reg.variable.getDim();
                if (nvars > 1) {
                    JointRegressionTest jtest = new JointRegressionTest(.05);
                    boolean ok = jtest.accept(ll, nhp, reg.position, nvars, null);
                    StringBuilder builder = new StringBuilder();
                    builder.append("Joint F-Test = ").append(df2.format(jtest.getTest().getValue()))
                            .append(" (").append(df4.format(jtest.getTest().getPValue())).append(')');

                    if (!ok) {
                        stream.write(builder.toString(), HtmlStyle.Bold, HtmlStyle.Danger);
                    } else {
                        stream.write(builder.toString(), HtmlStyle.Italic);
                    }
                    stream.newLines(2);
                }
            }
        } else {
            stream.write(HtmlTag.HEADER3, h3, title);
            stream.open(new HtmlTable(0, 400));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("", 100));
            stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
            stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
            stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
            stream.close(HtmlTag.TABLEROW);
            for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(reg.variable.getDescription(context), 100));
                stream.write(new HtmlTableCell(df4.format(b[reg.position]), 100));
                double tval = ll.getTStat(reg.position, true, nhp);
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLines(2);
        }
    }
}
