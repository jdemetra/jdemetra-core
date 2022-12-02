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
package demetra.html.modelling;

import demetra.arima.SarimaOrders;
import demetra.data.DoubleSeq;
import demetra.data.DoubleSeqCursor;
import demetra.data.Parameter;
import demetra.html.AbstractHtmlElement;
import demetra.html.HtmlStream;
import demetra.html.HtmlTable;
import demetra.html.HtmlTableCell;
import demetra.html.HtmlTag;
import demetra.html.Bootstrap4;
import jdplus.stats.likelihood.LikelihoodStatistics;
import demetra.math.matrices.Matrix;
import demetra.arima.SarimaSpec;
import demetra.stats.ProbabilityType;
import demetra.timeseries.TsDomain;
import demetra.timeseries.TsPeriod;
import demetra.timeseries.calendars.DayClustering;
import demetra.timeseries.calendars.LengthOfPeriodType;
import demetra.timeseries.regression.GenericTradingDaysVariable;
import demetra.timeseries.regression.HolidaysCorrectedTradingDays;
import demetra.timeseries.regression.IEasterVariable;
import demetra.timeseries.regression.ILengthOfPeriodVariable;
import demetra.timeseries.regression.IOutlier;
import demetra.timeseries.regression.ITradingDaysVariable;
import demetra.timeseries.regression.ITsVariable;
import demetra.timeseries.regression.InterventionVariable;
import demetra.timeseries.regression.MissingValueEstimation;
import demetra.timeseries.regression.ModellingUtility;
import demetra.timeseries.regression.Ramp;
import demetra.timeseries.regression.TrendConstant;
import demetra.timeseries.regression.UserVariable;
import demetra.timeseries.regression.Variable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jdplus.data.DataBlock;
import jdplus.dstats.F;
import jdplus.dstats.T;
import jdplus.math.matrices.FastMatrix;
import jdplus.math.matrices.LowerTriangularMatrix;
import jdplus.math.matrices.QuadraticForm;
import jdplus.math.matrices.SymmetricMatrix;
import jdplus.modelling.GeneralLinearModel;
import jdplus.modelling.regression.RegressionDesc;
import jdplus.regsarima.regular.RegSarimaModel;

/**
 *
 * @author Jean Palate
 */
public class HtmlRegSarima extends AbstractHtmlElement {

    private final RegSarimaModel model;
    boolean summary;

    public HtmlRegSarima(final RegSarimaModel model, boolean summary) {
        this.model = model;
        this.summary = summary;
    }

    private <T extends ITsVariable> int countVariables(Class<T> tclass, boolean fixed) {
        Variable[] variables = model.getDescription().getVariables();
        if (fixed) {
            return Arrays.stream(variables).filter(var -> tclass.isInstance(var.getCore())).mapToInt(var -> var.fixedCoefficientsCount()).sum();
        } else {
            return Arrays.stream(variables).filter(var -> tclass.isInstance(var.getCore())).mapToInt(var -> var.freeCoefficientsCount()).sum();
        }
    }

    @Override
    public void write(HtmlStream stream) throws IOException {
        writeSummary(stream);
        if (summary) {
            return;
        }
        writeDetails(stream);
    }

