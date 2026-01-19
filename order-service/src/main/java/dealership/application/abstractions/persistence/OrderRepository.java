package dealership.application.abstractions.persistence;

import dealership.domain.orders.Order;

public interface OrderRepository extends CrudRepository<Order> {
}
