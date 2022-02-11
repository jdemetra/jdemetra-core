/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demetra.toolkit.dictionaries;

import demetra.toolkit.dictionaries.AtomicDictionary.Item;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class ArimaDictionaries {

    public final String AR = "ar", // Stationary auto-regressive polynomial
            DELTA = "delta", // Differencing polynomial
            MA = "ma", // Moving average polynomial
            VAR = "var",// Innovation variance
            NAME = "name";

    public final Dictionary ARIMA = AtomicDictionary.builder()
            .name("arima")
            .item(Item.builder().name(AR).description("stationary autoregressive polynomial").type(double[].class).build())
            .item(Item.builder().name(DELTA).description("non-stationary autoregressive polynomial").type(double[].class).build())
            .item(Item.builder().name(MA).description("moving-average polynomial").type(double[].class).build())
            .item(Item.builder().name(VAR).description("innovation variance").type(double.class).build())
            .item(Item.builder().name(NAME).description("name of the model").type(String.class).build())
            .build();

    public final static String P = "p", D = "d", Q = "q",
            BP = "bp", BD = "bd", BQ = "bq",
            PARAMETERS = "parameters", PARAMETERS2 = "parameters2",
            PHI = "phi", THETA = "theta", BPHI = "bphi", BTHETA = "btheta",
            PHI_ = "phi(?)", THETA_ = "theta(?)", BPHI_ = "bphi(?)", BTHETA_ = "btheta(?)",
            PERIOD = "period";

    public final Dictionary SARIMA = AtomicDictionary.builder()
            .name("sarima")
            .item(Item.builder().name(PERIOD).description("period of the seasonal part").type(int.class).build())
            .item(Item.builder().name(P).description("regular autoregressive order").type(int.class).build())
            .item(Item.builder().name(D).description("regular differencing order").type(int.class).build())
            .item(Item.builder().name(Q).description("regular moving-average order").type(int.class).build())
            .item(Item.builder().name(BP).description("seasonal autoregressive order").type(int.class).build())
            .item(Item.builder().name(BD).description("seasonal differencing order").type(int.class).build())
            .item(Item.builder().name(BQ).description("seasonal moving-average order").type(int.class).build())
            .item(Item.builder().name(PHI).description("regular autoregressive parameters").type(double[].class).build())
            .item(Item.builder().name(THETA).description("regular moving-average parameters").type(double[].class).build())
            .item(Item.builder().name(BPHI).description("seasonal autoregressive parameters").type(double[].class).build())
            .item(Item.builder().name(BTHETA).description("seasonal moving-average parameters").type(double[].class).build())
            .item(Item.builder().name(THETA_).description("regular moving-average parameter").type(double.class).list(true).build())
            .item(Item.builder().name(PHI_).description("seasonal autoregressive parameter").type(double.class).list(true).build())
            .item(Item.builder().name(BTHETA_).description("seasonal moving-average parameter").type(double.class).list(true).build())
            .item(Item.builder().name(BPHI_).description("seasonal autoregressive parameter").type(double.class).list(true).build())
            .item(Item.builder().name(PARAMETERS).description("phi, bphi, theta, btheta").type(double[].class).build())
            .item(Item.builder().name(PARAMETERS2).description("-phi, -bphi, theta, btheta").type(double[].class).build())
            .item(Item.builder().name(NAME).description("name of the model").type(String.class).build())
            .build();

    public final  String COMPONENT = "component(?)", MODEL = "model", REDUCEDMODEL = "reducedmodel", // Component
            SUM = "sum", // Reduced model
            SIZE = "size";  // Number of components
    
    private final Dictionary UCARIMA_DETAILS=AtomicDictionary.builder()
                            .item(Item.builder()
                                    .name(SIZE).description("number of components").type(int.class).build())
                            .build();

    public final Dictionary UCARIMA = ComplexDictionary.builder()
            .dictionary(new PrefixedDictionary(null, UCARIMA_DETAILS))
            .dictionary(new PrefixedDictionary(MODEL, ARIMA))
            .dictionary(new PrefixedDictionary(COMPONENT, ARIMA))
            .dictionary(new PrefixedDictionary(REDUCEDMODEL, ARIMA))
            .build();

}
