# mixtape &bull; usagi

> a kotlin multi-platform [rabbitmq](https://rabbitmq.org) client

- ⚡ powered by [kotlin coroutines](https://github.com/kotlin/kotlinx.coroutines)
- 🚀 uses [ktor](https://ktor.io)
- 😎 modern api

## installation

current version: *coming soon*

#### 🐘 docker

```kotlin
repositories {
    maven("https://maven.dimensional.fun/public")
}

dependencies {
    implementation("mixtape.oss.usagi:amqp-client:{VERSION}")
}
```

## usage

```kt
suspend fun main() {
    /* connect to an amqp server */
    val client = Usagi.connect("amqp://guest:guest@localhost:5672")

    /* allocate a new channel to this connection. */
    val channel = client.channel() // or client.channel(id = _)
    
    // more coming soon!
}
```

## acknowledgements

- a bit of the internal connection/channel code was adapted from the [official rabbitmq java client](https://github.com/rabbitmq/rabbitmq-java-client)
- [amqp 0.9.1 spec](https://www.rabbitmq.com/resources/specs/amqp0-9-1.pdf)
- [amqp 0.9.1 doc](https://www.rabbitmq.com/resources/specs/amqp-xml-doc0-9-1.pdf)
