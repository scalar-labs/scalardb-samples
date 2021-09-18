package example.rpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * for Customer Service
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.38.0)",
    comments = "Source: example.proto")
public final class CustomerServiceGrpc {

  private CustomerServiceGrpc() {}

  public static final String SERVICE_NAME = "rpc.CustomerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<example.rpc.GetCustomerInfoRequest,
      example.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCustomerInfo",
      requestType = example.rpc.GetCustomerInfoRequest.class,
      responseType = example.rpc.GetCustomerInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.GetCustomerInfoRequest,
      example.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod() {
    io.grpc.MethodDescriptor<example.rpc.GetCustomerInfoRequest, example.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod;
    if ((getGetCustomerInfoMethod = CustomerServiceGrpc.getGetCustomerInfoMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getGetCustomerInfoMethod = CustomerServiceGrpc.getGetCustomerInfoMethod) == null) {
          CustomerServiceGrpc.getGetCustomerInfoMethod = getGetCustomerInfoMethod =
              io.grpc.MethodDescriptor.<example.rpc.GetCustomerInfoRequest, example.rpc.GetCustomerInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCustomerInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.GetCustomerInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.GetCustomerInfoResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("GetCustomerInfo"))
              .build();
        }
      }
    }
    return getGetCustomerInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.RepaymentRequest,
      com.google.protobuf.Empty> getRepaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Repayment",
      requestType = example.rpc.RepaymentRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.RepaymentRequest,
      com.google.protobuf.Empty> getRepaymentMethod() {
    io.grpc.MethodDescriptor<example.rpc.RepaymentRequest, com.google.protobuf.Empty> getRepaymentMethod;
    if ((getRepaymentMethod = CustomerServiceGrpc.getRepaymentMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getRepaymentMethod = CustomerServiceGrpc.getRepaymentMethod) == null) {
          CustomerServiceGrpc.getRepaymentMethod = getRepaymentMethod =
              io.grpc.MethodDescriptor.<example.rpc.RepaymentRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Repayment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.RepaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Repayment"))
              .build();
        }
      }
    }
    return getRepaymentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.JoinRequest,
      com.google.protobuf.Empty> getJoinMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Join",
      requestType = example.rpc.JoinRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.JoinRequest,
      com.google.protobuf.Empty> getJoinMethod() {
    io.grpc.MethodDescriptor<example.rpc.JoinRequest, com.google.protobuf.Empty> getJoinMethod;
    if ((getJoinMethod = CustomerServiceGrpc.getJoinMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getJoinMethod = CustomerServiceGrpc.getJoinMethod) == null) {
          CustomerServiceGrpc.getJoinMethod = getJoinMethod =
              io.grpc.MethodDescriptor.<example.rpc.JoinRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Join"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.JoinRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Join"))
              .build();
        }
      }
    }
    return getJoinMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.PaymentRequest,
      com.google.protobuf.Empty> getPaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Payment",
      requestType = example.rpc.PaymentRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.PaymentRequest,
      com.google.protobuf.Empty> getPaymentMethod() {
    io.grpc.MethodDescriptor<example.rpc.PaymentRequest, com.google.protobuf.Empty> getPaymentMethod;
    if ((getPaymentMethod = CustomerServiceGrpc.getPaymentMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getPaymentMethod = CustomerServiceGrpc.getPaymentMethod) == null) {
          CustomerServiceGrpc.getPaymentMethod = getPaymentMethod =
              io.grpc.MethodDescriptor.<example.rpc.PaymentRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Payment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.PaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Payment"))
              .build();
        }
      }
    }
    return getPaymentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.PrepareRequest,
      com.google.protobuf.Empty> getPrepareMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Prepare",
      requestType = example.rpc.PrepareRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.PrepareRequest,
      com.google.protobuf.Empty> getPrepareMethod() {
    io.grpc.MethodDescriptor<example.rpc.PrepareRequest, com.google.protobuf.Empty> getPrepareMethod;
    if ((getPrepareMethod = CustomerServiceGrpc.getPrepareMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getPrepareMethod = CustomerServiceGrpc.getPrepareMethod) == null) {
          CustomerServiceGrpc.getPrepareMethod = getPrepareMethod =
              io.grpc.MethodDescriptor.<example.rpc.PrepareRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Prepare"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.PrepareRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Prepare"))
              .build();
        }
      }
    }
    return getPrepareMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.ValidateRequest,
      com.google.protobuf.Empty> getValidateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Validate",
      requestType = example.rpc.ValidateRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.ValidateRequest,
      com.google.protobuf.Empty> getValidateMethod() {
    io.grpc.MethodDescriptor<example.rpc.ValidateRequest, com.google.protobuf.Empty> getValidateMethod;
    if ((getValidateMethod = CustomerServiceGrpc.getValidateMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getValidateMethod = CustomerServiceGrpc.getValidateMethod) == null) {
          CustomerServiceGrpc.getValidateMethod = getValidateMethod =
              io.grpc.MethodDescriptor.<example.rpc.ValidateRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Validate"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.ValidateRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Validate"))
              .build();
        }
      }
    }
    return getValidateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.CommitRequest,
      com.google.protobuf.Empty> getCommitMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Commit",
      requestType = example.rpc.CommitRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.CommitRequest,
      com.google.protobuf.Empty> getCommitMethod() {
    io.grpc.MethodDescriptor<example.rpc.CommitRequest, com.google.protobuf.Empty> getCommitMethod;
    if ((getCommitMethod = CustomerServiceGrpc.getCommitMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getCommitMethod = CustomerServiceGrpc.getCommitMethod) == null) {
          CustomerServiceGrpc.getCommitMethod = getCommitMethod =
              io.grpc.MethodDescriptor.<example.rpc.CommitRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Commit"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.CommitRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Commit"))
              .build();
        }
      }
    }
    return getCommitMethod;
  }

  private static volatile io.grpc.MethodDescriptor<example.rpc.RollbackRequest,
      com.google.protobuf.Empty> getRollbackMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Rollback",
      requestType = example.rpc.RollbackRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<example.rpc.RollbackRequest,
      com.google.protobuf.Empty> getRollbackMethod() {
    io.grpc.MethodDescriptor<example.rpc.RollbackRequest, com.google.protobuf.Empty> getRollbackMethod;
    if ((getRollbackMethod = CustomerServiceGrpc.getRollbackMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getRollbackMethod = CustomerServiceGrpc.getRollbackMethod) == null) {
          CustomerServiceGrpc.getRollbackMethod = getRollbackMethod =
              io.grpc.MethodDescriptor.<example.rpc.RollbackRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Rollback"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  example.rpc.RollbackRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Rollback"))
              .build();
        }
      }
    }
    return getRollbackMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CustomerServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CustomerServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CustomerServiceStub>() {
        @java.lang.Override
        public CustomerServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CustomerServiceStub(channel, callOptions);
        }
      };
    return CustomerServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CustomerServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CustomerServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CustomerServiceBlockingStub>() {
        @java.lang.Override
        public CustomerServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CustomerServiceBlockingStub(channel, callOptions);
        }
      };
    return CustomerServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CustomerServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CustomerServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<CustomerServiceFutureStub>() {
        @java.lang.Override
        public CustomerServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new CustomerServiceFutureStub(channel, callOptions);
        }
      };
    return CustomerServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * for Customer Service
   * </pre>
   */
  public static abstract class CustomerServiceImplBase implements io.grpc.BindableService {

    /**
     * <pre>
     * Get a customer information
     * </pre>
     */
    public void getCustomerInfo(example.rpc.GetCustomerInfoRequest request,
        io.grpc.stub.StreamObserver<example.rpc.GetCustomerInfoResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCustomerInfoMethod(), responseObserver);
    }

    /**
     * <pre>
     * Credit card repayment
     * </pre>
     */
    public void repayment(example.rpc.RepaymentRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRepaymentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Join the transaction
     * </pre>
     */
    public void join(example.rpc.JoinRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getJoinMethod(), responseObserver);
    }

    /**
     * <pre>
     * Credit card payment
     * </pre>
     */
    public void payment(example.rpc.PaymentRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPaymentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Prepare the transaction
     * </pre>
     */
    public void prepare(example.rpc.PrepareRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPrepareMethod(), responseObserver);
    }

    /**
     * <pre>
     * Validate the transaction
     * </pre>
     */
    public void validate(example.rpc.ValidateRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateMethod(), responseObserver);
    }

    /**
     * <pre>
     * Commit the transaction
     * </pre>
     */
    public void commit(example.rpc.CommitRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCommitMethod(), responseObserver);
    }

    /**
     * <pre>
     * Rollback the transaction
     * </pre>
     */
    public void rollback(example.rpc.RollbackRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRollbackMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getGetCustomerInfoMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.GetCustomerInfoRequest,
                example.rpc.GetCustomerInfoResponse>(
                  this, METHODID_GET_CUSTOMER_INFO)))
          .addMethod(
            getRepaymentMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.RepaymentRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_REPAYMENT)))
          .addMethod(
            getJoinMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.JoinRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_JOIN)))
          .addMethod(
            getPaymentMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.PaymentRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_PAYMENT)))
          .addMethod(
            getPrepareMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.PrepareRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_PREPARE)))
          .addMethod(
            getValidateMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.ValidateRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_VALIDATE)))
          .addMethod(
            getCommitMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.CommitRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_COMMIT)))
          .addMethod(
            getRollbackMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                example.rpc.RollbackRequest,
                com.google.protobuf.Empty>(
                  this, METHODID_ROLLBACK)))
          .build();
    }
  }

  /**
   * <pre>
   * for Customer Service
   * </pre>
   */
  public static final class CustomerServiceStub extends io.grpc.stub.AbstractAsyncStub<CustomerServiceStub> {
    private CustomerServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CustomerServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CustomerServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get a customer information
     * </pre>
     */
    public void getCustomerInfo(example.rpc.GetCustomerInfoRequest request,
        io.grpc.stub.StreamObserver<example.rpc.GetCustomerInfoResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCustomerInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Credit card repayment
     * </pre>
     */
    public void repayment(example.rpc.RepaymentRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRepaymentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Join the transaction
     * </pre>
     */
    public void join(example.rpc.JoinRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Credit card payment
     * </pre>
     */
    public void payment(example.rpc.PaymentRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPaymentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Prepare the transaction
     * </pre>
     */
    public void prepare(example.rpc.PrepareRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPrepareMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Validate the transaction
     * </pre>
     */
    public void validate(example.rpc.ValidateRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Commit the transaction
     * </pre>
     */
    public void commit(example.rpc.CommitRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCommitMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Rollback the transaction
     * </pre>
     */
    public void rollback(example.rpc.RollbackRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRollbackMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * <pre>
   * for Customer Service
   * </pre>
   */
  public static final class CustomerServiceBlockingStub extends io.grpc.stub.AbstractBlockingStub<CustomerServiceBlockingStub> {
    private CustomerServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CustomerServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CustomerServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get a customer information
     * </pre>
     */
    public example.rpc.GetCustomerInfoResponse getCustomerInfo(example.rpc.GetCustomerInfoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCustomerInfoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Credit card repayment
     * </pre>
     */
    public com.google.protobuf.Empty repayment(example.rpc.RepaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRepaymentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Join the transaction
     * </pre>
     */
    public com.google.protobuf.Empty join(example.rpc.JoinRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getJoinMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Credit card payment
     * </pre>
     */
    public com.google.protobuf.Empty payment(example.rpc.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPaymentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Prepare the transaction
     * </pre>
     */
    public com.google.protobuf.Empty prepare(example.rpc.PrepareRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPrepareMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Validate the transaction
     * </pre>
     */
    public com.google.protobuf.Empty validate(example.rpc.ValidateRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Commit the transaction
     * </pre>
     */
    public com.google.protobuf.Empty commit(example.rpc.CommitRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCommitMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Rollback the transaction
     * </pre>
     */
    public com.google.protobuf.Empty rollback(example.rpc.RollbackRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRollbackMethod(), getCallOptions(), request);
    }
  }

  /**
   * <pre>
   * for Customer Service
   * </pre>
   */
  public static final class CustomerServiceFutureStub extends io.grpc.stub.AbstractFutureStub<CustomerServiceFutureStub> {
    private CustomerServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CustomerServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new CustomerServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Get a customer information
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<example.rpc.GetCustomerInfoResponse> getCustomerInfo(
        example.rpc.GetCustomerInfoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCustomerInfoMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Credit card repayment
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> repayment(
        example.rpc.RepaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRepaymentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Join the transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> join(
        example.rpc.JoinRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getJoinMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Credit card payment
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> payment(
        example.rpc.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPaymentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Prepare the transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> prepare(
        example.rpc.PrepareRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPrepareMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Validate the transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> validate(
        example.rpc.ValidateRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Commit the transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> commit(
        example.rpc.CommitRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCommitMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Rollback the transaction
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> rollback(
        example.rpc.RollbackRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRollbackMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_CUSTOMER_INFO = 0;
  private static final int METHODID_REPAYMENT = 1;
  private static final int METHODID_JOIN = 2;
  private static final int METHODID_PAYMENT = 3;
  private static final int METHODID_PREPARE = 4;
  private static final int METHODID_VALIDATE = 5;
  private static final int METHODID_COMMIT = 6;
  private static final int METHODID_ROLLBACK = 7;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CustomerServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CustomerServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_CUSTOMER_INFO:
          serviceImpl.getCustomerInfo((example.rpc.GetCustomerInfoRequest) request,
              (io.grpc.stub.StreamObserver<example.rpc.GetCustomerInfoResponse>) responseObserver);
          break;
        case METHODID_REPAYMENT:
          serviceImpl.repayment((example.rpc.RepaymentRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_JOIN:
          serviceImpl.join((example.rpc.JoinRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_PAYMENT:
          serviceImpl.payment((example.rpc.PaymentRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_PREPARE:
          serviceImpl.prepare((example.rpc.PrepareRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_VALIDATE:
          serviceImpl.validate((example.rpc.ValidateRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_COMMIT:
          serviceImpl.commit((example.rpc.CommitRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_ROLLBACK:
          serviceImpl.rollback((example.rpc.RollbackRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CustomerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CustomerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return example.rpc.Example.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CustomerService");
    }
  }

  private static final class CustomerServiceFileDescriptorSupplier
      extends CustomerServiceBaseDescriptorSupplier {
    CustomerServiceFileDescriptorSupplier() {}
  }

  private static final class CustomerServiceMethodDescriptorSupplier
      extends CustomerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CustomerServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CustomerServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CustomerServiceFileDescriptorSupplier())
              .addMethod(getGetCustomerInfoMethod())
              .addMethod(getRepaymentMethod())
              .addMethod(getJoinMethod())
              .addMethod(getPaymentMethod())
              .addMethod(getPrepareMethod())
              .addMethod(getValidateMethod())
              .addMethod(getCommitMethod())
              .addMethod(getRollbackMethod())
              .build();
        }
      }
    }
    return result;
  }
}
