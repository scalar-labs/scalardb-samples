using Grpc.Core;

namespace MicroserviceTransactionsSample.Common;

public class FailedPreconditionException : RpcException
{
    public FailedPreconditionException(string message)
        : base(new Status(StatusCode.FailedPrecondition, message))
    { }
}
