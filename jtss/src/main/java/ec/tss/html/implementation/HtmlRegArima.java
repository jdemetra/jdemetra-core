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

import ec.tss.html.*;
import ec.tstoolkit.Parameter;
import ec.tstoolkit.dstats.T;
import ec.tstoolkit.eco.ConcentratedLikelihood;
import ec.tstoolkit.maths.matrices.Matrix;
import ec.tstoolkit.modelling.DefaultTransformationType;
import ec.tstoolkit.modelling.PreadjustmentVariable;
import ec.tstoolkit.modelling.Variable;
import ec.tstoolkit.modelling.arima.JointRegressionTest;
import ec.tstoolkit.modelling.arima.PreprocessingModel;
import ec.tstoolkit.sarima.SarimaComponent;
import ec.tstoolkit.sarima.SarimaSpecification;
import ec.tstoolkit.timeseries.calendars.LengthOfPeriodType;
import ec.tstoolkit.timeseries.regression.GregorianCalendarVariables;
import ec.tstoolkit.timeseries.regression.ILengthOfPeriodVariable;
import ec.tstoolkit.timeseries.regression.IMovingHolidayVariable;
import ec.tstoolkit.timeseries.regression.IOutlierVariable;
import ec.tstoolkit.timeseries.regression.ITradingDaysVariable;
import ec.tstoolkit.timeseries.regression.ITsVariable;
import ec.tstoolkit.timeseries.regression.IUserTsVariable;
import ec.tstoolkit.timeseries.regression.InterventionVariable;
import ec.tstoolkit.timeseries.regression.MissingValueEstimation;
import ec.tstoolkit.timeseries.regression.Ramp;
import ec.tstoolkit.timeseries.regression.TsVariableList;
import ec.tstoolkit.timeseries.regression.TsVariableSelection;
import ec.tstoolkit.timeseries.simplets.TsData;
import ec.tstoolkit.timeseries.simplets.TsFrequency;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

/**
 *
 * @author Jean Palate
 */
public class HtmlRegArima extends AbstractHtmlElement {

    private final PreprocessingModel model_;
    private final TsVariableList x_;
    private final ConcentratedLikelihood ll_;
    private final int nhp_;
    boolean summary_;
    private boolean ml_;

    public HtmlRegArima(final PreprocessingModel model, boolean summary) {
        model_ = model;
        x_ = model_.description.buildRegressionVariables();
        ll_ = model_.estimation.getLikelihood();
        nhp_ = model_.description.getArimaComponent().getFreeParametersCount();
        summary_ = summary;
    }

    public boolean isML() {
        return ml_;
    }

