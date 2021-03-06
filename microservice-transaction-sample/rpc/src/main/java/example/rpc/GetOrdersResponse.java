// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: example.proto

package example.rpc;

/**
 * Protobuf type {@code rpc.GetOrdersResponse}
 */
public final class GetOrdersResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:rpc.GetOrdersResponse)
    GetOrdersResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use GetOrdersResponse.newBuilder() to construct.
  private GetOrdersResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private GetOrdersResponse() {
    order_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new GetOrdersResponse();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private GetOrdersResponse(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
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
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              order_ = new java.util.ArrayList<example.rpc.Order>();
              mutable_bitField0_ |= 0x00000001;
            }
            order_.add(
                input.readMessage(example.rpc.Order.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        order_ = java.util.Collections.unmodifiableList(order_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return example.rpc.Example.internal_static_rpc_GetOrdersResponse_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return example.rpc.Example.internal_static_rpc_GetOrdersResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            example.rpc.GetOrdersResponse.class, example.rpc.GetOrdersResponse.Builder.class);
  }

  public static final int ORDER_FIELD_NUMBER = 1;
  private java.util.List<example.rpc.Order> order_;
  /**
   * <code>repeated .rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public java.util.List<example.rpc.Order> getOrderList() {
    return order_;
  }
  /**
   * <code>repeated .rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public java.util.List<? extends example.rpc.OrderOrBuilder> 
      getOrderOrBuilderList() {
    return order_;
  }
  /**
   * <code>repeated .rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public int getOrderCount() {
    return order_.size();
  }
  /**
   * <code>repeated .rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public example.rpc.Order getOrder(int index) {
    return order_.get(index);
  }
  /**
   * <code>repeated .rpc.Order order = 1;</code>
   */
  @java.lang.Override
  public example.rpc.OrderOrBuilder getOrderOrBuilder(
      int index) {
    return order_.get(index);
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
    for (int i = 0; i < order_.size(); i++) {
      output.writeMessage(1, order_.get(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < order_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, order_.get(i));
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
    if (!(obj instanceof example.rpc.GetOrdersResponse)) {
      return super.equals(obj);
    }
    example.rpc.GetOrdersResponse other = (example.rpc.GetOrdersResponse) obj;

    if (!getOrderList()
        .equals(other.getOrderList())) return false;
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
    if (getOrderCount() > 0) {
      hash = (37 * hash) + ORDER_FIELD_NUMBER;
      hash = (53 * hash) + getOrderList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static example.rpc.GetOrdersResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrdersResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static example.rpc.GetOrdersResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static example.rpc.GetOrdersResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrdersResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static example.rpc.GetOrdersResponse parseFrom(
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
  public static Builder newBuilder(example.rpc.GetOrdersResponse prototype) {
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
   * Protobuf type {@code rpc.GetOrdersResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:rpc.GetOrdersResponse)
      example.rpc.GetOrdersResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return example.rpc.Example.internal_static_rpc_GetOrdersResponse_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return example.rpc.Example.internal_static_rpc_GetOrdersResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              example.rpc.GetOrdersResponse.class, example.rpc.GetOrdersResponse.Builder.class);
    }

    // Construct using example.rpc.GetOrdersResponse.newBuilder()
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
        getOrderFieldBuilder();
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      if (orderBuilder_ == null) {
        order_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        orderBuilder_.clear();
      }
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return example.rpc.Example.internal_static_rpc_GetOrdersResponse_descriptor;
    }

    @java.lang.Override
    public example.rpc.GetOrdersResponse getDefaultInstanceForType() {
      return example.rpc.GetOrdersResponse.getDefaultInstance();
    }

    @java.lang.Override
    public example.rpc.GetOrdersResponse build() {
      example.rpc.GetOrdersResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public example.rpc.GetOrdersResponse buildPartial() {
      example.rpc.GetOrdersResponse result = new example.rpc.GetOrdersResponse(this);
      int from_bitField0_ = bitField0_;
      if (orderBuilder_ == null) {
        if (((bitField0_ & 0x00000001) != 0)) {
          order_ = java.util.Collections.unmodifiableList(order_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
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
      if (other instanceof example.rpc.GetOrdersResponse) {
        return mergeFrom((example.rpc.GetOrdersResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(example.rpc.GetOrdersResponse other) {
      if (other == example.rpc.GetOrdersResponse.getDefaultInstance()) return this;
      if (orderBuilder_ == null) {
        if (!other.order_.isEmpty()) {
          if (order_.isEmpty()) {
            order_ = other.order_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureOrderIsMutable();
            order_.addAll(other.order_);
          }
          onChanged();
        }
      } else {
        if (!other.order_.isEmpty()) {
          if (orderBuilder_.isEmpty()) {
            orderBuilder_.dispose();
            orderBuilder_ = null;
            order_ = other.order_;
            bitField0_ = (bitField0_ & ~0x00000001);
            orderBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getOrderFieldBuilder() : null;
          } else {
            orderBuilder_.addAllMessages(other.order_);
          }
        }
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
      example.rpc.GetOrdersResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (example.rpc.GetOrdersResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<example.rpc.Order> order_ =
      java.util.Collections.emptyList();
    private void ensureOrderIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        order_ = new java.util.ArrayList<example.rpc.Order>(order_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder> orderBuilder_;

    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public java.util.List<example.rpc.Order> getOrderList() {
      if (orderBuilder_ == null) {
        return java.util.Collections.unmodifiableList(order_);
      } else {
        return orderBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public int getOrderCount() {
      if (orderBuilder_ == null) {
        return order_.size();
      } else {
        return orderBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public example.rpc.Order getOrder(int index) {
      if (orderBuilder_ == null) {
        return order_.get(index);
      } else {
        return orderBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder setOrder(
        int index, example.rpc.Order value) {
      if (orderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderIsMutable();
        order_.set(index, value);
        onChanged();
      } else {
        orderBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder setOrder(
        int index, example.rpc.Order.Builder builderForValue) {
      if (orderBuilder_ == null) {
        ensureOrderIsMutable();
        order_.set(index, builderForValue.build());
        onChanged();
      } else {
        orderBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder addOrder(example.rpc.Order value) {
      if (orderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderIsMutable();
        order_.add(value);
        onChanged();
      } else {
        orderBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder addOrder(
        int index, example.rpc.Order value) {
      if (orderBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureOrderIsMutable();
        order_.add(index, value);
        onChanged();
      } else {
        orderBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder addOrder(
        example.rpc.Order.Builder builderForValue) {
      if (orderBuilder_ == null) {
        ensureOrderIsMutable();
        order_.add(builderForValue.build());
        onChanged();
      } else {
        orderBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder addOrder(
        int index, example.rpc.Order.Builder builderForValue) {
      if (orderBuilder_ == null) {
        ensureOrderIsMutable();
        order_.add(index, builderForValue.build());
        onChanged();
      } else {
        orderBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder addAllOrder(
        java.lang.Iterable<? extends example.rpc.Order> values) {
      if (orderBuilder_ == null) {
        ensureOrderIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, order_);
        onChanged();
      } else {
        orderBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder clearOrder() {
      if (orderBuilder_ == null) {
        order_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        orderBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public Builder removeOrder(int index) {
      if (orderBuilder_ == null) {
        ensureOrderIsMutable();
        order_.remove(index);
        onChanged();
      } else {
        orderBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public example.rpc.Order.Builder getOrderBuilder(
        int index) {
      return getOrderFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public example.rpc.OrderOrBuilder getOrderOrBuilder(
        int index) {
      if (orderBuilder_ == null) {
        return order_.get(index);  } else {
        return orderBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public java.util.List<? extends example.rpc.OrderOrBuilder> 
         getOrderOrBuilderList() {
      if (orderBuilder_ != null) {
        return orderBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(order_);
      }
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public example.rpc.Order.Builder addOrderBuilder() {
      return getOrderFieldBuilder().addBuilder(
          example.rpc.Order.getDefaultInstance());
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public example.rpc.Order.Builder addOrderBuilder(
        int index) {
      return getOrderFieldBuilder().addBuilder(
          index, example.rpc.Order.getDefaultInstance());
    }
    /**
     * <code>repeated .rpc.Order order = 1;</code>
     */
    public java.util.List<example.rpc.Order.Builder> 
         getOrderBuilderList() {
      return getOrderFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder> 
        getOrderFieldBuilder() {
      if (orderBuilder_ == null) {
        orderBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            example.rpc.Order, example.rpc.Order.Builder, example.rpc.OrderOrBuilder>(
                order_,
                ((bitField0_ & 0x00000001) != 0),
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


    // @@protoc_insertion_point(builder_scope:rpc.GetOrdersResponse)
  }

  // @@protoc_insertion_point(class_scope:rpc.GetOrdersResponse)
  private static final example.rpc.GetOrdersResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new example.rpc.GetOrdersResponse();
  }

  public static example.rpc.GetOrdersResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<GetOrdersResponse>
      PARSER = new com.google.protobuf.AbstractParser<GetOrdersResponse>() {
    @java.lang.Override
    public GetOrdersResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new GetOrdersResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<GetOrdersResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<GetOrdersResponse> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public example.rpc.GetOrdersResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

