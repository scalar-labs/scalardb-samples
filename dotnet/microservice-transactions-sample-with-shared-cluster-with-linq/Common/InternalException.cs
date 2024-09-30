using Grpc.Core;

namespace MicroserviceTransactionsSample.Common;

public class InternalException : RpcException
{
    public InternalException(string message, Exception? innerEx)
        : base(new Status(StatusCode.Internal, message, innerEx))
    { }
}
