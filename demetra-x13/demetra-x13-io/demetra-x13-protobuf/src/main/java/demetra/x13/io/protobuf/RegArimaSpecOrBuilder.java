// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: x13.proto

package demetra.x13.io.protobuf;

public interface RegArimaSpecOrBuilder extends
    // @@protoc_insertion_point(interface_extends:x13.RegArimaSpec)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.x13.RegArimaSpec.BasicSpec basic = 1;</code>
   * @return Whether the basic field is set.
   */
  boolean hasBasic();
  /**
   * <code>.x13.RegArimaSpec.BasicSpec basic = 1;</code>
   * @return The basic.
   */
  demetra.x13.io.protobuf.RegArimaSpec.BasicSpec getBasic();
  /**
   * <code>.x13.RegArimaSpec.BasicSpec basic = 1;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.BasicSpecOrBuilder getBasicOrBuilder();

  /**
   * <code>.x13.RegArimaSpec.TransformSpec transform = 2;</code>
   * @return Whether the transform field is set.
   */
  boolean hasTransform();
  /**
   * <code>.x13.RegArimaSpec.TransformSpec transform = 2;</code>
   * @return The transform.
   */
  demetra.x13.io.protobuf.RegArimaSpec.TransformSpec getTransform();
  /**
   * <code>.x13.RegArimaSpec.TransformSpec transform = 2;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.TransformSpecOrBuilder getTransformOrBuilder();

  /**
   * <code>.x13.RegArimaSpec.OutlierSpec outlier = 3;</code>
   * @return Whether the outlier field is set.
   */
  boolean hasOutlier();
  /**
   * <code>.x13.RegArimaSpec.OutlierSpec outlier = 3;</code>
   * @return The outlier.
   */
  demetra.x13.io.protobuf.RegArimaSpec.OutlierSpec getOutlier();
  /**
   * <code>.x13.RegArimaSpec.OutlierSpec outlier = 3;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.OutlierSpecOrBuilder getOutlierOrBuilder();

  /**
   * <code>.regarima.SarimaSpec arima = 4;</code>
   * @return Whether the arima field is set.
   */
  boolean hasArima();
  /**
   * <code>.regarima.SarimaSpec arima = 4;</code>
   * @return The arima.
   */
  demetra.regarima.io.protobuf.RegArimaProtos.SarimaSpec getArima();
  /**
   * <code>.regarima.SarimaSpec arima = 4;</code>
   */
  demetra.regarima.io.protobuf.RegArimaProtos.SarimaSpecOrBuilder getArimaOrBuilder();

  /**
   * <code>.x13.RegArimaSpec.AutoModelSpec automodel = 5;</code>
   * @return Whether the automodel field is set.
   */
  boolean hasAutomodel();
  /**
   * <code>.x13.RegArimaSpec.AutoModelSpec automodel = 5;</code>
   * @return The automodel.
   */
  demetra.x13.io.protobuf.RegArimaSpec.AutoModelSpec getAutomodel();
  /**
   * <code>.x13.RegArimaSpec.AutoModelSpec automodel = 5;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.AutoModelSpecOrBuilder getAutomodelOrBuilder();

  /**
   * <code>.x13.RegArimaSpec.RegressionSpec regression = 6;</code>
   * @return Whether the regression field is set.
   */
  boolean hasRegression();
  /**
   * <code>.x13.RegArimaSpec.RegressionSpec regression = 6;</code>
   * @return The regression.
   */
  demetra.x13.io.protobuf.RegArimaSpec.RegressionSpec getRegression();
  /**
   * <code>.x13.RegArimaSpec.RegressionSpec regression = 6;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.RegressionSpecOrBuilder getRegressionOrBuilder();

  /**
   * <code>.x13.RegArimaSpec.EstimateSpec estimate = 7;</code>
   * @return Whether the estimate field is set.
   */
  boolean hasEstimate();
  /**
   * <code>.x13.RegArimaSpec.EstimateSpec estimate = 7;</code>
   * @return The estimate.
   */
  demetra.x13.io.protobuf.RegArimaSpec.EstimateSpec getEstimate();
  /**
   * <code>.x13.RegArimaSpec.EstimateSpec estimate = 7;</code>
   */
  demetra.x13.io.protobuf.RegArimaSpec.EstimateSpecOrBuilder getEstimateOrBuilder();
}
