// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: scalardb.proto

package com.scalar.db.rpc;

public interface MutateRequestOrBuilder extends
    // @@protoc_insertion_point(interface_extends:rpc.MutateRequest)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>repeated .rpc.Mutation mutation = 1;</code>
   */
  java.util.List<com.scalar.db.rpc.Mutation> 
      getMutationList();
  /**
   * <code>repeated .rpc.Mutation mutation = 1;</code>
   */
  com.scalar.db.rpc.Mutation getMutation(int index);
  /**
   * <code>repeated .rpc.Mutation mutation = 1;</code>
   */
  int getMutationCount();
  /**
   * <code>repeated .rpc.Mutation mutation = 1;</code>
   */
  java.util.List<? extends com.scalar.db.rpc.MutationOrBuilder> 
      getMutationOrBuilderList();
  /**
   * <code>repeated .rpc.Mutation mutation = 1;</code>
   */
  com.scalar.db.rpc.MutationOrBuilder getMutationOrBuilder(
      int index);
}