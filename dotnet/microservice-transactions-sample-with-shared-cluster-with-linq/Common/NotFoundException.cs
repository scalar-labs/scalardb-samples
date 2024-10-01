using Grpc.Core;

namespace MicroserviceTransactionsSample.Common;

public class NotFoundException : RpcException
{
    public NotFoundException(string message)
        : base(new Status(StatusCode.NotFound, message))
    { }
}
