/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.tstoolkit.jdr.x11;

import static demetra.information.InformationExtractor.concatenate;
import demetra.information.InformationMapping;
import ec.satoolkit.x11.X11Results;
import ec.tstoolkit.timeseries.simplets.TsData;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.experimental.UtilityClass
public class X11DecompositionInfo {

    final String A = "a-tables", B = "b-tables", C = "c-tables",
            D = "d-tables", E = "e-tables", F = "f-tables";
    final String A1 = "a1", A1a = "a1a", A1b = "a1b",
            A6 = "a6", A7 = "a7",
            A8 = "a8", A8t = "a8t",
            A8s = "a8s", A8i = "a8i", A9 = "a9", A9u = "a9u", A9sa = "a9sa", A9ser = "a9ser";
    final String B1 = "b1", B2 = "b2", B3 = "b3",
            B3TEST = "b3-seasonalityTest", B4 = "b4", B5 = "b5", B6 = "b6",
            B7 = "b7", B7_IC = "b7-IC ratio", B8 = "b8", B9 = "b9",
            B10 = "b10", B11 = "b11", B12 = "b12", B13 = "b13", B14 = "b14",
            B15 = "b15", B16 = "b16", B17 = "b17", B18 = "b18", B19 = "b19",
            B20 = "b20";
    final String C1 = "c1", C2 = "c2", C3 = "c3",
            C3TEST = "c3-seasonalityTest", C4 = "c4", C5 = "c5", C6 = "c6",
            C7 = "c7", C7_IC = "c7-IC ratio", C8 = "c8", C9 = "c9",
            C10 = "c10", C11 = "c11", C12 = "c12", C13 = "c13", C14 = "c14",
            C15 = "c15", C16 = "c16", C17 = "c17", C18 = "c18", C19 = "c19",
            C20 = "c20";
    final String D1 = "d1", D2 = "d2", D3 = "d3",
            D3TEST = "d3-seasonalityTest", D4 = "d4", D5 = "d5", D6 = "d6",
            D7 = "d7", D7_IC = "d7-IC ratio", D8 = "d8", D9 = "d9",
            D10 = "d10", D10a = "d10a", D10b = "d10b", D11 = "d11", D11a = "d11a", D12 = "d12", D12a = "d12a", D12_IC = "d12-IC ratio",
            D13 = "d13", D14 = "d14", D15 = "d15", D16 = "d16", D16a = "d16a", D16b = "d16b", D17 = "d17",
            D18 = "d18", D19 = "d19", D20 = "d20", D9_RMS = "finalRMS",
            D9_RMSROUND = "rmsRounds", D9_SLEN = "slen",
            D10L = "d10_lin", D11L = "d11_lin", D12L = "d12_lin", D13L = "d13_lin",
            D10aL = "d10a_lin", D11aL = "d11a_lin", D12aL = "d12a_lin", D13aL = "d13a_lin",
            D9_DEFAULT = "s3x5default", D9_FILTER = "d9filter", D12_FILTER = "d12filter", D12_TLEN = "tlen", D9_FILTER_COMPOSIT = "d9filtercomposit";
    final String E1 = "e1", E2 = "e2", E3 = "e3", E11 = "e11";

    final String[] ALL_A = {A1, A1a, A1b, A6, A7, A8, A8t, A8s, A8i, A9, A9sa, A9u, A9ser};
    final String[] ALL_B = {B1, B2, B3, B4, B5, B6, B7, B8, B9,
        B10, B11, B12, B13, B14, B15, B16, B17, B18, B19, B20};
    final String[] ALL_C = {C1, C2, C3, C4, C5, C6, C7, C8, C9, C10,
        C11, C12, C13, C14, C15, C16, C17, C18, C19, C20};
    final String[] ALL_D = {D1, D2, D3, D4, D5, D6, D7, D8, D9,
        D10, D10a, D10b, D11, D11a, D12, D12a, D13, D14, D15, D16, D16a, D16b, D18, D19, D20};
    final String[] ALL_E = {E1, E2, E3, E11};

    final InformationMapping<X11Results> MAPPING = new InformationMapping<>(X11Results.class);

    static {
        for (int i = 0; i < ALL_A.length; ++i) {
            final String item = concatenate(A, ALL_A[i]);
            MAPPING.set(ALL_A[i], TsData.class, source -> source.getData(item, TsData.class));
        }
        for (int i = 0; i < ALL_B.length; ++i) {
            final String item = concatenate(B, ALL_B[i]);
            MAPPING.set(ALL_B[i], TsData.class, source -> source.getData(item, TsData.class));
        }
        for (int i = 0; i < ALL_C.length; ++i) {
            final String item = concatenate(C, ALL_C[i]);
            MAPPING.set(ALL_C[i], TsData.class, source -> source.getData(item, TsData.class));
        }
        for (int i = 0; i < ALL_D.length; ++i) {
            final String item = concatenate(D, ALL_D[i]);
            MAPPING.set(ALL_D[i], TsData.class, source -> source.getData(item, TsData.class));
        }
        for (int i = 0; i < ALL_E.length; ++i) {
            final String item = concatenate(E, ALL_E[i]);
            MAPPING.set(ALL_E[i], TsData.class, source -> source.getData(item, TsData.class));
        }
        MAPPING.set(D9_FILTER, String.class, source->source.getData(D9_FILTER, String.class));
        MAPPING.set(D9_SLEN, Integer.class, source->source.getData(D9_SLEN, Integer.class));
        MAPPING.set(D12_FILTER, String.class, source->source.getData(D12_FILTER, String.class));
        MAPPING.set(D12_TLEN, Integer.class, source->source.getData(D12_TLEN, Integer.class));
    }

    public InformationMapping<X11Results> getMapping() {
        return MAPPING;
    }

}
