/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demetra.tempdisagg.univariate;

import demetra.data.AggregationType;
import demetra.data.Data;
import demetra.data.DoubleSequence;
import demetra.data.ParameterSpec;
import demetra.ssf.SsfAlgorithm;
import demetra.timeseries.TsData;
import demetra.timeseries.TsPeriod;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jean Palate
 */
public class TemporalDisaggregationProcessorTest {

    public TemporalDisaggregationProcessorTest() {
    }

    @Test
    public void testChowLin() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
        System.out.println("CL");
        System.out.println(rslt2.getDisaggregatedSeries().getValues());
        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
        System.out.println(rslt2.getConcentratedLikelihood().coefficients());
        System.out.println(rslt2.getConcentratedLikelihood().e());
        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testChowLin2() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        System.out.println(y);
        System.out.println(q);
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Average)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
        System.out.println("CL-average");
        System.out.println(rslt2.getDisaggregatedSeries().getValues());
        System.out.println(rslt2.getStdevDisaggregatedSeries().getValues());
        System.out.println(rslt2.getConcentratedLikelihood().coefficients());
        System.out.println(rslt2.getConcentratedLikelihood().e());
        System.out.println(rslt2.getConcentratedLikelihood().logLikelihood());
    }

    @Test
    public void testFernandez() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
//        System.out.println("RW");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testLitterman() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
    }

    @Test
    public void testAr1() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Ar1)
                //                .diffuseRegressors(true)
                .constant(true)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
        System.out.println("ar1");
        System.out.println(rslt1.getDisaggregatedSeries().getValues());
        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testRw() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
//        System.out.println("rw");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testRwAr1() {
        TsData y = TsData.of(TsPeriod.yearly(1977), DoubleSequence.ofInternal(Data.PCRA));
        TsData q = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationSpec spec1 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec1);
        TemporalDisaggregationSpec spec2 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec2);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        assertTrue(rslt1.getStdevDisaggregatedSeries().distance(rslt2.getStdevDisaggregatedSeries()) < 1e-5);
        TemporalDisaggregationSpec spec3 = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.RwAr1)
                .constant(false)
                .maximumLikelihood(true)
                .estimationPrecision(1e-9)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y, new TsData[]{q}, spec3);
        assertTrue(rslt1.getConcentratedLikelihood().coefficients().distance(rslt3.getConcentratedLikelihood().coefficients()) < 1e-6);
        assertTrue(rslt1.getConcentratedLikelihood().unscaledCovariance().diagonal()
                .distance(rslt3.getConcentratedLikelihood().unscaledCovariance().diagonal()) < 1e-6);
//        System.out.println("rwar1");
//        System.out.println(rslt1.getDisaggregatedSeries().getValues());
//        System.out.println(rslt1.getStdevDisaggregatedSeries().getValues());
    }

    @Test
    public void testSum() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Sum)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TsData y1 = TsData.of(TsPeriod.yearly(1976), DoubleSequence.ofInternal(Data.PCRA));
        TsData q1 = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q2 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q3 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR)).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }

    @Test
    public void testLast() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.Last)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Augmented)
                .build();
        TsData y1 = TsData.of(TsPeriod.yearly(1976), DoubleSequence.ofInternal(Data.PCRA));
        TsData q1 = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q2 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q3 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR)).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }

    @Test
    public void testFirst() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .aggregationType(AggregationType.First)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.Diffuse)
                .build();
        TsData y1 = TsData.of(TsPeriod.yearly(1976), DoubleSequence.ofInternal(Data.PCRA));
        TsData q1 = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q2 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q3 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR)).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }
    
    @Test
    public void testUser() {
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .observationPosition(2)
                .residualsModel(TemporalDisaggregationSpec.Model.Rw)
                .constant(false)
                .rescale(true)
                .algorithm(SsfAlgorithm.SqrtDiffuse)
                .build();
        TsData y1 = TsData.of(TsPeriod.yearly(1976), DoubleSequence.ofInternal(Data.PCRA));
        TsData q1 = TsData.of(TsPeriod.quarterly(1977, 1), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt1 = TemporalDisaggregationProcessor.PROCESSOR.process(y1, new TsData[]{q1}, spec);
        assertTrue(rslt1 != null);
        TsData y2 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q2 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR));
        TemporalDisaggregationResults rslt2 = TemporalDisaggregationProcessor.PROCESSOR.process(y2, new TsData[]{q2}, spec);
        assertTrue(rslt2 != null);
        TsData y3 = TsData.of(TsPeriod.yearly(1979), DoubleSequence.ofInternal(Data.PCRA));
        TsData q3 = TsData.of(TsPeriod.quarterly(1977, 3), DoubleSequence.ofInternal(Data.IND_PCR)).drop(0, 30);
        TemporalDisaggregationResults rslt3 = TemporalDisaggregationProcessor.PROCESSOR.process(y3, new TsData[]{q3}, spec);
        assertTrue(rslt3 != null);
    }
    
}