    private void writeSummary(HtmlStream stream) throws IOException {
        TsDomain edom = model.getEstimation().getDomain();
        stream.write(HtmlTag.HEADER1, "Summary").newLine();
        stream.write("Estimation span: [").write(edom.getStartPeriod().display());
        stream.write(" - ").write(edom.getLastPeriod().display()).write(']').newLine();
        GeneralLinearModel.Description<SarimaSpec> description = model.getDescription();
        Variable[] variables = description.getVariables();
        GeneralLinearModel.Estimation estimation = model.getEstimation();
        MissingValueEstimation[] missing = estimation.getMissing();
        int nm = missing.length;
        if (nm > 0) {
            stream.write(Integer.toString(edom.getLength())).
                    write(" observations (including missing)").newLine();
        } else {
            stream.write(Integer.toString(edom.getLength())).
                    write(" observations").newLine();
        }
        if (description.isLogTransformation()) {
            stream.write("Series has been log-transformed").newLine();
        }
        if (description.getLengthOfPeriodTransformation() == LengthOfPeriodType.LeapYear) {
            stream.write("Series has been corrected for leap year").newLine();
        }else if (description.getLengthOfPeriodTransformation() == LengthOfPeriodType.LengthOfPeriod) {
            stream.write("Series has been corrected for length of period").newLine();
        }
        int ntd = countVariables(ITradingDaysVariable.class, false);
        int nftd = countVariables(ITradingDaysVariable.class, true);
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
        Optional<Variable> ee = Arrays.stream(variables).filter(var -> var.isFree() && var.getCore() instanceof IEasterVariable).findFirst();
        Optional<Variable> fee = Arrays.stream(variables).filter(var -> !var.isFree() && var.getCore() instanceof IEasterVariable).findFirst();
        if (!ee.isPresent() && !fee.isPresent()) {
            stream.write("No easter effect").newLine();
        } else {
            if (ee.isPresent()) {
                stream.write(ee.get().getCore().description(edom) + " detected").newLine();
            }
            if (fee.isPresent()) {
                stream.write("Fixed " + fee.get().getCore().description(edom) + " effect").newLine();
            }
        }

        int no = (int) Arrays.stream(variables).filter(var -> var.getCore() instanceof IOutlier && var.hasAttribute(ModellingUtility.AMI)).count();
        int nfo = (int) Arrays.stream(variables).filter(var -> var.getCore() instanceof IOutlier && !var.isFree()).count();
        int npo = -nfo + (int) Arrays.stream(variables).filter(var -> var.getCore() instanceof IOutlier && !var.hasAttribute(ModellingUtility.AMI)).count();

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
        stream.write(HtmlTag.HEADER1, "Final model");
        stream.newLine();
        stream.write(HtmlTag.HEADER2, "Likelihood statistics");
        stream.write(new HtmlLikelihood(model.getEstimation().getStatistics()));
        writeScore(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, "Arima model");
        writeArima(stream);
        stream.write(HtmlTag.LINEBREAK);
        stream.write(HtmlTag.HEADER2, "Regression model");
        writeRegression(stream, outliers);
        stream.write(HtmlTag.LINEBREAK);
    }

    private String pheader(String name, int pos) {
        StringBuilder header = new StringBuilder();
        header.append(name).append('(').append(pos + 1).append(')');
        return header.toString();
    }

