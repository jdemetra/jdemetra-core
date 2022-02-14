/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jdplus.sa.extractors;

import demetra.information.InformationExtractor;
import demetra.information.InformationMapping;
import demetra.sa.SaDictionaries;
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


    public CombinedSeasonalityTestsExtractor() {

        set(SaDictionaries.SEAS_LIN_COMBINED, String.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_LIN_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(satest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_LIN_STABLE, Double.class, source -> {
            CombinedSeasonality satest = source.linearizedTest();
            if (satest == null) {
                return null;
            } else {
               return TestsUtility.ofAnova(satest.getStableSeasonalityTest()).getPvalue();
             }
        });
 
        set(SaDictionaries.SEAS_SI_COMBINED, String.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_SI_COMBINED3, String.class, source -> {
            CombinedSeasonality sitest = source.siTest(true);
            if (sitest == null) {
                return null;
            } else {
                return sitest.getSummary().name();
            }
        });
        set(SaDictionaries.SEAS_SI_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_SI_STABLE, Double.class, source -> {
            CombinedSeasonality sitest = source.siTest(false);
            if (sitest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(sitest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_RES_COMBINED, String.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_RES_COMBINED3, String.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(true);
            if (rtest == null) {
                return null;
            } else {
                return rtest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_RES_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_RES_STABLE, Double.class, source -> {
            CombinedSeasonality rtest = source.residualsTest(false);
            if (rtest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(rtest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_I_COMBINED, String.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_I_COMBINED3, String.class, source -> {
            CombinedSeasonality itest = source.irrTest(true);
            if (itest == null) {
                return null;
            } else {
                return itest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_I_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_I_STABLE, Double.class, source -> {
            CombinedSeasonality itest = source.irrTest(false);
            if (itest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(itest.getStableSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_SA_COMBINED, String.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_SA_COMBINED3, String.class, source -> {
            CombinedSeasonality satest = source.saTest(true);
            if (satest == null) {
                return null;
            } else {
                return satest.getSummary().name();
            }
        });

        set(SaDictionaries.SEAS_SA_EVOLUTIVE, Double.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
                return TestsUtility.ofAnova(satest.getEvolutiveSeasonalityTest()).getPvalue();
            }
        });

        set(SaDictionaries.SEAS_SA_STABLE, Double.class, source -> {
            CombinedSeasonality satest = source.saTest(false);
            if (satest == null) {
                return null;
            } else {
               return TestsUtility.ofAnova(satest.getStableSeasonalityTest()).getPvalue();
             }
        });

    }
}

