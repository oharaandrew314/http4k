package org.http4k.format

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.startsWith
import com.squareup.moshi.Moshi.Builder
import org.http4k.format.StrictnessMode.FailOnUnknown
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class MoshiXmlTest : AutoMarshallingContract(MoshiXml) {
    override val obj = ArbObject("hello", ArbObject("world", null, listOf(1), true), listOf(3,2,1), false)
    override val expectedAutoMarshallingResult: String = "<string>hello</string><bool>false</bool><numbers>3</numbers><numbers>2</numbers><numbers>1</numbers><child><string>world</string><bool>true</bool><numbers>1</numbers></child>"

    override val expectedAutoMarshallingResultPrimitives: String = "<localDateTime>2000-01-01T01:01:01</localDateTime><period>P1Y2M3D</period><offsetTime>01:01:01Z</offsetTime><uuid>1a448854-1687-4f90-9562-7d527d64383c</uuid><uri>http://uri:8000</uri><url>http://url:9000</url><instant>1970-01-01T00:00:00Z</instant><duration>PT1S</duration><localTime>01:01:01</localTime><zonedDateTime>2000-01-01T01:01:01Z[UTC]</zonedDateTime><offsetDateTime>2000-01-01T01:01:01Z</offsetDateTime><localDate>2000-01-01</localDate><status>200</status>"

    override val expectedWrappedMap = "<value><key2>123</key2><key>value</key></value>"

    override val expectedConvertToInputStream: String = "<value>hello</value>"
    override val expectedThrowable: String = "<value>org.http4k.format.CustomException:foobar"

    override val inputUnknownValue: String = "<value>value</value><unknown>2000-01-01</unknown>"

    override val inputEmptyObject: String = ""
    override val expectedRegexSpecial: String = "<regex>.*</regex>"
    override val expectedMap = "<key2>123</key2><key>value</key>"

    override val expectedArbitraryArray = "<value>foo</value><value>123.1</value><value><foo><bar>1.1</bar><bar>2.1</bar></foo></value><value>true</value>"

    override val expectedArbitrarySet = "<value>foo</value><value>bar</value>"

    override val expectedArbitraryMap = "<str>val1</str><bool>true</bool><array>1.1</array><array>stuff</array><num>123.1</num><map><foo>bar</foo></map>"

    override val expectedAutoMarshallingZonesAndLocale = "<zoneOffset>-04:00</zoneOffset><zoneId>America/Toronto</zoneId><locale>en-CA</locale>"

    override fun strictMarshaller() = object : ConfigurableMoshiXml(
        Builder().asConfigurable().customise(), strictness = FailOnUnknown
    ) {}

    override fun customMarshaller() = ConfigurableMoshiXml(Builder().asConfigurable().customise()
        .add(NullSafeMapAdapter).add(ListAdapter))

    override fun customMarshallerProhibitStrings() = ConfigurableMoshiXml(
        Builder().asConfigurable().prohibitStrings().customise()
    )

    override fun `exception is marshalled`() {
        assertThat(
            MoshiXml.asFormatString(RuntimeException("foobar")),
            containsSubstring("foobar")
        )
    }

    @Disabled("not supported")
    override fun `roundtrip arbitrary array`() {}

    @Disabled("not supported")
    override fun `roundtrip arbitrary map`() {}

    @Disabled("not supported")
    override fun `roundtrip arbitrary set`() {}

    @Test
    override fun `roundtrip custom value`() {
        val marshaller = customMarshaller()

        val wrapper = MyValueHolder(MyValue("foobar"))
        assertThat(marshaller.asFormatString(wrapper), equalTo("<value>foobar</value>"))
        assertThat(marshaller.asA("<value>foobar</value>", MyValueHolder::class), equalTo(wrapper))

        // FIXME
//        assertThat(marshaller.asA("<value></value>", MyValueHolder::class), equalTo(MyValueHolder(null)))
    }

    override fun `roundtrip custom boolean`() {
        val marshaller = customMarshaller()

        val wrapper = XmlEnvelope(BooleanHolder(true))
        assertThat(marshaller.asFormatString(wrapper), equalTo("<wrapped>$expectedCustomBoolean</wrapped>"))
        assertThat(marshaller.asA("<wrapped>$expectedCustomBoolean</wrapped>", BooleanHolder::class), equalTo(wrapper))
    }

    override fun `roundtrip custom decimal`() {
        val marshaller = customMarshaller()

        val wrapper = XmlEnvelope(BigDecimalHolder(1.01.toBigDecimal()))
        assertThat(marshaller.asFormatString(wrapper), equalTo("<wrapped>$expectedCustomDecimal</wrapped>"))
        assertThat(marshaller.asA("<wrapped>$expectedCustomDecimal</wrapped>", BigDecimalHolder::class), equalTo(wrapper))
    }

    override fun `roundtrip custom number`() {
        val marshaller = customMarshaller()

        val wrapper = XmlEnvelope(BigIntegerHolder(1.toBigInteger()))
        assertThat(marshaller.asFormatString(wrapper), equalTo("<wrapped>$expectedCustomNumber</wrapped>"))
        assertThat(marshaller.asA("<wrapped>$expectedCustomNumber</wrapped>", BigIntegerHolder::class), equalTo(wrapper))
    }

    @Test
    override fun `automarshalling failure has expected message`() {
        assertThat(runCatching { MoshiXml.autoBody<ArbObject>().toLens()(invalidArbObjectRequest) }
            .exceptionOrNull()!!.message!!, startsWith("Required value 'string' missing at \$"))
    }
}

private data class XmlEnvelope<T: Any>(val wrapped: T)
