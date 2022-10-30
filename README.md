# Usagi

> a kotlin multi-platform [rabbitmq](https://rabbitmq.org) client

- ⚡ powered by [kotlin coroutines](https://github.com/kotlin/kotlinx.coroutines)
- 🚀 uses [ktor](https://ktor.io)
- 😎 modern api

> **Warning**
> Usagi is in Alpha, bugs may be ahead!

## installation

current version: *coming soon*

#### 🐘 docker

```kotlin
repositories {
    maven("https://maven.dimensional.fun/releases")
}

dependencies {
    // common:
    implementation("mixtape.oss.usagi:usagi:{VERSION}")
}
```

## usage

**create a channel:**

```kotlin
val connection = Usagi.connect("amqp://guest:guest@localhost:5672")
val channel = connection.channels.create() ?: error("Unable to create channel")
```

**using exchanges & queues**
```kotlin
channel.exchange.declare { 
    exchange = "my-exchange" 
}

val queueName = channel.queue.declare().queue
channel.queue.bind {
    exchange = "my-exchange"
    queue = queueName
    routingKey = "my-routing-key"
}
```

**publishing messages:**
```kotlin
channel.basic.publish {
    data = "Hello, World!".decodeToString()
    
    properties {
        contentType = "text/plain"
    }
    
    options {
        exchangeName = "my-exchange"
        routingKey = "my-routing-key"
    }
}
```

**consuming messages:**
```kotlin
val consumer = channel.basic.consume {
    exchangeName = "my-exchange"
    routingKey = "my-routingKey"
}

consumer.on<MessagePublishedEvent> {
    println(delivery.data.encodeToString()) // >> 'Hello, World'
    delivery.ack(multiple = false)
}
```

## acknowledgements

- a bit of the internal connection/channel code was adapted from the [official rabbitmq java client](https://github.com/rabbitmq/rabbitmq-java-client)
- [amqp 0.9.1 spec](https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)
- [amqp 0.9.1 doc](https://www.rabbitmq.com/resources/specs/amqp-xml-doc0-9-1.pdf)

---

[Dimensional Fun](https://dimensional.fun)
