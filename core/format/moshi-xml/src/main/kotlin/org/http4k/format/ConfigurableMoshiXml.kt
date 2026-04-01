package org.http4k.format

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpMessage
import org.http4k.core.with
import org.http4k.format.StrictnessMode.Lenient
import org.http4k.lens.BiDiBodyLensSpec
import org.http4k.lens.BiDiMapping
import org.http4k.lens.ContentNegotiation
import org.http4k.lens.ContentNegotiation.Companion.None
import org.json.JSONObject
import org.json.XML
import java.io.InputStream
import kotlin.reflect.KClass

open class ConfigurableMoshiXml(
    builder: Moshi.Builder,
    override val defaultContentType: ContentType = ContentType.APPLICATION_XML,
    strictness: StrictnessMode = Lenient
) : AutoMarshallingXml() {

    private val json = ConfigurableMoshi(builder, defaultContentType, strictness)

    override fun <T : Any> asA(input: InputStream, target: KClass<T>) = XML
        .toJSONObject(input.reader(), true)
        .toString()
        .let { json.asA(it, target) }

    override fun <T : Any> asA(input: String, target: KClass<T>) = asA(input.byteInputStream(), target)

    override fun Any.asXmlString(): String = json
        .asFormatString(this)
        .also { println(it) }
        .let(::JSONObject)
        .let(XML::toString)

    inline fun <reified T : Any> asBiDiMapping() =
        BiDiMapping<String, T>({ asA(it) }, ::asFormatString)

    inline fun <reified T : Any> Body.Companion.auto(
        description: String? = null,
        contentNegotiation: ContentNegotiation = None
    ) = autoBody<T>(description, contentNegotiation)

    inline fun <reified T : Any> autoBody(
        description: String? = null,
        contentNegotiation: ContentNegotiation = None
    ): BiDiBodyLensSpec<T> = httpBodyLens(
        description, contentNegotiation,
        defaultContentType
    ).map({ asA(it) }, ::asFormatString)

    inline fun <reified T : Any, R : HttpMessage> R.xml(t: T): R = with(Body.auto<T>().toLens() of t)
    inline fun <reified T : Any> HttpMessage.xml(): T = Body.auto<T>().toLens()(this)
}
