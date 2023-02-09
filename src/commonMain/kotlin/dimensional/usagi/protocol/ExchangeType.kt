package dimensional.usagi.protocol

/**
 * An object containing all valid AMQP 0.9.1 exchange types.
 *
 * The [RabbitMQ AMQP Concepts](https://www.rabbitmq.com/tutorials/amqp-concepts.html#exchanges) page does a good job of illustrating and explaining the differences between the exchange types.
 */
public object ExchangeType {
    /**
     * A direct exchange delivers messages to queues based on the message routing key.
     */
    public const val Direct: String = "direct"

    /**
     * A fanout exchange routes messages to all the queues that are bound to it and the routing key is ignored.
     */
    public const val Fanout: String = "fanout"

    /**
     * Topic exchanges route messages to one or many queues based on matching between a message routing key
     * and the pattern that was used to bind a queue to an exchange.
     */
    public const val Topic: String = "topic"

    /**
     * A headers exchange is designed for routing on multiple attributes that are more easily expressed
     * as message headers than a routing key.
     */
    public const val Headers: String = "headers"
}