    public void setML(boolean val) {
        ml_ = val;
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeSummary(stream);
        if (summary_) {
            return;
        }
        writeDetails(stream);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        TsFrequency context = model_.getFrequency();
        stream.write(HtmlTag.HEADER1, h1, "Summary").newLine();
        stream.write("Estimation span: [").write(model_.description.getEstimationDomain().getStart().toString());
        stream.write(" - ").write(model_.description.getEstimationDomain().getLast().toString()).write(']').newLine();
        int nm = model_.estimation.getRegArima().getMissingsCount();
        if (nm > 0) {
            stream.write(Integer.toString(model_.description.getEstimationDomain().getLength())).
                    write(" observations (including missing)").newLine();
        } else {
            stream.write(Integer.toString(model_.description.getEstimationDomain().getLength())).
                    write(" observations").newLine();
        }
        if (model_.description.getTransformation() == DefaultTransformationType.Log) {
            stream.write("Series has been log-transformed").newLine();
        }
        if (model_.description.getLengthOfPeriodType() != LengthOfPeriodType.None) {
            stream.write("Series has been corrected for leap year").newLine();
        }
        int ntd = model_.description.countRegressors(var -> var.isCalendar() && var.status.isSelected());
        int nftd = model_.description.countFixedRegressors(var -> var.isCalendar());
        if (ntd == 0 && nftd == 0) {
            stream.write("No trading days effects").newLine();
        } else {
            if (ntd != 0) {
                stream.write("Trading days effects (").write(Integer.toString(ntd)).write(ntd > 1 ? " variables)" : " variable)").newLine();
            }
            if (nftd != 0) {
                stream.write("Fixed Trading days effects (").write(Integer.toString(nftd)).write(nftd > 1 ? " variables)" : " variable)").newLine();
            }
        }
        List<Variable> ee = model_.description.selectVariables(var -> var.isMovingHoliday() && var.status.isSelected());
        List<PreadjustmentVariable> fee = model_.description.selectPreadjustmentVariables(var -> var.isMovingHoliday());
        if (ee.isEmpty() && fee.isEmpty()) {
            stream.write("No easter effect").newLine();
        } else {
            if (!ee.isEmpty()) {
                stream.write(ee.get(0).getVariable().getDescription(context) + " detected").newLine();
            }
            if (!fee.isEmpty()) {
                stream.write("Fixed " + fee.get(0).getVariable().getDescription(context) + " effect").newLine();
            }
        }
        int no = model_.description.getOutliers().size();
        int npo = model_.description.getPrespecifiedOutliers().size();
        int nfo = model_.description.countFixedRegressors(var -> var.isOutlier());

        if (npo > 1) {
            stream.write(Integer.toString(npo)).write(" pre-specified outliers").newLine();
        } else if (npo == 1) {
            stream.write(Integer.toString(npo)).write(" pre-specified outlier").newLine();
        }
        if (no > 1) {
            stream.write(Integer.toString(no)).write(" detected outliers").newLine();
        } else if (no == 1) {
            stream.write(Integer.toString(no)).write(" detected outlier").newLine();
        }
        if (nfo > 1) {
            stream.write(Integer.toString(nfo)).write(" fixed outliers").newLine();
        } else if (nfo == 1) {
            stream.write(Integer.toString(nfo)).write(" fixed outlier").newLine();
        }
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeDetails(HtmlStream stream) throws IOException {
        writeDetails(stream, true);
    }

    public void writeDetails(HtmlStream stream, boolean outliers) throws IOException {
        // write likelihood
        stream.write(HtmlTag.HEADER1, h1, "Final model");
        stream.newLine();
        stream.write(HtmlTag.HEADER2, h2, "Likelihood statistics");
        stream.write(new HtmlLikelihood(model_.estimation.getStatistics()));
        writeScore(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Arima model");
        writeArima(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, h2, "Regression model");
        writeRegression(stream, outliers);
        stream.write(HtmlTag.LINEBREAK);
    }

    public void writeArima(HtmlStream stream) throws IOException {
        SarimaComponent arima = model_.description.getArimaComponent();
        SarimaSpecification sspec = arima.getSpecification();
        stream.write('[').write(sspec.toString()).write(']').newLines(2);
        if (sspec.getParametersCount() == 0) {
            return;
        }
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        int P = sspec.getP();
        Parameter[] p = arima.getPhi();
        T t = new T();
        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
        for (int j = 0; j < P; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("Phi(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int Q = sspec.getQ();
        p = arima.getTheta();
        for (int j = 0; j < Q; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("Theta(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BP = sspec.getBP();
        p = arima.getBPhi();
        for (int j = 0; j < BP; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("BPhi(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BQ = sspec.getBQ();
        p = arima.getBTheta();
        for (int j = 0; j < BQ; ++j) {
            stream.open(HtmlTag.TABLEROW);
            StringBuilder header = new StringBuilder();
            header.append("BTheta(").append(j + 1).append(')');
            stream.write(new HtmlTableCell(header.toString(), 100));
            double val = p[j].getValue(), stde = p[j].getStde();
            stream.write(new HtmlTableCell(df4.format(val), 100));
            if (stde > 0) {
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
            }
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);

        Matrix pcov = model_.estimation.getParametersCovariance();
        if (pcov != null && pcov.getRowsCount() > 1) {
            int size = pcov.getColumnsCount();
            stream.newLines(2);
            stream.write(HtmlTag.HEADER3, h3, "Correlation of the estimates").newLine();
            stream.open(HtmlTag.TABLE);

            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("", 100));

            for (int i = 0; i < P; ++i) {
                StringBuilder header = new StringBuilder();
                header.append("Phi(").append(i + 1).append(")");
                stream.write(new HtmlTableCell(header.toString(), 100));
            }

            for (int i = 0; i < Q; ++i) {
                StringBuilder header = new StringBuilder();
                header.append("Theta(").append(i + 1).append(")");
                stream.write(new HtmlTableCell(header.toString(), 100));
            }

            for (int i = 0; i < BP; ++i) {
                StringBuilder header = new StringBuilder();
                header.append("BPhi(").append(i + 1).append(")");
                stream.write(new HtmlTableCell(header.toString(), 100));
            }

            for (int i = 0; i < BQ; ++i) {
                StringBuilder header = new StringBuilder();
                header.append("BTheta(").append(i + 1).append(")");
                stream.write(new HtmlTableCell(header.toString(), 100));
            }
            stream.close(HtmlTag.TABLEROW);

            for (int i = 0; i < size; ++i) {
                StringBuilder header = new StringBuilder();
                stream.open(HtmlTag.TABLEROW);
                if (i < P) {
                    header.append("Phi(").append(i + 1);
                } else if (i < P + Q) {
                    header.append("Theta(").append(i - P + 1);
                } else if (i < P + Q + BP) {
                    header.append("BPhi(").append(i - P - Q + 1);
                } else {
                    header.append("BTheta(").append(i - P - Q - BP + 1);
                }
                header.append(")");
                stream.write(new HtmlTableCell(header.toString(), 100));
                for (int j = 0; j < size; ++j) {
                    double vi = pcov.get(i, i), vj = pcov.get(j, j);
                    if (vi != 0 && vj != 0) {
                        double val = pcov.get(i, j) / Math.sqrt(vi * vj);
                        stream.write(new HtmlTableCell(df4.format(val), 100));
                    } else {
                        stream.write(new HtmlTableCell("-", 100));
                    }
                }
                stream.close(HtmlTag.TABLEROW);
            }

            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
    }

    public void writeRegression(HtmlStream stream) throws IOException {
        writeRegression(stream, true);
    }

    public void writeRegression(HtmlStream stream, boolean outliers) throws IOException {
        writeMean(stream);
        TsFrequency context = context();
        writeRegressionItems(stream, context, true, var -> var instanceof ITradingDaysVariable);
        writeRegressionItems(stream, context, false, var -> var instanceof ILengthOfPeriodVariable);
        writeFixedRegressionItems(stream, "Fixed calendar effects", context, var -> var.isCalendar());
        writeRegressionItems(stream, context, false, var -> var instanceof IMovingHolidayVariable);
        writeFixedRegressionItems(stream, "Fixed moving holidays effects", context, var -> var.isMovingHoliday());
        if (outliers) {
            writeOutliers(stream, true, context);
            writeOutliers(stream, false, context);
            writeFixedRegressionItems(stream, "Fixed outliers", context, var -> var.isOutlier());
        }
        writeRegressionItems(stream, "Ramps", true, context, var -> var instanceof Ramp);
        writeRegressionItems(stream, "Intervention variables", true, context, var -> var instanceof InterventionVariable);
        writeRegressionItems(stream, "User variables", true, context, var -> var instanceof IUserTsVariable
                && !(var instanceof InterventionVariable) && !(var instanceof Ramp));
        writeFixedRegressionItems(stream, "Fixed other regression effects", context, var -> var.isUser());
        writeMissing(stream);
    }

    private void writeMean(HtmlStream stream) throws IOException {
        if (!model_.description.isMean()) {
            return;
        }
        if (model_.description.isEstimatedMean()) {
            writeEstimatedMean(stream);
        } else {
            writeFixedMean(stream);
        }
    }

    private void writeEstimatedMean(HtmlStream stream) throws IOException {
        double[] b = ll_.getB();
        stream.write(HtmlTag.HEADER3, h3, "Mean");
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficient", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("mu", 100));
        stream.write(new HtmlTableCell(df4.format(b[0]), 100));
        T t = new T();
        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
        double tval = ll_.getTStat(0, true, nhp_);
        stream.write(new HtmlTableCell(formatT(tval), 100));
        double prob = 1 - t.getProbabilityForInterval(-tval, tval);
        stream.write(new HtmlTableCell(df4.format(prob), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeFixedMean(HtmlStream stream) throws IOException {
        stream.write(HtmlTag.HEADER3, h3, "Fixed mean");
        stream.open(new HtmlTable(0, 200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficient", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("mu", 100));
        stream.write(new HtmlTableCell(df4.format(model_.description.getArimaComponent().getMeanCorrection()), 100));
        stream.close(HtmlTag.TABLEROW);
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private void writeOutliers(HtmlStream stream, boolean prespecified, TsFrequency context) throws IOException {

        String header = prespecified ? "Prespecified outliers" : "Outliers";
        writeRegressionItems(stream, header, true, context, var -> var instanceof IOutlierVariable
                && model_.description.isPrespecified((IOutlierVariable) var) == prespecified);
//        
//        TsVariableSelection<IOutlierVariable> regs = x_.select(IOutlierVariable.class);
//        boolean found = false;
//        for (TsVariableSelection.Item<IOutlierVariable> reg : regs.elements()) {
//            if (model_.description.isPrespecified(reg.variable) == prespecified) {
//                found = true;
//                break;
//            }
//        }
//        if (!found) {
//            return;
//        }
//        T t = new T();
//        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
//        double[] b = ll_.getB();
//        stream.write(HtmlTag.HEADER3, h3, prespecified ? "Prespecified outliers" : "Outliers");
//        stream.open(new HtmlTable(0, 400));
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("", 100));
//        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
//        stream.close(HtmlTag.TABLEROW);
//        int start = model_.description.getRegressionVariablesStartingPosition();
//        for (TsVariableSelection.Item<IOutlierVariable> reg : regs.elements()) {
//            if (model_.description.isPrespecified(reg.variable) == prespecified) {
//                stream.open(HtmlTag.TABLEROW);
//                stream.write(new HtmlTableCell(reg.variable.getDescription(context), 100));
//                stream.write(new HtmlTableCell(df4.format(b[start + reg.position]), 100));
//                double tval = ll_.getTStat(start + reg.position, true, nhp_);
//                stream.write(new HtmlTableCell(formatT(tval), 100));
//                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
//                stream.write(new HtmlTableCell(df4.format(prob), 100));
//                stream.close(HtmlTag.TABLEROW);
//            }
//        }
//        stream.close(HtmlTag.TABLE);
//        stream.newLine();
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, TsFrequency context, boolean jointest, Predicate<ITsVariable> predicate) throws IOException {
        TsVariableSelection<ITsVariable> regs = x_.select(predicate);
        if (regs.isEmpty()) {
            return;
        }
        T t = new T();
        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
        double[] b = ll_.getB();
        int start = model_.description.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
            stream.write(HtmlTag.HEADER3, h3, reg.variable.getDescription(context));
            stream.open(new HtmlTable(0, 400));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("", 100));
            stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
            stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
            stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
            stream.close(HtmlTag.TABLEROW);
            int ndim = reg.variable.getDim();
            for (int j = 0; j < reg.variable.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                if (ndim > 1) {
                    stream.write(new HtmlTableCell(reg.variable.getItemDescription(j, context), 100));
                } else {
                    stream.write(new HtmlTableCell("", 100));
                }
                stream.write(new HtmlTableCell(df4.format(b[start + j + reg.position]), 100));
                double tval = ll_.getTStat(start + j + reg.position, true, nhp_);
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
                stream.close(HtmlTag.TABLEROW);
            }
            if (ndim > 1 && reg.variable instanceof GregorianCalendarVariables) {
                // we compute the derived sunday variable
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell("Sunday (derived)", 100));
                double bd = 0;
                int k0 = start + reg.position, k1 = k0 + ndim;
                for (int k = k0; k < k1; ++k) {
                    bd -= b[k];
                }
                stream.write(new HtmlTableCell(df4.format(bd), 100));
                double var = ll_.getBVar(true, nhp_).subMatrix(k0, k1, k0, k1).sum();
                double tval = bd / Math.sqrt(var);
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
        int nvars = regs.getVariablesCount();
        if (jointest && regs.getItemsCount() == 1 && nvars > 1) {
            try {
                JointRegressionTest jtest = new JointRegressionTest(.05);
                boolean ok = jtest.accept(ll_, nhp_, start + regs.get(0).position, nvars, null);
                StringBuilder builder = new StringBuilder();
                builder.append("Joint F-Test = ").append(df2.format(jtest.getTest().getValue()))
                        .append(" (").append(df4.format(jtest.getTest().getPValue())).append(')');

                if (!ok) {
                    stream.write(builder.toString(), HtmlStyle.Bold, HtmlStyle.Danger);
                } else {
                    stream.write(builder.toString(), HtmlStyle.Italic);
                }
                stream.newLines(2);
            } catch (Exception ex) {
            }
        }
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, String header, boolean desc, TsFrequency context, Predicate<ITsVariable> predicate) throws IOException {
        TsVariableSelection<ITsVariable> regs = x_.select(predicate);
        if (regs.isEmpty()) {
            return;
        }
        if (header != null) {
            stream.write(HtmlTag.HEADER3, h3, header);
        }
        T t = new T();
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
        double[] b = ll_.getB();
        int start = model_.description.getRegressionVariablesStartingPosition();
        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
            int ndim = reg.variable.getDim();
            for (int j = 0; j < reg.variable.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                if (ndim > 1 || desc) {
                    stream.write(new HtmlTableCell(reg.variable.getItemDescription(j, context), 100));
                } else {
                    stream.write(new HtmlTableCell("", 100));
                }
                stream.write(new HtmlTableCell(df4.format(b[start + j + reg.position]), 100));
                double tval = ll_.getTStat(start + j + reg.position, true, nhp_);
                stream.write(new HtmlTableCell(formatT(tval), 100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob), 100));
                stream.close(HtmlTag.TABLEROW);
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeFixedRegressionItems(HtmlStream stream, String header, TsFrequency context, Predicate<PreadjustmentVariable> predicate) throws IOException {
        List<PreadjustmentVariable> regs = model_.description.selectPreadjustmentVariables(predicate);
        if (regs.isEmpty()) {
            return;
        }
        if (header != null) {
            stream.write(HtmlTag.HEADER3, h3, header);
        }
        stream.open(new HtmlTable(0, 200));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("", 100));
        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);

        for (PreadjustmentVariable reg : regs) {
            ITsVariable cur = reg.getVariable();
            double[] c = reg.getCoefficients();
            for (int j = 0; j < cur.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(cur.getItemDescription(j, context), 100));
                stream.write(new HtmlTableCell(df4.format(c[j]), 100));
                stream.close(HtmlTag.TABLEROW);
            }
        }

        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeFixedRegressionItems(HtmlStream stream, TsFrequency context, Predicate<PreadjustmentVariable> predicate) throws IOException {
        List<PreadjustmentVariable> regs = model_.description.selectPreadjustmentVariables(predicate);
        if (regs.isEmpty()) {
            return;
        }
        for (PreadjustmentVariable reg : regs) {
            ITsVariable cur = reg.getVariable();
            stream.write(HtmlTag.HEADER3, h3, cur.getDescription(context));
            stream.open(new HtmlTable(0, 200));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("", 100));
            stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
            stream.close(HtmlTag.TABLEROW);

            double[] c = reg.getCoefficients();
            for (int j = 0; j < cur.getDim(); ++j) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(cur.getItemDescription(j, context), 100));
                stream.write(new HtmlTableCell(df4.format(c[j]), 100));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }

    }

//    private <V extends ITsVariable> void writeInterventionVariables(HtmlStream stream) throws IOException {
//        TsVariableSelection<InterventionVariable> regs = x_.select(InterventionVariable.class);
//        if (regs.isEmpty()) {
//            return;
//        }
//        T t = new T();
//        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
//        double[] b = ll_.getB();
//        int start = model_.description.getRegressionVariablesStartingPosition();
//        stream.write(HtmlTag.HEADER3, h3, "Intervention variables");
//
//        stream.open(new HtmlTable(0, 400));
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("", 100));
//        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
//        stream.close(HtmlTag.TABLEROW);
//
//        for (TsVariableSelection.Item<InterventionVariable> reg : regs.elements()) {
//            stream.open(HtmlTag.TABLEROW);
//            stream.write(new HtmlTableCell(reg.variable.toString(TsFrequency.valueOf(model_.description.getFrequency())), 100));
//            stream.write(new HtmlTableCell(df4.format(b[start + reg.position]), 100));
//            double tval = ll_.getTStat(start + reg.position, true, nhp_);
//            stream.write(new HtmlTableCell(formatT(tval), 100));
//            double prob = 1 - t.getProbabilityForInterval(-tval, tval);
//            stream.write(new HtmlTableCell(df4.format(prob), 100));
//            stream.close(HtmlTag.TABLEROW);
//        }
//
//        stream.close(HtmlTag.TABLE);
//        stream.newLine();
//    }
//
//    private <V extends ITsVariable> void writeOtherVariables(HtmlStream stream, TsFrequency context) throws IOException {
//        TsVariableSelection<ITsVariable> regs = x_.select(var -> (var instanceof IUserTsVariable) && !(var instanceof Ramp) && !(var instanceof InterventionVariable));
//        if (regs.isEmpty()) {
//            return;
//        }
//        T t = new T();
//        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
//        double[] b = ll_.getB();
//        int start = model_.description.getRegressionVariablesStartingPosition();
//        stream.write(HtmlTag.HEADER3, h3, "User variables");
//
//        stream.open(new HtmlTable(0, 400));
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("", 100));
//        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
//        stream.close(HtmlTag.TABLEROW);
//
//        for (TsVariableSelection.Item<ITsVariable> reg : regs.elements()) {
//            for (int j = 0; j < reg.variable.getDim(); ++j) {
//                stream.open(HtmlTag.TABLEROW);
//                stream.write(new HtmlTableCell(reg.variable.getItemDescription(j, context), 100));
//                stream.write(new HtmlTableCell(df4.format(b[start + reg.position + j]), 100));
//                double tval = ll_.getTStat(start + reg.position + j, true, nhp_);
//                stream.write(new HtmlTableCell(formatT(tval), 100));
//                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
//                stream.write(new HtmlTableCell(df4.format(prob), 100));
//                stream.close(HtmlTag.TABLEROW);
//            }
//        }
//
//        stream.close(HtmlTag.TABLE);
//        stream.newLine();
//    }
//
//    private <V extends ITsVariable> void writeFixedOtherVariables(HtmlStream stream, TsFrequency context) throws IOException {
//        List<PreadjustmentVariable> regs = model_.description.selectPreadjustmentVariables(var
//                -> {
//            ITsVariable tvar = var.getVariable();
//            return (tvar instanceof IUserTsVariable) && !(tvar instanceof Ramp) && !(tvar instanceof InterventionVariable);
//        });
//
//        if (regs.isEmpty()) {
//            return;
//        }
//        stream.write(HtmlTag.HEADER3, h3, "Fixed user variables");
//
//        stream.open(new HtmlTable(0, 200));
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("", 200));
//        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
//        stream.close(HtmlTag.TABLEROW);
//
//        for (PreadjustmentVariable reg : regs) {
//            ITsVariable cur = reg.getVariable();
//            double[] c = reg.getCoefficients();
//            for (int j = 0; j < cur.getDim(); ++j) {
//                stream.open(HtmlTag.TABLEROW);
//                stream.write(new HtmlTableCell(cur.getItemDescription(j, context), 100));
//                stream.write(new HtmlTableCell(df4.format(c[j]), 100));
//                stream.close(HtmlTag.TABLEROW);
//            }
//        }
//
//        stream.close(HtmlTag.TABLE);
//        stream.newLine();
//    }
//
//    private <V extends ITsVariable> void writeRamps(HtmlStream stream, TsFrequency context) throws IOException {
//        TsVariableSelection<Ramp> regs = x_.select(Ramp.class);
//        if (regs.isEmpty()) {
//            return;
//        }
//        T t = new T();
//        t.setDegreesofFreedom(ll_.getDegreesOfFreedom(true, nhp_));
//        double[] b = ll_.getB();
//        int start = model_.description.getRegressionVariablesStartingPosition();
//        stream.write(HtmlTag.HEADER3, h3, "Ramps");
//
//        stream.open(new HtmlTable(0, 400));
//        stream.open(HtmlTag.TABLEROW);
//        stream.write(new HtmlTableCell("", 100));
//        stream.write(new HtmlTableCell("Coefficients", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("T-Stat", 100, HtmlStyle.Bold));
//        stream.write(new HtmlTableCell("P[|T| &gt t]", 100, HtmlStyle.Bold));
//        stream.close(HtmlTag.TABLEROW);
//
//        for (TsVariableSelection.Item<Ramp> reg : regs.elements()) {
//            stream.open(HtmlTag.TABLEROW);
//            stream.write(new HtmlTableCell(reg.variable.getDescription(context), 100));
//            stream.write(new HtmlTableCell(df4.format(b[start + reg.position]), 100));
//            double tval = ll_.getTStat(start + reg.position, true, nhp_);
//            stream.write(new HtmlTableCell(formatT(tval), 100));
//            double prob = 1 - t.getProbabilityForInterval(-tval, tval);
//            stream.write(new HtmlTableCell(df4.format(prob), 100));
//            stream.close(HtmlTag.TABLEROW);
//        }
//
//        stream.close(HtmlTag.TABLE);
//        stream.newLine();
//    }
//
    private void writeMissing(HtmlStream stream) throws IOException {
        MissingValueEstimation[] missings = model_.missings(true);
        if (missings == null) {
            return;
        }
        stream.write(HtmlTag.HEADER3, h3, "Missing values");
        stream.open(new HtmlTable(0, 400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Periods", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("Value", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("Standard error", 100, HtmlStyle.Bold));
        stream.write(new HtmlTableCell("Untransformed value", 100, HtmlStyle.Bold));
        stream.close(HtmlTag.TABLEROW);
        for (int i = 0; i < missings.length; ++i) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(missings[i].getPosition().toString(), 100));
            stream.write(new HtmlTableCell(df4.format(missings[i].getValue()), 100));
            stream.write(new HtmlTableCell(df4.format(missings[i].getStdev()), 100));
            TsData tmp = new TsData(missings[i].getPosition(), new double[]{missings[i].getValue()}, false);
            model_.backTransform(tmp, true, true);
            stream.write(new HtmlTableCell(df4.format(tmp.get(0)), 100));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);

    }

    private TsFrequency context() {
        return model_.description.getEstimationDomain().getFrequency();
    }

    private void writeScore(HtmlStream stream) throws IOException {
        double[] score = model_.info_.deepSearch("score", double[].class);
        if (score == null) {
            return;
        }
        stream.newLine();
        stream.write(HtmlTag.HEADER3, h3, "Scores at the solution");
        for (int i = 0; i < score.length; ++i) {
            stream.write(dg6.format(score[i]));
            stream.write("  ");
        }
    }

}
