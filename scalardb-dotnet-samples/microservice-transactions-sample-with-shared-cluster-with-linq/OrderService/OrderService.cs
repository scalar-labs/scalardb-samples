using Grpc.Core;
using MicroserviceTransactionsSample.Rpc;
using MicroserviceTransactionsSample.Common;
using MicroserviceTransactionsSample.Common.OrderService;
using ScalarDB.Client.Exceptions;
using static MicroserviceTransactionsSample.Rpc.OrderService;
using static MicroserviceTransactionsSample.Rpc.CustomerService;
using Order = MicroserviceTransactionsSample.Common.OrderService.Order;
using Statement = MicroserviceTransactionsSample.Common.OrderService.Statement;

namespace MicroserviceTransactionsSample.OrderService;

public class OrderService : OrderServiceBase
{
    private readonly OrderDbContext _db;
    private readonly CustomerServiceClient _customerClient;
    private readonly ILogger<OrderService> _logger;

    public OrderService(OrderDbContext db,
                        CustomerServiceClient customerClient,
                        ILogger<OrderService> logger)
    {
        _db = db;
        _customerClient = customerClient;
        _logger = logger;
    }

    /// <summary>
    /// Place an order. It's a transaction that spans OrderService and CustomerService 
    /// </summary>
    public override async Task<PlaceOrderResponse> PlaceOrder(PlaceOrderRequest request,
                                                              ServerCallContext context)
        => await execOperationsAsCoordinator(
               "Placing an order",
               async () =>
               {
                   var orderId = Guid.NewGuid().ToString();

                   // Insert the order info into the orders table
                   var order = new Order
                               {
                                   Id = orderId,
                                   CustomerId = request.CustomerId,
                                   Timestamp = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds()
                               };
                   await _db.Orders.AddAsync(order);

                   var amount = 0;
                   foreach (var itemOrder in request.ItemOrder)
                   {
                       // Insert the order statement into the statements table
                       var statement = new Statement
                                       {
                                           OrderId = orderId,
                                           ItemId = itemOrder.ItemId,
                                           Count = itemOrder.Count
                                       };
                       await _db.Statements.AddAsync(statement);

                       // Retrieve the item info from the items table
                       var item = _db.Items.FirstOrDefault(i => i.Id == itemOrder.ItemId);
                       if (item == null)
                           throw new NotFoundException("Item not found");

                       // Calculate the total amount
                       amount += item.Price * itemOrder.Count;
                   }

                   // Call the payment endpoint of Customer service
                   var paymentRequest = new PaymentRequest
                                        {
                                            TransactionId = _db.CurrentTransactionId,
                                            CustomerId = request.CustomerId,
                                            Amount = amount
                                        };
                   await _customerClient.PaymentAsync(paymentRequest);

                   // Return the order id
                   return new PlaceOrderResponse { OrderId = orderId };
               });

    /// <summary>
    /// Get Order information by order ID
    /// </summary>
    public override async Task<GetOrderResponse> GetOrder(GetOrderRequest request,
                                                          ServerCallContext context)
        => await execOperationsAsCoordinator(
               "Getting an order",
               async () =>
               {
                   // Retrieve the order info for the specified order ID
                   var order = _db.Orders.FirstOrDefault(o => o.Id == request.OrderId);
                   if (order == null)
                       throw new NotFoundException("Order not found");

                   // Get the customer name from the Customer service
                   var customerName = await getCustomerName(_db.CurrentTransactionId,
                                                            order.CustomerId);

                   // Make an order protobuf to return
                   var rpcOrder = getRpcOrder(order, customerName);
                   return new GetOrderResponse { Order = rpcOrder };
               });

    /// <summary>
    /// Get Order information by customer ID 
    /// </summary>
    public override async Task<GetOrdersResponse> GetOrders(GetOrdersRequest request,
                                                            ServerCallContext context)
        => await execOperationsAsCoordinator(
               "Getting orders",
               async () =>
               {
                   // Get the customer name from the Customer service
                   var customerName = await getCustomerName(_db.CurrentTransactionId,
                                                            request.CustomerId);

                   // Retrieve the order info for the specified customer ID
                   var response = new GetOrdersResponse();
                   var orders = _db.Orders.Where(order => order.CustomerId == request.CustomerId);

                   foreach (var order in orders)
                   {
                       // Make an order protobuf to return
                       var rpcOrder = getRpcOrder(order, customerName);
                       response.Order.Add(rpcOrder);
                   }

                   return response;
               });

