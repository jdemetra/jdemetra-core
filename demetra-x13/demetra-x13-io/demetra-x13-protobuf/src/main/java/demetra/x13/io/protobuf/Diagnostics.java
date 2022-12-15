// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: x13.proto

package demetra.x13.io.protobuf;

/**
 * Protobuf type {@code x13.Diagnostics}
 */
public final class Diagnostics extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:x13.Diagnostics)
    DiagnosticsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Diagnostics.newBuilder() to construct.
  private Diagnostics(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Diagnostics() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Diagnostics();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Diagnostics(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {
            demetra.x13.io.protobuf.MStatistics.Builder subBuilder = null;
            if (mstatistics_ != null) {
              subBuilder = mstatistics_.toBuilder();
            }
            mstatistics_ = input.readMessage(demetra.x13.io.protobuf.MStatistics.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(mstatistics_);
              mstatistics_ = subBuilder.buildPartial();
            }

            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return demetra.x13.io.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return demetra.x13.io.protobuf.X13Protos.internal_static_x13_Diagnostics_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            demetra.x13.io.protobuf.Diagnostics.class, demetra.x13.io.protobuf.Diagnostics.Builder.class);
  }

  public static final int MSTATISTICS_FIELD_NUMBER = 1;
  private demetra.x13.io.protobuf.MStatistics mstatistics_;
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   * @return Whether the mstatistics field is set.
   */
  @java.lang.Override
  public boolean hasMstatistics() {
    return mstatistics_ != null;
  }
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   * @return The mstatistics.
   */
  @java.lang.Override
  public demetra.x13.io.protobuf.MStatistics getMstatistics() {
    return mstatistics_ == null ? demetra.x13.io.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
  }
  /**
   * <code>.x13.MStatistics mstatistics = 1;</code>
   */
  @java.lang.Override
  public demetra.x13.io.protobuf.MStatisticsOrBuilder getMstatisticsOrBuilder() {
    return getMstatistics();
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (mstatistics_ != null) {
      output.writeMessage(1, getMstatistics());
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (mstatistics_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getMstatistics());
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof demetra.x13.io.protobuf.Diagnostics)) {
      return super.equals(obj);
    }
    demetra.x13.io.protobuf.Diagnostics other = (demetra.x13.io.protobuf.Diagnostics) obj;

    if (hasMstatistics() != other.hasMstatistics()) return false;
    if (hasMstatistics()) {
      if (!getMstatistics()
          .equals(other.getMstatistics())) return false;
    }
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasMstatistics()) {
      hash = (37 * hash) + MSTATISTICS_FIELD_NUMBER;
      hash = (53 * hash) + getMstatistics().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static demetra.x13.io.protobuf.Diagnostics parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(demetra.x13.io.protobuf.Diagnostics prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code x13.Diagnostics}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:x13.Diagnostics)
      demetra.x13.io.protobuf.DiagnosticsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return demetra.x13.io.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return demetra.x13.io.protobuf.X13Protos.internal_static_x13_Diagnostics_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              demetra.x13.io.protobuf.Diagnostics.class, demetra.x13.io.protobuf.Diagnostics.Builder.class);
    }

    // Construct using demetra.x13.io.protobuf.Diagnostics.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (mstatisticsBuilder_ == null) {
        mstatistics_ = null;
      } else {
        mstatistics_ = null;
        mstatisticsBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return demetra.x13.io.protobuf.X13Protos.internal_static_x13_Diagnostics_descriptor;
    }

    @java.lang.Override
    public demetra.x13.io.protobuf.Diagnostics getDefaultInstanceForType() {
      return demetra.x13.io.protobuf.Diagnostics.getDefaultInstance();
    }

    @java.lang.Override
    public demetra.x13.io.protobuf.Diagnostics build() {
      demetra.x13.io.protobuf.Diagnostics result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public demetra.x13.io.protobuf.Diagnostics buildPartial() {
      demetra.x13.io.protobuf.Diagnostics result = new demetra.x13.io.protobuf.Diagnostics(this);
      if (mstatisticsBuilder_ == null) {
        result.mstatistics_ = mstatistics_;
      } else {
        result.mstatistics_ = mstatisticsBuilder_.build();
      }
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof demetra.x13.io.protobuf.Diagnostics) {
        return mergeFrom((demetra.x13.io.protobuf.Diagnostics)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(demetra.x13.io.protobuf.Diagnostics other) {
      if (other == demetra.x13.io.protobuf.Diagnostics.getDefaultInstance()) return this;
      if (other.hasMstatistics()) {
        mergeMstatistics(other.getMstatistics());
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      demetra.x13.io.protobuf.Diagnostics parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (demetra.x13.io.protobuf.Diagnostics) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private demetra.x13.io.protobuf.MStatistics mstatistics_;
    private com.google.protobuf.SingleFieldBuilderV3<
        demetra.x13.io.protobuf.MStatistics, demetra.x13.io.protobuf.MStatistics.Builder, demetra.x13.io.protobuf.MStatisticsOrBuilder> mstatisticsBuilder_;
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     * @return Whether the mstatistics field is set.
     */
    public boolean hasMstatistics() {
      return mstatisticsBuilder_ != null || mstatistics_ != null;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     * @return The mstatistics.
     */
    public demetra.x13.io.protobuf.MStatistics getMstatistics() {
      if (mstatisticsBuilder_ == null) {
        return mstatistics_ == null ? demetra.x13.io.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
      } else {
        return mstatisticsBuilder_.getMessage();
      }
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder setMstatistics(demetra.x13.io.protobuf.MStatistics value) {
      if (mstatisticsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        mstatistics_ = value;
        onChanged();
      } else {
        mstatisticsBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder setMstatistics(
        demetra.x13.io.protobuf.MStatistics.Builder builderForValue) {
      if (mstatisticsBuilder_ == null) {
        mstatistics_ = builderForValue.build();
        onChanged();
      } else {
        mstatisticsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder mergeMstatistics(demetra.x13.io.protobuf.MStatistics value) {
      if (mstatisticsBuilder_ == null) {
        if (mstatistics_ != null) {
          mstatistics_ =
            demetra.x13.io.protobuf.MStatistics.newBuilder(mstatistics_).mergeFrom(value).buildPartial();
        } else {
          mstatistics_ = value;
        }
        onChanged();
      } else {
        mstatisticsBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public Builder clearMstatistics() {
      if (mstatisticsBuilder_ == null) {
        mstatistics_ = null;
        onChanged();
      } else {
        mstatistics_ = null;
        mstatisticsBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public demetra.x13.io.protobuf.MStatistics.Builder getMstatisticsBuilder() {
      
      onChanged();
      return getMstatisticsFieldBuilder().getBuilder();
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    public demetra.x13.io.protobuf.MStatisticsOrBuilder getMstatisticsOrBuilder() {
      if (mstatisticsBuilder_ != null) {
        return mstatisticsBuilder_.getMessageOrBuilder();
      } else {
        return mstatistics_ == null ?
            demetra.x13.io.protobuf.MStatistics.getDefaultInstance() : mstatistics_;
      }
    }
    /**
     * <code>.x13.MStatistics mstatistics = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        demetra.x13.io.protobuf.MStatistics, demetra.x13.io.protobuf.MStatistics.Builder, demetra.x13.io.protobuf.MStatisticsOrBuilder> 
        getMstatisticsFieldBuilder() {
      if (mstatisticsBuilder_ == null) {
        mstatisticsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            demetra.x13.io.protobuf.MStatistics, demetra.x13.io.protobuf.MStatistics.Builder, demetra.x13.io.protobuf.MStatisticsOrBuilder>(
                getMstatistics(),
                getParentForChildren(),
                isClean());
        mstatistics_ = null;
      }
      return mstatisticsBuilder_;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:x13.Diagnostics)
  }

  // @@protoc_insertion_point(class_scope:x13.Diagnostics)
  private static final demetra.x13.io.protobuf.Diagnostics DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new demetra.x13.io.protobuf.Diagnostics();
  }

  public static demetra.x13.io.protobuf.Diagnostics getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Diagnostics>
      PARSER = new com.google.protobuf.AbstractParser<Diagnostics>() {
    @java.lang.Override
    public Diagnostics parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Diagnostics(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Diagnostics> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Diagnostics> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public demetra.x13.io.protobuf.Diagnostics getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

