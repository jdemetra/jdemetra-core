/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.data.Parameter;
import demetra.timeseries.regression.RegressionItem;
import demetra.toolkit.dictionaries.AtomicDictionary.Item;
import demetra.toolkit.dictionaries.Dictionary.EntryType;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ArimaDictionaries {

    public final String AR = "ar", // Stationary auto-regressive polynomial
            DELTA = "delta", // Differencing polynomial
            MA = "ma", // Moving average polynomial
            VAR = "var";// Innovation variance

    public final Dictionary ARIMA = AtomicDictionary.builder()
            .name("arima")
            .item(Item.builder().name(AR).description("stationary autoregressive polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(DELTA).description("non-stationary autoregressive polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(MA).description("moving-average polynomial").outputClass(double[].class).build())
            .item(Item.builder().name(VAR).description("innovation variance").outputClass(Double.class).build())
            .build();

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
           PERIOD = "period";

    public final Dictionary SARIMA = AtomicDictionary.builder()
            .name("sarima")
            .item(Item.builder().name(PERIOD).description("period of the seasonal part").outputClass(Integer.class).build())
            .item(Item.builder().name(P).description("regular autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(D).description("regular differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(Q).description("regular moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(BP).description("seasonal autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(BD).description("seasonal differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(BQ).description("seasonal moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(PHI).description("regular autoregressive parameters").outputClass(double[].class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameters").outputClass(double[].class).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameters").outputClass(double[].class).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameters").outputClass(double[].class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameter").outputClass(Double.class).type(EntryType.Array).build())
            .item(Item.builder().name(PHI).description("seasonal autoregressive parameter").outputClass(Double.class).type(EntryType.Array).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameter").outputClass(Double.class).type(EntryType.Array).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameter").outputClass(Double.class).type(EntryType.Array).build())
            .item(Item.builder().name(PARAMETERS).description("phi, bphi, theta, btheta").outputClass(double[].class).build())
            .item(Item.builder().name(PARAMETERS2).description("-phi, -bphi, theta, btheta").outputClass(double[].class).build())
            .build();

    public final Dictionary SARIMA_ESTIMATION = AtomicDictionary.builder()
            .name("sarima")
            .item(Item.builder().name(P).description("regular autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(D).description("regular differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(Q).description("regular moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(BP).description("seasonal autoregressive order").outputClass(Integer.class).build())
            .item(Item.builder().name(BD).description("seasonal differencing order").outputClass(Integer.class).build())
            .item(Item.builder().name(BQ).description("seasonal moving-average order").outputClass(Integer.class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameter").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(PHI).description("seasonal autoregressive parameter").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameter").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameter").outputClass(RegressionItem.class).type(EntryType.Array).build())
            .build();

    public final  String COMPONENT = "component", COMPONENTC = "complement", MODEL = "model", 
            SUM = "sum", // Reduced model
            SIZE = "size";  // Number of components
    
    private final Dictionary UCARIMA_DETAILS=AtomicDictionary.builder()
                            .item(Item.builder()
                                    .name(SIZE).description("number of components").outputClass(Integer.class).build())
                            .build();

    public final Dictionary UCARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, UCARIMA_DETAILS))
            .dictionary(new PrefixedDictionary(MODEL, ARIMA))
            .dictionary(new PrefixedDictionary(COMPONENT, ARIMA, EntryType.Array))
            .dictionary(new PrefixedDictionary(COMPONENTC, ARIMA, EntryType.Array))
            .dictionary(new PrefixedDictionary(SUM, ARIMA))
            .build();

}
