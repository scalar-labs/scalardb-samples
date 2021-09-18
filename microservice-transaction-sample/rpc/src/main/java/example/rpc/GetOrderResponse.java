// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: example.proto

package example.rpc;

/**
 * Protobuf type {@code rpc.GetOrderResponse}
 */
public final class GetOrderResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:rpc.GetOrderResponse)
    GetOrderResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GetOrderResponse.newBuilder() to construct.
  private GetOrderResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GetOrderResponse() {
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new GetOrderResponse();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private GetOrderResponse(
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
            example.rpc.Order.Builder subBuilder = null;
            if (order_ != null) {
              subBuilder = order_.toBuilder();
            }
            order_ = input.readMessage(example.rpc.Order.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(order_);
              order_ = subBuilder.buildPartial();
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
    return example.rpc.Example.internal_static_rpc_GetOrderResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return example.rpc.Example.internal_static_rpc_GetOrderResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            example.rpc.GetOrderResponse.class, example.rpc.GetOrderResponse.Builder.class);
  }

  public static final int ORDER_FIELD_NUMBER = 1;
  private example.rpc.Order order_;
  /**
   * <code>.rpc.Order order = 1;</code>
   * @return Whether the order field is set.
   */
  @java.lang.Override
  public boolean hasOrder() {
    return order_ != null;
  }
  /**
   * <code>.rpc.Order order = 1;</code>
   * @return The order.
   */
  @java.lang.Override
  public example.rpc.Order getOrder() {
    return order_ == null ? example.rpc.Order.getDefaultInstance() : order_;
  }
  /**
   * <code>.rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public example.rpc.OrderOrBuilder getOrderOrBuilder() {
    return getOrder();
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
    if (order_ != null) {
      output.writeMessage(1, getOrder());
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (order_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getOrder());
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
    if (!(obj instanceof example.rpc.GetOrderResponse)) {
      return super.equals(obj);
    }
    example.rpc.GetOrderResponse other = (example.rpc.GetOrderResponse) obj;

    if (hasOrder() != other.hasOrder()) return false;
    if (hasOrder()) {
      if (!getOrder()
          .equals(other.getOrder())) return false;
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
    if (hasOrder()) {
      hash = (37 * hash) + ORDER_FIELD_NUMBER;
      hash = (53 * hash) + getOrder().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static example.rpc.GetOrderResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrderResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrderResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static example.rpc.GetOrderResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrderResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static example.rpc.GetOrderResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrderResponse parseFrom(
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
  public static Builder newBuilder(example.rpc.GetOrderResponse prototype) {
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
   * Protobuf type {@code rpc.GetOrderResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:rpc.GetOrderResponse)
      example.rpc.GetOrderResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return example.rpc.Example.internal_static_rpc_GetOrderResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return example.rpc.Example.internal_static_rpc_GetOrderResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              example.rpc.GetOrderResponse.class, example.rpc.GetOrderResponse.Builder.class);
    }

    // Construct using example.rpc.GetOrderResponse.newBuilder()
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
      if (orderBuilder_ == null) {
        order_ = null;
      } else {
        order_ = null;
        orderBuilder_ = null;
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return example.rpc.Example.internal_static_rpc_GetOrderResponse_descriptor;
    }

    @java.lang.Override
    public example.rpc.GetOrderResponse getDefaultInstanceForType() {
      return example.rpc.GetOrderResponse.getDefaultInstance();
    }

    @java.lang.Override
    public example.rpc.GetOrderResponse build() {
      example.rpc.GetOrderResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public example.rpc.GetOrderResponse buildPartial() {
      example.rpc.GetOrderResponse result = new example.rpc.GetOrderResponse(this);
      if (orderBuilder_ == null) {
        result.order_ = order_;
      } else {
        result.order_ = orderBuilder_.build();
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
      if (other instanceof example.rpc.GetOrderResponse) {
        return mergeFrom((example.rpc.GetOrderResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(example.rpc.GetOrderResponse other) {
      if (other == example.rpc.GetOrderResponse.getDefaultInstance()) return this;
      if (other.hasOrder()) {
        mergeOrder(other.getOrder());
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
      example.rpc.GetOrderResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (example.rpc.GetOrderResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private example.rpc.Order order_;
    private com.google.protobuf.SingleFieldBuilderV3<
        example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder> orderBuilder_;
    /**
     * <code>.rpc.Order order = 1;</code>
     * @return Whether the order field is set.
     */
    public boolean hasOrder() {
      return orderBuilder_ != null || order_ != null;
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     * @return The order.
     */
    public example.rpc.Order getOrder() {
      if (orderBuilder_ == null) {
        return order_ == null ? example.rpc.Order.getDefaultInstance() : order_;
      } else {
        return orderBuilder_.getMessage();
      }
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public Builder setOrder(example.rpc.Order value) {
      if (orderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        order_ = value;
        onChanged();
      } else {
        orderBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public Builder setOrder(
        example.rpc.Order.Builder builderForValue) {
      if (orderBuilder_ == null) {
        order_ = builderForValue.build();
        onChanged();
      } else {
        orderBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public Builder mergeOrder(example.rpc.Order value) {
      if (orderBuilder_ == null) {
        if (order_ != null) {
          order_ =
            example.rpc.Order.newBuilder(order_).mergeFrom(value).buildPartial();
        } else {
          order_ = value;
        }
        onChanged();
      } else {
        orderBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public Builder clearOrder() {
      if (orderBuilder_ == null) {
        order_ = null;
        onChanged();
      } else {
        order_ = null;
        orderBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public example.rpc.Order.Builder getOrderBuilder() {
      
      onChanged();
      return getOrderFieldBuilder().getBuilder();
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    public example.rpc.OrderOrBuilder getOrderOrBuilder() {
      if (orderBuilder_ != null) {
        return orderBuilder_.getMessageOrBuilder();
      } else {
        return order_ == null ?
            example.rpc.Order.getDefaultInstance() : order_;
      }
    }
    /**
     * <code>.rpc.Order order = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder> 
        getOrderFieldBuilder() {
      if (orderBuilder_ == null) {
        orderBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder>(
                getOrder(),
                getParentForChildren(),
                isClean());
        order_ = null;
      }
      return orderBuilder_;
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


    // @@protoc_insertion_point(builder_scope:rpc.GetOrderResponse)
  }

  // @@protoc_insertion_point(class_scope:rpc.GetOrderResponse)
  private static final example.rpc.GetOrderResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new example.rpc.GetOrderResponse();
  }

  public static example.rpc.GetOrderResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GetOrderResponse>
      PARSER = new com.google.protobuf.AbstractParser<GetOrderResponse>() {
    @java.lang.Override
    public GetOrderResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new GetOrderResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<GetOrderResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GetOrderResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public example.rpc.GetOrderResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