    private Rpc.Order getRpcOrder(Order order, string customerName)
    {
        var rpcOrder = new Rpc.Order
                       {
                           OrderId = order.Id,
                           CustomerId = order.CustomerId,
                           CustomerName = customerName,
                           Timestamp = order.Timestamp
                       };

        var total = 0;

        // Create statements for the order ID with data from Statements and Items tables
        var rpcStatements = from statement in _db.Statements
                            join item in _db.Items
                                on statement.ItemId equals item.Id into items
                            from statItem in items.DefaultIfEmpty(Item.EmptyItem)
                            where statement.OrderId == order.Id
                            select new Rpc.Statement
                                   {
                                       ItemId = statItem.Id,
                                       ItemName = statItem.Name,
                                       Price = statItem.Price,
                                       Count = statement.Count,
                                       Total = statItem.Price * statement.Count
                                   };

        foreach (var rpcStatement in rpcStatements)
        {
            if (rpcStatement.ItemId == Item.EmptyItem.Id)
                throw new NotFoundException($"Item not found");

            rpcOrder.Statement.Add(rpcStatement);

            total += rpcStatement.Total;
        }

        rpcOrder.Total = total;
        return rpcOrder;
    }

    private async Task<string> getCustomerName(string transactionId, int customerId)
    {
        var request = new GetCustomerInfoRequest
                      {
                          TransactionId = transactionId,
                          CustomerId = customerId
                      };
        var customerInfo = await _customerClient.GetCustomerInfoAsync(request);

        return customerInfo.Name;
    }

    private async Task<T> execOperationsAsCoordinator<T>(string funcName,
                                                         Func<Task<T>> operations)
    {
        var retryCount = 0;
        Exception? lastException = null;

        while (true)
        {
            if (retryCount++ > 0)
            {
                // Retry the transaction three times maximum.
                if (retryCount >= 3)
                {
                    // If the transaction failed three times, return an error.
                    _logger.LogError(lastException, "{funcName} failed", funcName);

                    if (lastException is RpcException)
                        throw lastException;
                    else
                        throw new InternalException(funcName + " failed", lastException);
                }

                _logger.LogWarning(lastException, "Retrying the transaction after 100 milliseconds: {funcName}", funcName);

                await Task.Delay(TimeSpan.FromMilliseconds(100));
            }

            try
            {
                // Begin a transaction
                await _db.BeginTransactionAsync();

                // Execute operations
                var response = await operations();

                // Commit the transaction
                await _db.CommitTransactionAsync();

                // Return the response
                return response;
            }
            catch (UnknownTransactionStatusException ex)
            {
                // If you catch `UnknownTransactionStatusException`, it indicates that the status of the
                // transaction, whether it has succeeded or not, is unknown. In such a case, you need to
                // check if the transaction is committed successfully or not and retry it if it failed.
                // How to identify a transaction status is delegated to users

                _logger.LogError(ex, "{funcName} failed", funcName);
                throw new InternalException(funcName + " failed", ex);
            }
            catch (RpcException ex)
                when (ex.StatusCode is StatusCode.NotFound or StatusCode.FailedPrecondition)
            {
                // For `NOT_FOUND` and `FAILED_PRECONDITION` gRPC errors, you cannot retry the transaction

                // Rollback the transaction
                await tryRollbackTransaction();
                throw;
            }
            catch (Exception ex)
                when (ex is TransactionException or RpcException)
            {
                // For other `TransactionException` or gRPC cases, you can try retrying the transaction

                // Rollback the transaction
                await tryRollbackTransaction();

                // The thrown exception can be retryable. In such case, you can basically retry the
                // transaction. However, for the other exceptions, the transaction may still fail if the
                // cause of the exception is nontransient. For such a case, you need to limit the number
                // of retries and give up retrying
                lastException = ex;
            }
            catch (Exception ex)
            {
                // If exception is not inherited from `TransactionException` you cannot retry the transaction
                _logger.LogError(ex, "{funcName} failed", funcName);

                // Rollback the transaction
                await tryRollbackTransaction();

                throw;
            }
        }
    }

    private async Task tryRollbackTransaction()
    {
        if (String.IsNullOrEmpty(_db.CurrentTransactionId))
            return;

        try
        {
            await _db.RollbackTransactionAsync();
        }
        catch (TransactionException ex)
        {
            _logger.LogWarning(ex, "Rollback failed");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Rollback failed");
            throw;
        }
    }
}
