package sample.customer.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(schema = "customer_service", value = "customers")
public class Customer {

  @Id
  public final int customerId;
  public final String name;
  public final int creditLimit;
  public final int creditTotal;

  public Customer(int customerId, String name, int creditLimit, int creditTotal) {
    this.customerId = customerId;
    this.name = name;
    this.creditLimit = creditLimit;
    this.creditTotal = creditTotal;
  }

  public Customer withCreditTotal(int newCreditTotal) {
    return new Customer(this.customerId, this.name, this.creditLimit, newCreditTotal);
  }
}
