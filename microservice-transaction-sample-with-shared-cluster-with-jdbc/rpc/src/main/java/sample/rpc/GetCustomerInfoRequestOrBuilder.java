// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sample.proto

package sample.rpc;

public interface GetCustomerInfoRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rpc.GetCustomerInfoRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The global transaction ID.
   * </pre>
   *
   * <code>optional string transaction_id = 1;</code>
   * @return Whether the transactionId field is set.
   */
  boolean hasTransactionId();
  /**
   * <pre>
   * The global transaction ID.
   * </pre>
   *
   * <code>optional string transaction_id = 1;</code>
   * @return The transactionId.
   */
  java.lang.String getTransactionId();
  /**
   * <pre>
   * The global transaction ID.
   * </pre>
   *
   * <code>optional string transaction_id = 1;</code>
   * @return The bytes for transactionId.
   */
  com.google.protobuf.ByteString
      getTransactionIdBytes();

  /**
   * <pre>
   * The customer ID of the customer.
   * </pre>
   *
   * <code>int32 customer_id = 2;</code>
   * @return The customerId.
   */
  int getCustomerId();
}