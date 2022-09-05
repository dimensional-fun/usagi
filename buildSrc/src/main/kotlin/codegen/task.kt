// Thanks https://github.com/Noelware/iana-tz/blob/master/buildSrc/src/main/kotlin/GenerateDataTask.kt <3

package codegen

import com.squareup.kotlinpoet.FileSpec
import kotlinx.serialization.json.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException

abstract class GenerateAMQPClasses : DefaultTask() {
    companion object {
        const val CODE_GEN_DATA = "https://github.com/rabbitmq/rabbitmq-codegen/raw/main/amqp-rabbitmq-0.9.1.json"
    }

    init {
        group = "build"
        outputs.upToDateWhen { false }
    }

    private val okhttp = OkHttpClient.Builder()
        .followRedirects(true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @get:OutputDirectory
    abstract val outputDirectory: Property<File>

    @TaskAction
    fun execute() {
        val request = Request.Builder()
            .url(CODE_GEN_DATA)
            .method("GET", null)
            .addHeader("Accept", "application/json; charset=utf-8")
            .build()

        try {
            val res = okhttp.newCall(request).execute()
            val body = res.body!!.use { it.string() }

            if (res.code in 200..299) {
                val data = json.decodeFromString(JsonObject.serializer(), body)
                generateWithCodegenData(data)
            }
        } catch (e: IOException) {
            project.logger.error("Unable to request to $CODE_GEN_DATA")
        }


    }

    private fun generate(fileSpec: FileSpec) {
        val directory = outputDirectory.get()
        directory.mkdirs()
        fileSpec.writeTo(directory.toPath())
    }

    private fun generateWithCodegenData(data: JsonObject) {
        val domains = mutableMapOf<String, AMQP.Type>()
        for (domainElement in data["domains"]!!.jsonArray) {
            val (alias, type) = domainElement.jsonArray
            domains[alias.jsonPrimitive.content] = AMQP.Type.valueOf(type.jsonPrimitive.content.toUpperCase())
        }

        val file = generateAMQP(data["name"]!!.jsonPrimitive.content, data["classes"]!!.jsonArray
            .map { it.jsonObject }
            .map { AMQP.Class.fromJson(domains, it) })

        generate(file)
    }
}
