// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sample.proto

package sample.rpc;

public interface GetOrderResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rpc.GetOrderResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * The order information.
   * </pre>
   *
   * <code>.rpc.Order order = 1;</code>
   * @return Whether the order field is set.
   */
  boolean hasOrder();
  /**
   * <pre>
   * The order information.
   * </pre>
   *
   * <code>.rpc.Order order = 1;</code>
   * @return The order.
   */
  sample.rpc.Order getOrder();
  /**
   * <pre>
   * The order information.
   * </pre>
   *
   * <code>.rpc.Order order = 1;</code>
   */
  sample.rpc.OrderOrBuilder getOrderOrBuilder();
}