package dev.pgordon

import com.google.gson.annotations.SerializedName
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.gson.gson
import io.ktor.request.path
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.webjars.Webjars
import kotlinx.coroutines.runBlocking
import org.slf4j.event.Level
import java.io.File
import java.time.ZoneId

val pathToAnkiResources = "/Users/pavelgordon/Library/Application Support/Anki2/Pavel/collection.media"
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    install(Webjars) {
        path = "/webjars" //defaults to /webjars
        zone = ZoneId.systemDefault() //defaults to ZoneId.systemDefault()
    }

    install(ContentNegotiation) {
        gson {
        }
    }

    val client = HttpClient(Apache) {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }
    runBlocking {
        // Sample for making a HTTP Client request
        /*
        val message = client.post<JsonSampleClass> {
            url("http://127.0.0.1:8080/path/to/endpoint")
            contentType(ContentType.Application.Json)
            body = JsonSampleClass(hello = "world")
        }
        */
    }

    routing {
        post("/dictionary/download") {
            val data = call.receive<BabbelData>()
            val deckName = call.request.queryParameters["deckName"]
                ?: data.url?.substringAfter("learn_languages/")?.substringBefore("/learned_items")
                ?: "deck"
            //url format "https://api.babbel.io/gamma/v5/en/accounts/4434c6db8cdc475783477885d114998b/learn_languages/ITA/learned_items?per_page=10&sort=last_reviewed_at"

            suspend fun downloadFile(url: String, fileName: String) {
                if (File(fileName).exists()) {
                    log.debug("No need to download $url")
                    return
                }
                val file: HttpResponse = client.get { url(url) }
                File(fileName).writeBytes(file.readBytes())
                log.info("Downloaded $fileName from url $url")
            }

            val file = File("decks/$deckName.csv")
            val existingDeck =
                when {
                    file.exists() -> file.readLines()
                        .filterNot { it.isEmpty() }
                        .map { it.substringBefore("|") }
                    else -> listOf()
                }
            val newCards = data.learnedItems
                .filterNot { it.learnLanguageText in existingDeck }
                .onEach { item ->
                    downloadFile(item.image.id.toImageUrl(), "$pathToAnkiResources/${item.image.id}.png")
                    downloadFile(item.sound.id.toAudioUrl(), "$pathToAnkiResources/${item.sound.id}.mp3")
                }

            if(newCards.isNotEmpty()) {
                file.appendText(newCards.joinToString(separator = "\n", postfix = "\n") { it.format() })
            }
            call.response.headers.append("Access-Control-Allow-Origin", "*")
            call.respond(
                mapOf(
                    "status" to "Success! Now import /decks/$deckName.csv to Anki using delimiter |",
                    "new" to newCards.size,
                    "total" to data.learnedItems.size
                )
            )
        }
    }
}


fun String.toImageUrl() = """https://images.babbel.com/v1.0.0/images/$this/variations/square/resolutions/500x500.png"""
fun String.toAudioUrl() = """https://sounds.babbel.com/v1.0.0/sounds/$this/normal.mp3"""

data class BabbelData(
    @SerializedName("learned_items")
    val learnedItems: List<Item>,
    val url: String?
)

data class Item(
    val id: String,
    @SerializedName("display_language_text")
    val displayLanguageText: String,
    @SerializedName("learn_language_text")
    val learnLanguageText: String,
    val image: IdMessage,
    val sound: IdMessage

) {
    fun format(): String {
        return "$learnLanguageText|<img src='$image.png'/>|$displayLanguageText|[sound:$sound.mp3]"
    }
}

data class IdMessage(val id: String) {
    override fun toString(): String {
        return id
    }
}