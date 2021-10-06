/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import jdplus.sa.diagnostics.CombinedSeasonalityTests;
import jdplus.sa.tests.CombinedSeasonality;
import jdplus.stats.tests.TestsUtility;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author PALATEJ
 */
@ServiceProvider(InformationExtractor.class)
public class CombinedSeasonalityTestsExtractor extends InformationMapping<CombinedSeasonalityTests> {

    @Override
    public Class getSourceClass() {
        return CombinedSeasonalityTests.class;
    }

    public static final String 
            SEAS_LIN_COMBINED = "seas-lin-combined",
            SEAS_LIN_EVOLUTIVE = "seas-lin-evolutive",
            SEAS_LIN_STABLE = "seas-lin-stable",
            SEAS_SI_COMBINED = "seas-si-combined",
            SEAS_SI_COMBINED3 = "seas-si-combined3",
            SEAS_SI_EVOLUTIVE = "seas-si-evolutive",
            SEAS_SI_STABLE = "seas-si-stable",
            SEAS_RES_COMBINED = "seas-res-combined",
            SEAS_RES_COMBINED3 = "seas-res-combined3",
            SEAS_RES_EVOLUTIVE = "seas-res-evolutive",
            SEAS_RES_STABLE = "seas-res-stable",
            SEAS_SA_COMBINED = "seas-sa-combined",
            SEAS_SA_COMBINED3 = "seas-sa-combined3",
            SEAS_SA_STABLE = "seas-sa-stable",
            SEAS_SA_EVOLUTIVE = "seas-sa-evolutive",
            SEAS_I_COMBINED = "seas-i-combined",
            SEAS_I_COMBINED3 = "seas-i-combined3",
            SEAS_I_STABLE = "seas-i-stable",
            SEAS_I_EVOLUTIVE = "seas-i-evolutive";

    public CombinedSeasonalityTestsExtractor() {

        set(SEAS_LIN_COMBINED, String.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SEAS_LIN_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(satest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_LIN_STABLE, Double.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
               return TestsUtility.ofAnova(satest.getStableSeasonalityTest()).getPvalue();
             }
        });
 
        set(SEAS_SI_COMBINED, String.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });

        set(SEAS_SI_COMBINED3, String.class, source -> {
            CombinedSeasonality sitest = source.siTest(true);
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });
        set(SEAS_SI_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_SI_STABLE, Double.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_RES_COMBINED, String.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SEAS_RES_COMBINED3, String.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(true);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SEAS_RES_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_RES_STABLE, Double.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_I_COMBINED, String.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SEAS_I_COMBINED3, String.class, source -> {
            CombinedSeasonality itest = source.irrTest(true);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SEAS_I_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_I_STABLE, Double.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_SA_COMBINED, String.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SEAS_SA_COMBINED3, String.class, source -> {
            CombinedSeasonality satest = source.saTest(true);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SEAS_SA_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(satest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SEAS_SA_STABLE, Double.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
               return TestsUtility.ofAnova(satest.getStableSeasonalityTest()).getPvalue();
             }
        });

    }
}

