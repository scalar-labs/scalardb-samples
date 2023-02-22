package sample.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("customers")
public class Customer {
  @Id
  @JsonProperty("customer_id")
  public final int customerId;

  @JsonProperty("name")
  public final String name;

  @JsonProperty("credit_limit")
  public final int creditLimit;

  @JsonProperty("credit_total")
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
