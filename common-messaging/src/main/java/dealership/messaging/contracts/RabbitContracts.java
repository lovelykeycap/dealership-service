package dealership.messaging.contracts;

public final class RabbitContracts {

    public static final String ORDERS_EXCHANGE = "orders.exchange";

    public static final String ORDER_SENT_FOR_APPROVAL_ROUTING_KEY = "order.sent-for-approval";
    public static final String ORDER_APPROVED_ROUTING_KEY = "order.approved";
    public static final String ORDER_REJECTED_ROUTING_KEY = "order.rejected";

    public static final String STORAGE_APPROVAL_QUEUE = "storage.order.approval.queue";
    public static final String ORDER_APPROVAL_RESULT_QUEUE = "order.approval.result.queue";

    private RabbitContracts() {
    }
}
