/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.tramoseats;

import demetra.sa.SaDictionaries;
import demetra.seats.SeatsDictionaries;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.AtomicDictionary;
import demetra.toolkit.dictionaries.ComplexDictionary;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.PrefixedDictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class TramoSeatsDictionaries {

    // Decomposition
    public final String SEATS = SaDictionaries.DECOMPOSITION;


    // finals
    public final String FINAL = "";
     
    public final Dictionary TRAMOSEATSDICTIONARY=ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegArimaDictionaries.REGSARIMA))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.REGEFFECTS))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION_F))
            .dictionary(new PrefixedDictionary(SEATS, SeatsDictionaries.LINDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SEATS, SaDictionaries.CMPDECOMPOSITION))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.COMBINEDSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICTRADINGDAYS))
            .build();

     
}
