/*
 * Copyright 2020 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package demetra.x13;

import demetra.sa.SaDictionaries;
import demetra.timeseries.TsData;
import demetra.toolkit.dictionaries.AtomicDictionary;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;
import demetra.toolkit.dictionaries.ComplexDictionary;
import demetra.toolkit.dictionaries.Dictionary;
import demetra.toolkit.dictionaries.PrefixedDictionary;
import demetra.toolkit.dictionaries.RegArimaDictionaries;
import demetra.x11.X11Dictionaries;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class X13Dictionaries {

    // Preadjustment
    public final String PREADJUST = "preadjustment";
    // Decomposition
    public final String X11 = SaDictionaries.DECOMPOSITION;


    // finals
    public final String FINAL = "";
   
    
    public static final String A_TABLES="a-tables", D_TABLES="d-tables";
    
    public final String A1="a1", A1A="a1a", A1B="a1b", A6="a6", 
            A7="a7", A8="a8", A8T="a8t", A8S="a8s", A8I="a8i",
            A9="a9", A9U="a9u", A9SA="a9sa", A9SER="a9ser";
    public final String[] A_TABLE = new String[]{A1, A1A, A1B, 
        A6, A7, A8, A8T, A8S, A8I, A9, A9U, A9SA, A9SER};

    public final Dictionary ATABLES = AtomicDictionary.builder()
            .name(A_TABLES)
            .item(Item.builder().name(A1).description("original series").outputClass(TsData.class).build())
            .item(Item.builder().name(A1A).description("forecasts of the original series").outputClass(TsData.class).build())
            .item(Item.builder().name(A1B).description("backcasts of the original series").outputClass(TsData.class).build())
            .item(Item.builder().name(A6).description("trading days effects").outputClass(TsData.class).build())
            .item(Item.builder().name(A7).description("moving holidays effects").outputClass(TsData.class).build())
            .item(Item.builder().name(A8).description("outliers").outputClass(TsData.class).build())
            .item(Item.builder().name(A8T).description("outliers associated to the trend").outputClass(TsData.class).build())
            .item(Item.builder().name(A8I).description("outliers associated to the irregular").outputClass(TsData.class).build())
            .item(Item.builder().name(A8S).description("outliers associated to the seasonal").outputClass(TsData.class).build())
            .item(Item.builder().name(A9).description("other regression effects").outputClass(TsData.class).build())
            .item(Item.builder().name(A9).description("other regression effects").outputClass(TsData.class).build())
            .item(Item.builder().name(A9U).description("other regression effects, split in the different components").outputClass(TsData.class).build())
            .item(Item.builder().name(A9SA).description("other regression effects, associated to the SA series").outputClass(TsData.class).build())
            .item(Item.builder().name(A9SER).description("other regression effects, removed of the series and not integrated in the components").outputClass(TsData.class).build())
            .build();
    
    public final String D10FINAL="d10final", D11FINAL="d11final", D12FINAL="d12final", D13FINAL="d13final", 
            D16="d16", D18="d18", D10A="d10a", D11A="d11a", D12A="d12a", D16A="d16a", D18A="d18a";
    
    public final String[] D_TABLE_FINAL = new String[]{D10FINAL, D11FINAL, 
        D12FINAL, D13FINAL, D16, D18, D10A, D11A, D12A, D16A, D18A};
    
    public final Dictionary DTABLES_FINAL = AtomicDictionary.builder()
            .name(D_TABLES)
            .item(Item.builder().name(D10FINAL).description("seasonal component").outputClass(TsData.class).build())
            .item(Item.builder().name(D11FINAL).description("final seasonally adjusted series").outputClass(TsData.class).build())
            .item(Item.builder().name(D12FINAL).description("final trend component").outputClass(TsData.class).build())
            .item(Item.builder().name(D13FINAL).description("final irregular component").outputClass(TsData.class).build())
            .item(Item.builder().name(D16).description("final seasonal component").outputClass(TsData.class).build())
            .item(Item.builder().name(D18).description("calendar effects").outputClass(TsData.class).build())
            .item(Item.builder().name(D10A).description("forecasts of the seasonal component").outputClass(TsData.class).build())
            .item(Item.builder().name(D11A).description("forecasts of the final seasonally adjusted series").outputClass(TsData.class).build())
            .item(Item.builder().name(D12A).description("forecasts of the final trend component").outputClass(TsData.class).build())
            .item(Item.builder().name(D16A).description("forecasts of the final seasonal component").outputClass(TsData.class).build())
            .item(Item.builder().name(D18A).description("forecasts of the calendar effects").outputClass(TsData.class).build())
            .build();
    
    
    
    public final Dictionary X13DICTIONARY=ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, RegArimaDictionaries.REGSARIMA))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.SADECOMPOSITION_F))
            .dictionary(new PrefixedDictionary(null, SaDictionaries.REGEFFECTS))
            .dictionary(new PrefixedDictionary(X11, SaDictionaries.CMPDECOMPOSITION))
            .dictionary(new PrefixedDictionary(X11, ATABLES))
            .dictionary(new PrefixedDictionary(X11, X11Dictionaries.BTABLES))
            .dictionary(new PrefixedDictionary(X11, X11Dictionaries.CTABLES))
            .dictionary(new PrefixedDictionary(X11, X11Dictionaries.DTABLES))
            .dictionary(new PrefixedDictionary(X11, DTABLES_FINAL))
            .dictionary(new PrefixedDictionary(X11, X11Dictionaries.ETABLES))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.COMBINEDSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICSEASONALITY))
            .dictionary(new PrefixedDictionary(SaDictionaries.DIAGNOSTICS, SaDictionaries.GENERICTRADINGDAYS))
            .build();
    
}
