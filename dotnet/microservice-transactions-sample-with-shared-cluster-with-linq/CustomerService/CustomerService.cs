using Grpc.Core;
using MicroserviceTransactionsSample.Rpc;
using MicroserviceTransactionsSample.Common;
using ScalarDB.Client.Exceptions;
using static MicroserviceTransactionsSample.Rpc.CustomerService;

namespace MicroserviceTransactionsSample.CustomerService;

public class CustomerService : CustomerServiceBase
{
    private readonly CustomerDbContext _db;
    private readonly ILogger<CustomerService> _logger;

    public CustomerService(CustomerDbContext db,
                           ILogger<CustomerService> logger)
    {
        _db = db;
        _logger = logger;
    }

    /// <summary>
    /// Get customer information. This function processing operations can be used in both a normal
    /// transaction and a global transaction.
    /// </summary>
    public override async Task<GetCustomerInfoResponse> GetCustomerInfo(GetCustomerInfoRequest request,
                                                                        ServerCallContext context)
    {
        const string funcName = "Getting customer info";

        var operations = () =>
        {
            // Retrieve the customer info for the specified customer ID
            var customer = _db.Customers.FirstOrDefault(c => c.Id == request.CustomerId);
            if (customer == null)
                throw new NotFoundException($"Customer not found (id: {request.CustomerId})");

            return new GetCustomerInfoResponse
                   {
                       Id = customer.Id,
                       Name = customer.Name,
                       CreditLimit = customer.CreditLimit,
                       CreditTotal = customer.CreditTotal
                   };
        };

        if (request.HasTransactionId)
        {
            // For a global transaction, execute the operations as a participant
            return await execOperationsAsParticipant(funcName, request.TransactionId, operations);
        }
        else
        {
            // For a normal transaction, execute the operations
            return await execOperations(funcName, operations);
        }
    }

    /// <summary>
    /// Credit card payment. It's for a global transaction that spans OrderService and CustomerService.
    /// </summary>
    public override async Task<PaymentResponse> Payment(PaymentRequest request, ServerCallContext context)
        => await execOperationsAsParticipant(
               "Payment",
               request.TransactionId,
               async () =>
               {
                   // Retrieve the customer info for the customer ID
                   var customer = _db.Customers.FirstOrDefault(c => c.Id == request.CustomerId);
                   if (customer == null)
                       throw new NotFoundException($"Customer not found (id: {request.CustomerId})");

                   // Update credit_total for the customer
                   // and check if the credit total exceeds the credit limit after payment
                   customer.CreditTotal += request.Amount;
                   if (customer.CreditTotal > customer.CreditLimit)
                   {
                       throw new FailedPreconditionException(
                           $"Credit limit exceeded ({customer.CreditTotal} > {customer.CreditLimit})");
                   }

                   // Save changes to the customer
                   await _db.Customers.UpdateAsync(customer);

                   return new PaymentResponse();
               });

    /// <summary>
    /// Credit card repayment.
    /// </summary>
    public override async Task<RepaymentResponse> Repayment(RepaymentRequest request, ServerCallContext context)
        => await execOperations(
               "Repayment",
               async () =>
               {
                   // Retrieve the customer info for the specified customer ID
                   var customer = _db.Customers.FirstOrDefault(c => c.Id == request.CustomerId);
                   if (customer == null)
                       throw new NotFoundException($"Customer not found (id: {request.CustomerId})");

                   // Reduce credit_total for the customer
                   // and check if over-repayment or not
                   customer.CreditTotal -= request.Amount;
                   if (customer.CreditTotal < 0)
                       throw new FailedPreconditionException($"Over-repayment ({customer.CreditTotal})");

                   // Save changes to the customer
                   await _db.Customers.UpdateAsync(customer);

                   return new RepaymentResponse();
               });

    private Task<T> execOperations<T>(string funcName,
                                      Func<T> operations)
        => execOperations(funcName,
                          () => Task.FromResult(operations()));

    private async Task<T> execOperations<T>(string funcName,
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

                // Commit the transaction (even when the transaction is read-only, we need to commit)
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
            catch (TransactionException ex)
            {
                // For other cases, you can try retrying the transaction

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

    private Task<T> execOperationsAsParticipant<T>(string funcName,
                                                   string transactionId,
                                                   Func<T> operations)
        => execOperationsAsParticipant(funcName,
                                       transactionId,
                                       () => Task.FromResult(operations()));

    private async Task<T> execOperationsAsParticipant<T>(string funcName,
                                                         string transactionId,
                                                         Func<Task<T>> operations)
    {
        try
        {
            // Join the transaction
            await _db.JoinTransactionAsync(transactionId);

            // Execute operations and return the result
            return await operations();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "{funcName} failed", funcName);

            if (ex is TransactionException)
                throw new InternalException(funcName + " failed", ex);
            else
                throw;
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