    public void writeArima(HtmlStream stream) throws IOException {
        SarimaSpec arima = model.getDescription().getStochasticComponent();
        LikelihoodStatistics ll = model.getEstimation().getStatistics();
        int nhp = model.freeArimaParametersCount();
        SarimaOrders sspec = arima.orders();
        stream.write('[').write(sspec.toString()).write(']').newLines(2);
        if (sspec.getParametersCount() == 0) {
            return;
        }
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        int P = sspec.getP();
        Parameter[] p = arima.getPhi();
        int nobs = ll.getEffectiveObservationsCount(), nparams = ll.getEstimatedParametersCount();
        DoubleSeqCursor vars = model.getEstimation().getParameters().getCovariance().diagonal().cursor();
        double ndf = nobs - nparams;
        double vcorr = (ndf - nhp) / ndf;
        T t = new T(ndf - nhp);
        List<String> headers = new ArrayList<>();
        for (int j = 0; j < P; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("Phi", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p[j].getValue();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            if (!p[j].isFixed()) {
                double stde = Math.sqrt(vars.getAndNext() * vcorr);
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BP = sspec.getBp();
        p = arima.getBphi();
        for (int j = 0; j < BP; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("BPhi", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p[j].getValue();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            if (!p[j].isFixed()) {
                double stde = Math.sqrt(vars.getAndNext() * vcorr);
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int Q = sspec.getQ();
        p = arima.getTheta();
        for (int j = 0; j < Q; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("Theta", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p[j].getValue();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            if (!p[j].isFixed()) {
                double stde = Math.sqrt(vars.getAndNext() * vcorr);
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }
        int BQ = sspec.getBq();
        p = arima.getBtheta();
        for (int j = 0; j < BQ; ++j) {
            stream.open(HtmlTag.TABLEROW);
            String header = pheader("BTheta", j);
            stream.write(new HtmlTableCell(header).withWidth(100));
            double val = p[j].getValue();
            stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
            if (!p[j].isFixed()) {
                double stde = Math.sqrt(vars.getAndNext() * vcorr);
                headers.add(header);
                double tval = val / stde;
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);
        }

        stream.close(HtmlTag.TABLE);

        Matrix pcov = model.getEstimation().getParameters().getCovariance();
        if (!pcov.isEmpty()) {
            int size = pcov.getColumnsCount();
            stream.newLines(2);
            stream.write(HtmlTag.HEADER3, "Correlation of the estimates").newLine();
            stream.open(HtmlTag.TABLE);

            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("").withWidth(100));

            for (int i = 0; i < size; ++i) {
                stream.write(new HtmlTableCell(headers.get(i)).withWidth(100));
            }
            stream.close(HtmlTag.TABLEROW);

            for (int i = 0; i < size; ++i) {
                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell(headers.get(i)).withWidth(100));
                for (int j = 0; j < size; ++j) {
                    double vi = pcov.get(i, i), vj = pcov.get(j, j);
                    if (vi != 0 && vj != 0) {
                        double val = pcov.get(i, j) / Math.sqrt(vi * vj);
                        stream.write(new HtmlTableCell(df4.format(val)).withWidth(100));
                    } else {
                        stream.write(new HtmlTableCell("-").withWidth(100));
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
        TsDomain edom = model.getEstimation().getDomain();
        writeMean(stream);

        writeFullRegressionItems(stream, edom, true, var -> !var.isPreadjustment() && var.getCore() instanceof ITradingDaysVariable);
        writeFullRegressionItems(stream, edom, true, var -> !var.isPreadjustment() && var.getCore() instanceof ILengthOfPeriodVariable);
        writeFixedRegressionItems(stream, "Fixed trading days", edom, true, var -> !var.isFree() && var.getCore() instanceof ITradingDaysVariable);
        writeFixedRegressionItems(stream, "Fixed leap year", edom, true, var -> !var.isFree() && var.getCore() instanceof ILengthOfPeriodVariable);
        writeRegressionItems(stream, "Easter", edom, true, var -> !var.isPreadjustment() && var.getCore() instanceof IEasterVariable);
        writeFixedRegressionItems(stream, "Fixed Easter", edom, false,var -> var.isPreadjustment() && var.getCore() instanceof IEasterVariable);
        if (outliers) {
            writeOutliers(stream, true, edom);
            writeOutliers(stream, false, edom);
            writeFixedRegressionItems(stream, "Fixed outliers", edom, false, var -> var.isPreadjustment() && var.getCore() instanceof IOutlier);
        }
        writeRegressionItems(stream, "Ramps", edom, false, var -> var.isFree() && var.getCore() instanceof Ramp);
        writeRegressionItems(stream, "Intervention variables", edom, false, var -> var.isFree() && var.getCore() instanceof InterventionVariable);
        writeRegressionItems(stream, "User variables", edom, false, var -> !var.isPreadjustment() && var.test(v -> v instanceof UserVariable));
        writeFixedRegressionItems(stream, "Fixed ramps", edom, false, var -> !var.isFree() && var.getCore() instanceof Ramp);
        writeFixedRegressionItems(stream, "Fixed intervention variables", edom, false, var -> !var.isFree() && var.getCore() instanceof InterventionVariable);
        writeFixedRegressionItems(stream, "Other fixed regression effects", edom, false, var -> !var.isFree() && var.getCore() instanceof UserVariable);
        writeMissing(stream);
    }

    private void writeMean(HtmlStream stream) throws IOException {

        if (!model.isMeanCorrection()) {
            return;
        }
        Variable[] variables = model.getDescription().getVariables();
        Optional<Variable> mean = Arrays.stream(variables).filter(var -> var.getCore() instanceof TrendConstant).findFirst();
        if (!mean.isPresent()) {
            return;
        }
        Variable v = mean.get();
        if (v.isFree()) {
            List<RegressionDesc> regressionItems = model.getDetails().getRegressionItems();
            Optional<RegressionDesc> d = regressionItems.stream().filter(desc -> desc.getCore() instanceof TrendConstant).findFirst();
            if (!d.isPresent()) {
                return;
            }
            RegressionDesc reg = d.get();
            stream.write(HtmlTag.HEADER3, "Mean");
            stream.open(new HtmlTable().withWidth(400));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("").withWidth(100));
            stream.write(new HtmlTableCell("Coefficient").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
            stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
            stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
            stream.close(HtmlTag.TABLEROW);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("mu").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getCoef())).withWidth(100));
            stream.write(new HtmlTableCell(formatT(reg.getTStat())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getPvalue())).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        } else {
            stream.write(HtmlTag.HEADER3, "Fixed mean");
            stream.open(new HtmlTable().withWidth(200));
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("").withWidth(100));
            stream.write(new HtmlTableCell("Coefficient").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
            stream.close(HtmlTag.TABLEROW);
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell("mu").withWidth(100));
            stream.write(new HtmlTableCell(df4.format(v.getCoefficient(0).getValue())).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
    }

    private void writeOutliers(HtmlStream stream, boolean prespecified, TsDomain context) throws IOException {

        String header = prespecified ? "Prespecified outliers" : "Outliers";
        Set<ITsVariable> outliers = Arrays.stream(model.getDescription().getVariables())
                .filter(var -> var.getCore() instanceof IOutlier && var.isFree() && prespecified != var.hasAttribute(ModellingUtility.AMI))
                .map(var -> var.getCore()).collect(Collectors.toSet());
        if (!outliers.isEmpty()) {
            stream.write(HtmlTag.HEADER3, header);
            writeRegressionItems(stream, outliers, context, false);
        }
    }

    private <V extends ITsVariable> void writeFixedRegressionItems(HtmlStream stream, String header, TsDomain context, boolean description, Predicate<Variable> predicate) throws IOException {

        List<Variable> regs = Arrays.stream(model.getDescription().getVariables()).filter(predicate).collect(Collectors.toList());
        if (regs.isEmpty()) {
            return;
        }
        if (header != null) {
            stream.write(HtmlTag.HEADER3, header);
        }

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        for (Variable reg : regs) {
            for (int i = 0; i < reg.dim(); ++i) {
                Parameter[] c = reg.getCoefficients();
                if (c[i].isFixed()) {
                    stream.open(HtmlTag.TABLEROW);
                    stream.write(new HtmlTableCell(display(reg, i, context, description || c.length > 1)).withWidth(100));
                    stream.write(new HtmlTableCell(df4.format(c[i].getValue())).withWidth(100));
                    stream.close(HtmlTag.TABLEROW);
                }
            }
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, String header, TsDomain context, boolean description, Predicate<Variable> predicate) throws IOException {
        Set<ITsVariable> selection = Arrays.stream(model.getDescription().getVariables()).filter(var -> predicate.test(var))
                .map(var -> var.getCore())
                .collect(Collectors.toSet());
        if (!selection.isEmpty() && header != null) {
            stream.write(HtmlTag.HEADER3, header);
            writeRegressionItems(stream, selection, context, description);
        }
    }

    private <V extends ITsVariable> void writeFullRegressionItems(HtmlStream stream, TsDomain context, boolean description, Predicate<Variable> predicate) throws IOException {
        Set<ITsVariable> selection = Arrays.stream(model.getDescription().getVariables()).filter(var -> predicate.test(var))
                .map(var -> var.getCore())
                .collect(Collectors.toSet());
        if (!selection.isEmpty()) {
            for (ITsVariable var : selection) {
                writeRegressionItems(stream, var, context, description);
            }
        }
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, List<RegressionDesc> regs, TsDomain context, boolean description) throws IOException {

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        for (RegressionDesc reg : regs) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(display(reg, context, description)).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getCoef())).withWidth(100));
            stream.write(new HtmlTableCell(formatT(reg.getTStat())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getPvalue())).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLine();
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, Set<ITsVariable> vars, TsDomain context, boolean description) throws IOException {
        List<RegressionDesc> regs = new ArrayList<>();
        for (RegressionDesc reg : model.getDetails().getRegressionItems()) {
            if (vars.contains(reg.getCore())) {
                regs.add(reg);
            }
        }
        writeRegressionItems(stream, regs, context, description);
    }
    
    private static String display(RegressionDesc reg, TsDomain context, boolean description){
        String name = reg.getName();
        if (!description && name != null && ! name.isBlank())
            return name;
        else{
            return reg.getCore().description(reg.getItem(), context);
        }
    }

    private static String display(Variable reg, int idx, TsDomain context, boolean description){
        String name = reg.getName();
        if (!description &&  ! name.isBlank())
            return name;
        else{
            return reg.getCore().description(idx, context);
        }
    }

    private <V extends ITsVariable> void writeRegressionItems(HtmlStream stream, ITsVariable var, TsDomain context, boolean description) throws IOException {

        List<RegressionDesc> regs = model.getDetails().getRegressionItems().stream()
                .filter(desc -> desc.getCore() == var)
                .collect(Collectors.toList());
        if (regs.isEmpty()) {
            return;
        }
        int size = regs.size();
        if (size > 1) {
            stream.write(HtmlTag.HEADER3, var.description(context));
        }

        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("").withWidth(100));
        stream.write(new HtmlTableCell("Coefficients").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("T-Stat").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("P[|T| &gt t]").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        for (RegressionDesc reg : regs) {
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(display(reg, context, description || size > 1)).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getCoef())).withWidth(100));
            stream.write(new HtmlTableCell(formatT(reg.getTStat())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(reg.getPvalue())).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
        }
        if (size > 1 && regs.size() == var.dim() && var instanceof ITradingDaysVariable) {
            GeneralLinearModel.Estimation estimation = model.getEstimation();
            int startpos = regs.get(0).getPosition();
            DoubleSeq coef = estimation.getCoefficients().extract(startpos, size);
            FastMatrix bvar = FastMatrix.of(estimation.getCoefficientsCovariance().extract(startpos, size, startpos, size));
            DataBlock w = weights((ITradingDaysVariable) var);
            if (w != null) {
                double b = -coef.dot(w);
                double v = QuadraticForm.apply(bvar, w);
                double tval = b / Math.sqrt(v);
                T t = new T(estimation.getStatistics().getEffectiveObservationsCount() - estimation.getStatistics().getEstimatedParametersCount());

                stream.open(HtmlTag.TABLEROW);
                stream.write(new HtmlTableCell("sunday (derived)").withWidth(100));
                stream.write(new HtmlTableCell(df4.format(b)).withWidth(100));
                stream.write(new HtmlTableCell(formatT(tval)).withWidth(100));
                double prob = 1 - t.getProbabilityForInterval(-tval, tval);
                stream.write(new HtmlTableCell(df4.format(prob)).withWidth(100));
                stream.close(HtmlTag.TABLEROW);
            }
            stream.close(HtmlTag.TABLE);
            stream.newLine();
            try {
                SymmetricMatrix.lcholesky(bvar);
                DataBlock r = DataBlock.of(coef);
                LowerTriangularMatrix.solveLx(bvar, r);
                double f = r.ssq() / size;
                F fdist = new F(size, estimation.getStatistics().getEffectiveObservationsCount() - estimation.getStatistics().getEstimatedParametersCount());
                StringBuilder builder = new StringBuilder();
                double pval = fdist.getProbability(f, ProbabilityType.Upper);
                builder.append("Joint F-Test = ").append(df2.format(f))
                        .append(" (").append(df4.format(pval)).append(')');
                if (pval > .05) {
                    stream.write(HtmlTag.IMPORTANT_TEXT, builder.toString(), Bootstrap4.TEXT_DANGER);
                } else {
                    stream.write(HtmlTag.EMPHASIZED_TEXT, builder.toString());
                }
                stream.newLines(2);
            } catch (Exception ex) {
            }

        } else {
            stream.close(HtmlTag.TABLE);
            stream.newLine();
        }
    }

    private DataBlock weights(ITradingDaysVariable var) {
        if (var instanceof GenericTradingDaysVariable td) {
            return weights(td.getClustering());
        } else if (var instanceof HolidaysCorrectedTradingDays td) {
            return weights(td.getClustering());
        } else {
            return null;
        }
    }

    private DataBlock weights(DayClustering td) {
        int n = td.getGroupsCount();
        double[] w = new double[n - 1];
        for (int i = 1; i < n; ++i) {
            w[i - 1] = td.getGroupCount(i);
        }
        return DataBlock.of(w);
    }

    private void writeMissing(HtmlStream stream) throws IOException {
        TsDomain edom = model.getEstimation().getDomain();
        MissingValueEstimation[] missings = model.getEstimation().getMissing();
        if (missings == null || missings.length == 0) {
            return;
        }
        double[] missingEstimates = model.missingEstimates();
        stream.write(HtmlTag.HEADER3, "Missing values");
        stream.open(new HtmlTable().withWidth(400));
        stream.open(HtmlTag.TABLEROW);
        stream.write(new HtmlTableCell("Periods").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("Value").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("Standard error").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.write(new HtmlTableCell("Untransformed value").withWidth(100).withClass(Bootstrap4.FONT_WEIGHT_BOLD));
        stream.close(HtmlTag.TABLEROW);
        for (int i = 0; i < missings.length; ++i) {
            TsPeriod period = edom.get(missings[i].getPosition());
            stream.open(HtmlTag.TABLEROW);
            stream.write(new HtmlTableCell(period.display()).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(missings[i].getValue())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(missings[i].getStandardError())).withWidth(100));
            stream.write(new HtmlTableCell(df4.format(missingEstimates[i])).withWidth(100));
            stream.close(HtmlTag.TABLEROW);
        }
        stream.close(HtmlTag.TABLE);
        stream.newLines(2);

    }

    private void writeScore(HtmlStream stream) throws IOException {
        DoubleSeq score = model.getEstimation().getParameters().getScores();
        if (score.isEmpty()) {
            return;
        }
        stream.newLine();
        stream.write(HtmlTag.HEADER3, "Scores at the solution");
        stream.write(DoubleSeq.format(score, df6));
    }

}
