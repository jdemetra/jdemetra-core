package internal.test;

import com.google.common.collect.ImmutableList;
import ec.demetra.workspace.WorkspaceFamily;
import static ec.demetra.workspace.WorkspaceFamily.MOD_DOC_REGARIMA;
import static ec.demetra.workspace.WorkspaceFamily.MOD_DOC_TRAMO;
import static ec.demetra.workspace.WorkspaceFamily.MOD_SPEC_REGARIMA;
import static ec.demetra.workspace.WorkspaceFamily.MOD_SPEC_TRAMO;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_DOC_X13;
import static ec.demetra.workspace.WorkspaceFamily.SA_MULTI;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_TRAMOSEATS;
import static ec.demetra.workspace.WorkspaceFamily.SA_SPEC_X13;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_CAL;
import static ec.demetra.workspace.WorkspaceFamily.UTIL_VAR;
import ec.demetra.workspace.WorkspaceItem;
import ec.tss.sa.ISaProcessingFactoryLoader;
import ec.tss.sa.SaManager;
import internal.io.PathUtil;
import java.nio.file.Path;
import java.util.List;

/*
 * Copyright 2017 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
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
/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
public class TestResources {

    static {
        // FIXME: X13Document constructor needs ISaProcessingFactory
        new ISaProcessingFactoryLoader().get().forEach(SaManager.instance::add);
    }

    public static final Path GENERIC_INDEX = PathUtil.get(TestResources.class.getResource("/generic_ws.xml"));
    public static final Path GENERIC_ROOT = PathUtil.get(TestResources.class.getResource("/generic_ws"));

    public static final WorkspaceItem GENERIC_SA_MULTI = of(SA_MULTI, "SAProcessing-1", "hello world");
    public static final WorkspaceItem GENERIC_SA_DOC_X13 = of(SA_DOC_X13, "X13Doc-1");
    public static final WorkspaceItem GENERIC_SA_DOC_TRAMOSEATS = of(SA_DOC_TRAMOSEATS, "TramoSeatsDoc-1");
    public static final WorkspaceItem GENERIC_SA_SPEC_X13 = of(SA_SPEC_X13, "X13Spec-1");
    public static final WorkspaceItem GENERIC_SA_SPEC_TRAMOSEATS = of(SA_SPEC_TRAMOSEATS, "TramoSeatsSpec-1");

    public static final WorkspaceItem GENERIC_MOD_DOC_REGARIMA = of(MOD_DOC_REGARIMA, "RegArimaDoc-1");
    public static final WorkspaceItem GENERIC_MOD_DOC_TRAMO = of(MOD_DOC_TRAMO, "TramoDoc-1");
    public static final WorkspaceItem GENERIC_MOD_SPEC_REGARIMA = of(MOD_SPEC_REGARIMA, "RegArimaSpec-1");
    public static final WorkspaceItem GENERIC_MOD_SPEC_TRAMO = of(MOD_SPEC_TRAMO, "TramoSpec-1");

    public static final WorkspaceItem GENERIC_UTIL_CAL = of(UTIL_CAL, "Calendars");
    public static final WorkspaceItem GENERIC_UTIL_VAR = of(UTIL_VAR, "Vars-1");

    public static final List<WorkspaceItem> GENERIC_ITEMS = ImmutableList.of(
            GENERIC_MOD_SPEC_REGARIMA, GENERIC_SA_SPEC_X13, GENERIC_SA_DOC_X13, GENERIC_SA_SPEC_TRAMOSEATS,
            GENERIC_MOD_SPEC_TRAMO, GENERIC_MOD_DOC_TRAMO, GENERIC_SA_MULTI, GENERIC_UTIL_VAR, GENERIC_UTIL_CAL,
            GENERIC_MOD_DOC_REGARIMA, GENERIC_SA_DOC_TRAMOSEATS
    );

    public static final Path LEGACY_INDEX = PathUtil.get(TestResources.class.getResource("/legacy_ws.xml"));
    public static final Path LEGACY_ROOT = PathUtil.get(TestResources.class.getResource("/legacy_ws"));

    public static final WorkspaceItem LEGACY_SA_MULTI = of(SA_MULTI, "SAProcessing-1");
    public static final WorkspaceItem LEGACY_SA_DOC_X13 = of(SA_DOC_X13, "X12 [1]");
    public static final WorkspaceItem LEGACY_SA_DOC_TRAMOSEATS = of(SA_DOC_TRAMOSEATS, "TramoSeats [1]");
    public static final WorkspaceItem LEGACY_SA_SPEC_X13 = of(SA_SPEC_X13, "X12Spec-1");
    public static final WorkspaceItem LEGACY_SA_SPEC_TRAMOSEATS = of(SA_SPEC_TRAMOSEATS, "TramoSeatsSpec-1");

    public static final WorkspaceItem LEGACY_UTIL_CAL = of(UTIL_CAL, "Calendars");
    public static final WorkspaceItem LEGACY_UTIL_VAR = of(UTIL_VAR, "Variables");

    public static final List<WorkspaceItem> LEGACY_ITEMS = ImmutableList.of(
            LEGACY_SA_MULTI, LEGACY_SA_DOC_X13, LEGACY_UTIL_VAR, LEGACY_SA_SPEC_TRAMOSEATS,
            LEGACY_SA_SPEC_X13, LEGACY_UTIL_CAL, LEGACY_SA_DOC_TRAMOSEATS
    );

    private WorkspaceItem of(WorkspaceFamily family, String name) {
        return WorkspaceItem.builder().family(family).id(name).label(name).build();
    }

    private WorkspaceItem of(WorkspaceFamily family, String name, String comments) {
        return WorkspaceItem.builder().family(family).id(name).label(name).comments(comments).build();
    }
}
