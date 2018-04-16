/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import ec.satoolkit.DefaultSeriesDecomposition;
import ec.satoolkit.x11.X11Kernel;
import ec.tstoolkit.algorithm.CompositeResults;
import ec.tstoolkit.modelling.ComponentInformation;
import ec.tstoolkit.modelling.ComponentType;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Christiane Hofer
 * this class shouldn't be checkt in, and shouldn't be pushed to github
 */
public class WriteOutPutTest {

    /**
     *
     * @param results     composit results from the preprocessing
     * @param tablePreFix e.g. explanation what the Test ist for
     */
    public static void writeAllToOutput(CompositeResults results, String tablePreFix) {
        writeTablesToOutput(results, tablePreFix);
        writeDecompositionToOutput(results, tablePreFix);
    }

    /**
     *
     * @param results     composit results from the preprocessing
     * @param tablePreFix e.g. explanation what the Test ist for
     */
    public static void writeTablesToOutput(CompositeResults results, String tablePreFix) {
        for (String table : X11Kernel.ALL_A) {
            boolean exists = writeTsDataToOutput(results.getData("a-tables." + table, TsData.class), tablePreFix + table);
            if (exists) {
                writeAssertTrueToOutputTables(tablePreFix + table, "a-tables." + table);
            }
        }
        for (String table : X11Kernel.ALL_D) {
            boolean exists = writeTsDataToOutput(results.getData("d-tables." + table, TsData.class), tablePreFix + table);
            if (exists) {
                writeAssertTrueToOutputTables(tablePreFix + table, "d-tables." + table);
            }
        }
    }

    public static void writeDecompositionToOutput(CompositeResults results, String tablePreFix) {
        //series
        DefaultSeriesDecomposition dsd = results.get("final", DefaultSeriesDecomposition.class);
        TsData tsData;
        String tsDataname;
        boolean exists;

        //Series
        tsData = dsd.getSeries(ComponentType.Series, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.Series.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Series.name(), ComponentInformation.Value.name(), tsDataname);
        }
        // Series Forecast
        tsData = dsd.getSeries(ComponentType.Series, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.Series.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Series.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // Trend Series
        tsData = dsd.getSeries(ComponentType.Trend, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.Trend.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Trend.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // Trend Forecast
        tsData = dsd.getSeries(ComponentType.Trend, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.Trend.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Trend.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // Seasonal Series
        tsData = dsd.getSeries(ComponentType.Seasonal, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.Seasonal.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Seasonal.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // Seasonal Forecast
        tsData = dsd.getSeries(ComponentType.Seasonal, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.Seasonal.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Seasonal.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // Irregular Series
        tsData = dsd.getSeries(ComponentType.Irregular, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.Irregular.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Irregular.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // Seasonal Forecast
        tsData = dsd.getSeries(ComponentType.Irregular, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.Irregular.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Irregular.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // CalendarEffect Series
        tsData = dsd.getSeries(ComponentType.CalendarEffect, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.CalendarEffect.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.CalendarEffect.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // CalendarEffect Forecast
        tsData = dsd.getSeries(ComponentType.CalendarEffect, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.CalendarEffect.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.CalendarEffect.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // SeasonallyAdjusted Series
        tsData = dsd.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.SeasonallyAdjusted.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.SeasonallyAdjusted.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // SeasonallyAdjusted Forecast
        tsData = dsd.getSeries(ComponentType.SeasonallyAdjusted, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.SeasonallyAdjusted.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.SeasonallyAdjusted.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

        // Undefined Series
        tsData = dsd.getSeries(ComponentType.Undefined, ComponentInformation.Value);
        tsDataname = tablePreFix + ComponentType.Undefined.name() + ComponentInformation.Value.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Undefined.name(), ComponentInformation.Value.name(), tsDataname);
        }

        // Undefined Forecast
        tsData = dsd.getSeries(ComponentType.Undefined, ComponentInformation.Forecast);
        tsDataname = tablePreFix + ComponentType.Undefined.name() + ComponentInformation.Forecast.name();
        exists = writeTsDataToOutput(tsData, tsDataname);
        if (exists) {
            writeAssertTrueToOutputMainResults(ComponentType.Undefined.name(), ComponentInformation.Forecast.name(), tsDataname);
        }

    }

    /**
     *
     * @param ComponentType
     * @param Componentinformation
     * @param tsName
     * @param resultDataname       Name or get DefaultSeriesDecomposition.class
     */
    private static void writeAssertTrueToOutputMainResults(String strComponentType, String strComponentinformation, String resultDataname) {
        String out = "";
        out = "DefaultSeriesDecomposition dsd" + resultDataname + " = comprest.get(\"final\", DefaultSeriesDecomposition.class);";
        System.out.println(out);

        out = "TsData tsData" + resultDataname + " = dsd" + resultDataname + ".getSeries(ComponentType." + strComponentType + ", ComponentInformation." + strComponentinformation + ");";
        System.out.println(out);

        out = "Assert.assertTrue(\"";
        out = out + resultDataname + "\", CompareTsData.compareTS(";
        out = out + resultDataname + ",";
        out = out + " tsData" + resultDataname;
        out = out + ", 0.000000001));";
        System.out.println(out);
//   Assert.assertTrue("D11a", CompareTsData.compareTS(TsD11a, comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
    }

    private static void writeAssertTrueToOutputTables(String tsName, String resultDataname) {
        String out = "";
        out = out + "Assert.assertTrue(\"";
        out = out + tsName + "\", CompareTsData.compareTS(";
        out = out + tsName;
        out = out + ",comprest.getData(\"";
        out = out + resultDataname;
        out = out + "\", TsData.class), 0.000000001));";
        System.out.println(out);
//   Assert.assertTrue("D11a", CompareTsData.compareTS(TsD11a, comprest.getData("d-tables.d11a", TsData.class), 0.000000001));
    }

    /**
     *
     * @param tsData
     * @param tsName
     *
     * @return
     */
    private static boolean writeTsDataToOutput(TsData tsData, String tsName) {
        if (tsData != null) {
            String out = "double[] a" + tsName;
            out = out + "  = {";
            for (int i = 0; i < tsData.getLength(); i++) {
                if (Double.isNaN(tsData.get(i))) {
                    out = out + "Double.NaN";
                } else {
                    out = out + tsData.get(i);
                }
                if (i < tsData.getLength() - 1) {
                    out = out + ",";
                }
            }
            out = out + "};";
            System.out.println(out);

            out = "TsData " + tsName + " ";
            out = out + "= new TsData(TsFrequency." + tsData.getFrequency();
            out = out + " , ";

            out = out + tsData.getStart().getYear() + " , ";
            out = out + tsData.getStart().getPosition() + " , ";
            out = out + "a" + tsName + " ,false);";
            System.out.println(out);
            return true;
        } else {
            return false;
        }

    }

}
