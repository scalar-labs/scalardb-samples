package sample.rpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Customer Service.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.60.1)",
    comments = "Source: sample.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class CustomerServiceGrpc {

  private CustomerServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "rpc.CustomerService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<sample.rpc.GetCustomerInfoRequest,
      sample.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCustomerInfo",
      requestType = sample.rpc.GetCustomerInfoRequest.class,
      responseType = sample.rpc.GetCustomerInfoResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<sample.rpc.GetCustomerInfoRequest,
      sample.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod() {
    io.grpc.MethodDescriptor<sample.rpc.GetCustomerInfoRequest, sample.rpc.GetCustomerInfoResponse> getGetCustomerInfoMethod;
    if ((getGetCustomerInfoMethod = CustomerServiceGrpc.getGetCustomerInfoMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getGetCustomerInfoMethod = CustomerServiceGrpc.getGetCustomerInfoMethod) == null) {
          CustomerServiceGrpc.getGetCustomerInfoMethod = getGetCustomerInfoMethod =
              io.grpc.MethodDescriptor.<sample.rpc.GetCustomerInfoRequest, sample.rpc.GetCustomerInfoResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCustomerInfo"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.GetCustomerInfoRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.GetCustomerInfoResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("GetCustomerInfo"))
              .build();
        }
      }
    }
    return getGetCustomerInfoMethod;
  }

  private static volatile io.grpc.MethodDescriptor<sample.rpc.PaymentRequest,
      sample.rpc.PaymentResponse> getPaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Payment",
      requestType = sample.rpc.PaymentRequest.class,
      responseType = sample.rpc.PaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<sample.rpc.PaymentRequest,
      sample.rpc.PaymentResponse> getPaymentMethod() {
    io.grpc.MethodDescriptor<sample.rpc.PaymentRequest, sample.rpc.PaymentResponse> getPaymentMethod;
    if ((getPaymentMethod = CustomerServiceGrpc.getPaymentMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getPaymentMethod = CustomerServiceGrpc.getPaymentMethod) == null) {
          CustomerServiceGrpc.getPaymentMethod = getPaymentMethod =
              io.grpc.MethodDescriptor.<sample.rpc.PaymentRequest, sample.rpc.PaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Payment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.PaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.PaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Payment"))
              .build();
        }
      }
    }
    return getPaymentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<sample.rpc.RepaymentRequest,
      sample.rpc.RepaymentResponse> getRepaymentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Repayment",
      requestType = sample.rpc.RepaymentRequest.class,
      responseType = sample.rpc.RepaymentResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<sample.rpc.RepaymentRequest,
      sample.rpc.RepaymentResponse> getRepaymentMethod() {
    io.grpc.MethodDescriptor<sample.rpc.RepaymentRequest, sample.rpc.RepaymentResponse> getRepaymentMethod;
    if ((getRepaymentMethod = CustomerServiceGrpc.getRepaymentMethod) == null) {
      synchronized (CustomerServiceGrpc.class) {
        if ((getRepaymentMethod = CustomerServiceGrpc.getRepaymentMethod) == null) {
          CustomerServiceGrpc.getRepaymentMethod = getRepaymentMethod =
              io.grpc.MethodDescriptor.<sample.rpc.RepaymentRequest, sample.rpc.RepaymentResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Repayment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.RepaymentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  sample.rpc.RepaymentResponse.getDefaultInstance()))
              .setSchemaDescriptor(new CustomerServiceMethodDescriptorSupplier("Repayment"))
              .build();
        }
      }
    }
    return getRepaymentMethod;
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
   * Customer Service.
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Get customer information. This function processing operations can be used in both a normal
     * transaction and a global transaction.
     * </pre>
     */
    default void getCustomerInfo(sample.rpc.GetCustomerInfoRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.GetCustomerInfoResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCustomerInfoMethod(), responseObserver);
    }

    /**
     * <pre>
     * Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
     * </pre>
     */
    default void payment(sample.rpc.PaymentRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.PaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPaymentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Credit card repayment.
     * </pre>
     */
    default void repayment(sample.rpc.RepaymentRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.RepaymentResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRepaymentMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service CustomerService.
   * <pre>
   * Customer Service.
   * </pre>
   */
  public static abstract class CustomerServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return CustomerServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service CustomerService.
   * <pre>
   * Customer Service.
   * </pre>
   */
  public static final class CustomerServiceStub
      extends io.grpc.stub.AbstractAsyncStub<CustomerServiceStub> {
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
     * Get customer information. This function processing operations can be used in both a normal
     * transaction and a global transaction.
     * </pre>
     */
    public void getCustomerInfo(sample.rpc.GetCustomerInfoRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.GetCustomerInfoResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCustomerInfoMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
     * </pre>
     */
    public void payment(sample.rpc.PaymentRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.PaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPaymentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Credit card repayment.
     * </pre>
     */
    public void repayment(sample.rpc.RepaymentRequest request,
        io.grpc.stub.StreamObserver<sample.rpc.RepaymentResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRepaymentMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service CustomerService.
   * <pre>
   * Customer Service.
   * </pre>
   */
  public static final class CustomerServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<CustomerServiceBlockingStub> {
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
     * Get customer information. This function processing operations can be used in both a normal
     * transaction and a global transaction.
     * </pre>
     */
    public sample.rpc.GetCustomerInfoResponse getCustomerInfo(sample.rpc.GetCustomerInfoRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCustomerInfoMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
     * </pre>
     */
    public sample.rpc.PaymentResponse payment(sample.rpc.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPaymentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Credit card repayment.
     * </pre>
     */
    public sample.rpc.RepaymentResponse repayment(sample.rpc.RepaymentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRepaymentMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service CustomerService.
   * <pre>
   * Customer Service.
   * </pre>
   */
  public static final class CustomerServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<CustomerServiceFutureStub> {
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
     * Get customer information. This function processing operations can be used in both a normal
     * transaction and a global transaction.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<sample.rpc.GetCustomerInfoResponse> getCustomerInfo(
        sample.rpc.GetCustomerInfoRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCustomerInfoMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<sample.rpc.PaymentResponse> payment(
        sample.rpc.PaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPaymentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Credit card repayment.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<sample.rpc.RepaymentResponse> repayment(
        sample.rpc.RepaymentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRepaymentMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_CUSTOMER_INFO = 0;
  private static final int METHODID_PAYMENT = 1;
  private static final int METHODID_REPAYMENT = 2;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_CUSTOMER_INFO:
          serviceImpl.getCustomerInfo((sample.rpc.GetCustomerInfoRequest) request,
              (io.grpc.stub.StreamObserver<sample.rpc.GetCustomerInfoResponse>) responseObserver);
          break;
        case METHODID_PAYMENT:
          serviceImpl.payment((sample.rpc.PaymentRequest) request,
              (io.grpc.stub.StreamObserver<sample.rpc.PaymentResponse>) responseObserver);
          break;
        case METHODID_REPAYMENT:
          serviceImpl.repayment((sample.rpc.RepaymentRequest) request,
              (io.grpc.stub.StreamObserver<sample.rpc.RepaymentResponse>) responseObserver);
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

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetCustomerInfoMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              sample.rpc.GetCustomerInfoRequest,
              sample.rpc.GetCustomerInfoResponse>(
                service, METHODID_GET_CUSTOMER_INFO)))
        .addMethod(
          getPaymentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              sample.rpc.PaymentRequest,
              sample.rpc.PaymentResponse>(
                service, METHODID_PAYMENT)))
        .addMethod(
          getRepaymentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              sample.rpc.RepaymentRequest,
              sample.rpc.RepaymentResponse>(
                service, METHODID_REPAYMENT)))
        .build();
  }

  private static abstract class CustomerServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CustomerServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return sample.rpc.Sample.getDescriptor();
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
    private final java.lang.String methodName;

    CustomerServiceMethodDescriptorSupplier(java.lang.String methodName) {
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
              .addMethod(getPaymentMethod())
              .addMethod(getRepaymentMethod())
              .build();
        }
      }
    }
    return result;
  }
}
