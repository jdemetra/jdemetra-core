// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tramoseats.proto

package demetra.tramoseats.io.protobuf;

public final class TramoSeatsProtos {
  private TramoSeatsProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_DecompositionSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_DecompositionSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_BasicSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_BasicSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_TransformSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_TransformSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_OutlierSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_OutlierSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_AutoModelSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_AutoModelSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_EasterSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_EasterSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_TradingDaysSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_TradingDaysSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_RegressionSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_RegressionSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSpec_EstimateSpec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSpec_EstimateSpec_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_SeatsResults_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_SeatsResults_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSeatsResults_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSeatsResults_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoOutput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoOutput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoOutput_DetailsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoOutput_DetailsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSeatsOutput_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSeatsOutput_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_TramoSeatsOutput_DetailsEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_TramoSeatsOutput_DetailsEntry_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_tramoseats_Spec_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_tramoseats_Spec_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020tramoseats.proto\022\ntramoseats\032\rtoolkit." +
      "proto\032\017modelling.proto\032\016regarima.proto\032\010" +
      "sa.proto\"\254\002\n\021DecompositionSpec\022\023\n\013xl_bou" +
      "ndary\030\001 \001(\001\0225\n\rapproximation\030\002 \001(\0162\036.tra" +
      "moseats.SeatsApproximation\022\025\n\rseastolera" +
      "nce\030\003 \001(\001\022\026\n\016trend_boundary\030\004 \001(\001\022\025\n\rsea" +
      "s_boundary\030\005 \001(\001\022\033\n\023seas_boundary_at_pi\030" +
      "\006 \001(\001\022\027\n\017bias_correction\030\007 \001(\010\022\017\n\007nfcast" +
      "s\030\010 \001(\005\022\017\n\007nbcasts\030\t \001(\005\022-\n\talgorithm\030\n " +
      "\001(\0162\032.tramoseats.SeatsAlgorithm\"\202\016\n\tTram" +
      "oSpec\022.\n\005basic\030\001 \001(\0132\037.tramoseats.TramoS" +
      "pec.BasicSpec\0226\n\ttransform\030\002 \001(\0132#.tramo" +
      "seats.TramoSpec.TransformSpec\0222\n\007outlier" +
      "\030\003 \001(\0132!.tramoseats.TramoSpec.OutlierSpe" +
      "c\022#\n\005arima\030\004 \001(\0132\024.regarima.SarimaSpec\0226" +
      "\n\tautomodel\030\005 \001(\0132#.tramoseats.TramoSpec" +
      ".AutoModelSpec\0228\n\nregression\030\006 \001(\0132$.tra" +
      "moseats.TramoSpec.RegressionSpec\0224\n\010esti" +
      "mate\030\007 \001(\0132\".tramoseats.TramoSpec.Estima" +
      "teSpec\032G\n\tBasicSpec\022\037\n\004span\030\001 \001(\0132\021.jd3." +
      "TimeSelector\022\031\n\021preliminary_check\030\003 \001(\010\032" +
      "\227\001\n\rTransformSpec\0221\n\016transformation\030\001 \001(" +
      "\0162\031.modelling.Transformation\022\013\n\003fct\030\002 \001(" +
      "\001\022)\n\006adjust\030\003 \001(\0162\031.modelling.LengthOfPe" +
      "riod\022\033\n\023outliers_correction\030\004 \001(\010\032\227\001\n\013Ou" +
      "tlierSpec\022\017\n\007enabled\030\001 \001(\010\022\037\n\004span\030\002 \001(\013" +
      "2\021.jd3.TimeSelector\022\n\n\002ao\030\003 \001(\010\022\n\n\002ls\030\004 " +
      "\001(\010\022\n\n\002tc\030\005 \001(\010\022\n\n\002so\030\006 \001(\010\022\n\n\002va\030\007 \001(\001\022" +
      "\016\n\006tcrate\030\010 \001(\001\022\n\n\002ml\030\t \001(\010\032\232\001\n\rAutoMode" +
      "lSpec\022\017\n\007enabled\030\001 \001(\010\022\016\n\006cancel\030\002 \001(\001\022\013" +
      "\n\003ub1\030\003 \001(\001\022\013\n\003ub2\030\004 \001(\001\022\013\n\003pcr\030\005 \001(\001\022\n\n" +
      "\002pc\030\006 \001(\001\022\014\n\004tsig\030\007 \001(\001\022\022\n\naccept_def\030\010 " +
      "\001(\010\022\023\n\013ami_compare\030\t \001(\010\032\207\001\n\nEasterSpec\022" +
      "$\n\004type\030\001 \001(\0162\026.tramoseats.EasterType\022\020\n" +
      "\010duration\030\002 \001(\005\022\016\n\006julian\030\003 \001(\010\022\014\n\004test\030" +
      "\004 \001(\010\022#\n\013coefficient\030\n \001(\0132\016.jd3.Paramet" +
      "er\032\326\002\n\017TradingDaysSpec\022\"\n\002td\030\001 \001(\0162\026.mod" +
      "elling.TradingDays\022%\n\002lp\030\002 \001(\0162\031.modelli" +
      "ng.LengthOfPeriod\022\020\n\010holidays\030\003 \001(\t\022\r\n\005u" +
      "sers\030\004 \003(\t\022\t\n\001w\030\005 \001(\005\022)\n\004test\030\006 \001(\0162\033.tr" +
      "amoseats.TradingDaysTest\022.\n\004auto\030\007 \001(\0162 " +
      ".tramoseats.AutomaticTradingDays\022\r\n\005ptes" +
      "t\030\010 \001(\001\022\023\n\013auto_adjust\030\t \001(\010\022&\n\016tdcoeffi" +
      "cients\030\n \003(\0132\016.jd3.Parameter\022%\n\rlpcoeffi" +
      "cient\030\013 \001(\0132\016.jd3.Parameter\032\267\002\n\016Regressi" +
      "onSpec\022\034\n\004mean\030\001 \001(\0132\016.jd3.Parameter\0221\n\002" +
      "td\030\002 \001(\0132%.tramoseats.TramoSpec.TradingD" +
      "aysSpec\0220\n\006easter\030\003 \001(\0132 .tramoseats.Tra" +
      "moSpec.EasterSpec\022$\n\010outliers\030\004 \003(\0132\022.mo" +
      "delling.Outlier\022$\n\005users\030\005 \003(\0132\025.modelli" +
      "ng.TsVariable\0226\n\rinterventions\030\006 \003(\0132\037.m" +
      "odelling.InterventionVariable\022\036\n\005ramps\030\007" +
      " \003(\0132\017.modelling.Ramp\032U\n\014EstimateSpec\022\037\n" +
      "\004span\030\001 \001(\0132\021.jd3.TimeSelector\022\n\n\002ml\030\002 \001" +
      "(\010\022\013\n\003tol\030\003 \001(\001\022\013\n\003ubp\030\004 \001(\001\"\347\001\n\014SeatsRe" +
      "sults\022,\n\013seats_arima\030\001 \001(\0132\025.modelling.A" +
      "rimaModelH\000\022.\n\014seats_sarima\030\002 \001(\0132\026.mode" +
      "lling.SarimaModelH\000\022\014\n\004mean\030\003 \001(\010\0228\n\027can" +
      "onical_decomposition\030\004 \001(\0132\027.modelling.U" +
      "carimaModel\022(\n\013stochastics\030\005 \001(\0132\023.sa.Sa" +
      "DecompositionB\007\n\005model\"\301\001\n\021TramoSeatsRes" +
      "ults\022.\n\rpreprocessing\030\001 \001(\0132\027.regarima.R" +
      "egArimaModel\022/\n\rdecomposition\030\002 \001(\0132\030.tr" +
      "amoseats.SeatsResults\022\"\n\005final\030\003 \001(\0132\023.s" +
      "a.SaDecomposition\022\'\n\016diagnostics_sa\030\005 \001(" +
      "\0132\017.sa.Diagnostics\"\262\002\n\013TramoOutput\022\'\n\006re" +
      "sult\030\001 \001(\0132\027.regarima.RegArimaModel\022.\n\017e" +
      "stimation_spec\030\002 \001(\0132\025.tramoseats.TramoS" +
      "pec\022*\n\013result_spec\030\003 \001(\0132\025.tramoseats.Tr" +
      "amoSpec\022 \n\003log\030\004 \001(\0132\023.jd3.ProcessingLog" +
      "s\0225\n\007details\030\005 \003(\0132$.tramoseats.TramoOut" +
      "put.DetailsEntry\032E\n\014DetailsEntry\022\013\n\003key\030" +
      "\001 \001(\t\022$\n\005value\030\002 \001(\0132\025.jd3.ProcessingDet" +
      "ail:\0028\001\"\270\002\n\020TramoSeatsOutput\022-\n\006result\030\001" +
      " \001(\0132\035.tramoseats.TramoSeatsResults\022)\n\017e" +
      "stimation_spec\030\002 \001(\0132\020.tramoseats.Spec\022%" +
      "\n\013result_spec\030\003 \001(\0132\020.tramoseats.Spec\022 \n" +
      "\003log\030\004 \001(\0132\023.jd3.ProcessingLogs\022:\n\007detai" +
      "ls\030\005 \003(\0132).tramoseats.TramoSeatsOutput.D" +
      "etailsEntry\032E\n\014DetailsEntry\022\013\n\003key\030\001 \001(\t" +
      "\022$\n\005value\030\002 \001(\0132\025.jd3.ProcessingDetail:\002" +
      "8\001\"\206\001\n\004Spec\022$\n\005tramo\030\001 \001(\0132\025.tramoseats." +
      "TramoSpec\022,\n\005seats\030\002 \001(\0132\035.tramoseats.De" +
      "compositionSpec\022*\n\014benchmarking\030\003 \001(\0132\024." +
      "sa.BenchmarkingSpec*S\n\022SeatsApproximatio" +
      "n\022\022\n\016SEATS_APP_NONE\020\000\022\024\n\020SEATS_APP_LEGAC" +
      "Y\020\001\022\023\n\017SEATS_APP_NOISY\020\002*D\n\016SeatsAlgorit" +
      "hm\022\024\n\020SEATS_ALG_BURMAN\020\000\022\034\n\030SEATS_ALG_KA" +
      "LMANSMOOTHER\020\001*m\n\024AutomaticTradingDays\022\016" +
      "\n\nTD_AUTO_NO\020\000\022\021\n\rTD_AUTO_FTEST\020\001\022\020\n\014TD_" +
      "AUTO_WALD\020\002\022\017\n\013TD_AUTO_AIC\020\003\022\017\n\013TD_AUTO_" +
      "BIC\020\004*N\n\017TradingDaysTest\022\016\n\nTD_TEST_NO\020\000" +
      "\022\026\n\022TD_TEST_SEPARATE_T\020\001\022\023\n\017TD_TEST_JOIN" +
      "T_F\020\002*n\n\nEasterType\022\021\n\rEASTER_UNUSED\020\000\022\023" +
      "\n\017EASTER_STANDARD\020\001\022\030\n\024EASTER_INCLUDEEAS" +
      "TER\020\002\022\036\n\032EASTER_INCLUDEEASTERMONDAY\020\003B4\n" +
      "\036demetra.tramoseats.io.protobufB\020TramoSe" +
      "atsProtosP\001P\000P\001P\002P\003b\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          demetra.toolkit.io.protobuf.ToolkitProtos.getDescriptor(),
          demetra.modelling.io.protobuf.ModellingProtos.getDescriptor(),
          demetra.regarima.io.protobuf.RegArimaProtos.getDescriptor(),
          demetra.sa.io.protobuf.SaProtos.getDescriptor(),
        });
    internal_static_tramoseats_DecompositionSpec_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_tramoseats_DecompositionSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_DecompositionSpec_descriptor,
        new java.lang.String[] { "XlBoundary", "Approximation", "Seastolerance", "TrendBoundary", "SeasBoundary", "SeasBoundaryAtPi", "BiasCorrection", "Nfcasts", "Nbcasts", "Algorithm", });
    internal_static_tramoseats_TramoSpec_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_tramoseats_TramoSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_descriptor,
        new java.lang.String[] { "Basic", "Transform", "Outlier", "Arima", "Automodel", "Regression", "Estimate", });
    internal_static_tramoseats_TramoSpec_BasicSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(0);
    internal_static_tramoseats_TramoSpec_BasicSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_BasicSpec_descriptor,
        new java.lang.String[] { "Span", "PreliminaryCheck", });
    internal_static_tramoseats_TramoSpec_TransformSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(1);
    internal_static_tramoseats_TramoSpec_TransformSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_TransformSpec_descriptor,
        new java.lang.String[] { "Transformation", "Fct", "Adjust", "OutliersCorrection", });
    internal_static_tramoseats_TramoSpec_OutlierSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(2);
    internal_static_tramoseats_TramoSpec_OutlierSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_OutlierSpec_descriptor,
        new java.lang.String[] { "Enabled", "Span", "Ao", "Ls", "Tc", "So", "Va", "Tcrate", "Ml", });
    internal_static_tramoseats_TramoSpec_AutoModelSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(3);
    internal_static_tramoseats_TramoSpec_AutoModelSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_AutoModelSpec_descriptor,
        new java.lang.String[] { "Enabled", "Cancel", "Ub1", "Ub2", "Pcr", "Pc", "Tsig", "AcceptDef", "AmiCompare", });
    internal_static_tramoseats_TramoSpec_EasterSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(4);
    internal_static_tramoseats_TramoSpec_EasterSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_EasterSpec_descriptor,
        new java.lang.String[] { "Type", "Duration", "Julian", "Test", "Coefficient", });
    internal_static_tramoseats_TramoSpec_TradingDaysSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(5);
    internal_static_tramoseats_TramoSpec_TradingDaysSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_TradingDaysSpec_descriptor,
        new java.lang.String[] { "Td", "Lp", "Holidays", "Users", "W", "Test", "Auto", "Ptest", "AutoAdjust", "Tdcoefficients", "Lpcoefficient", });
    internal_static_tramoseats_TramoSpec_RegressionSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(6);
    internal_static_tramoseats_TramoSpec_RegressionSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_RegressionSpec_descriptor,
        new java.lang.String[] { "Mean", "Td", "Easter", "Outliers", "Users", "Interventions", "Ramps", });
    internal_static_tramoseats_TramoSpec_EstimateSpec_descriptor =
      internal_static_tramoseats_TramoSpec_descriptor.getNestedTypes().get(7);
    internal_static_tramoseats_TramoSpec_EstimateSpec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSpec_EstimateSpec_descriptor,
        new java.lang.String[] { "Span", "Ml", "Tol", "Ubp", });
    internal_static_tramoseats_SeatsResults_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_tramoseats_SeatsResults_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_SeatsResults_descriptor,
        new java.lang.String[] { "SeatsArima", "SeatsSarima", "Mean", "CanonicalDecomposition", "Stochastics", "Model", });
    internal_static_tramoseats_TramoSeatsResults_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_tramoseats_TramoSeatsResults_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSeatsResults_descriptor,
        new java.lang.String[] { "Preprocessing", "Decomposition", "Final", "DiagnosticsSa", });
    internal_static_tramoseats_TramoOutput_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_tramoseats_TramoOutput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoOutput_descriptor,
        new java.lang.String[] { "Result", "EstimationSpec", "ResultSpec", "Log", "Details", });
    internal_static_tramoseats_TramoOutput_DetailsEntry_descriptor =
      internal_static_tramoseats_TramoOutput_descriptor.getNestedTypes().get(0);
    internal_static_tramoseats_TramoOutput_DetailsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoOutput_DetailsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_tramoseats_TramoSeatsOutput_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_tramoseats_TramoSeatsOutput_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSeatsOutput_descriptor,
        new java.lang.String[] { "Result", "EstimationSpec", "ResultSpec", "Log", "Details", });
    internal_static_tramoseats_TramoSeatsOutput_DetailsEntry_descriptor =
      internal_static_tramoseats_TramoSeatsOutput_descriptor.getNestedTypes().get(0);
    internal_static_tramoseats_TramoSeatsOutput_DetailsEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_TramoSeatsOutput_DetailsEntry_descriptor,
        new java.lang.String[] { "Key", "Value", });
    internal_static_tramoseats_Spec_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_tramoseats_Spec_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_tramoseats_Spec_descriptor,
        new java.lang.String[] { "Tramo", "Seats", "Benchmarking", });
    demetra.toolkit.io.protobuf.ToolkitProtos.getDescriptor();
    demetra.modelling.io.protobuf.ModellingProtos.getDescriptor();
    demetra.regarima.io.protobuf.RegArimaProtos.getDescriptor();
    demetra.sa.io.protobuf.SaProtos.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
