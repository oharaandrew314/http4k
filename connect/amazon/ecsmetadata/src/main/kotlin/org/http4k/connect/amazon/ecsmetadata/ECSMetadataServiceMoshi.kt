package org.http4k.connect.amazon.ecsmetadata

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.http4k.connect.amazon.core.model.VpcId
import org.http4k.connect.amazon.model.IpV4Address
import org.http4k.format.AwsCoreJsonAdapterFactory
import org.http4k.format.ConfigurableMoshi
import org.http4k.format.ListAdapter
import org.http4k.format.MapAdapter
import org.http4k.format.asConfigurable
import org.http4k.format.stringOrNull
import org.http4k.format.value
import org.http4k.format.withAwsCoreMappings
import org.http4k.format.withStandardMappings
import se.ansman.kotshi.KotshiJsonAdapterFactory
import java.lang.reflect.Type
import java.math.BigDecimal

object ECSMetadataServiceMoshi : ConfigurableMoshi(
    Moshi.Builder()
        .add(EcsMetadataServiceAdapterFactory)
        .add(AwsCoreJsonAdapterFactory())
        .add(ListAdapter)
        .add(MapAdapter)
        .add(BigDecimal::class.java, BigDecimalAdapter)
        .add(Ip4vAddressListFactory)
        .asConfigurable()
        .withStandardMappings()
        .withAwsCoreMappings()
        .value(IpV4Address)
        .value(VpcId)
        .done()
)

private object BigDecimalAdapter: JsonAdapter<BigDecimal>() {
    override fun fromJson(reader: JsonReader) = reader
        .stringOrNull()
        ?.let(::BigDecimal)

    override fun toJson(writer: JsonWriter, value: BigDecimal?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value.toPlainString())
        }
    }
}

private object Ip4vAddressListFactory : JsonAdapter.Factory {
    private val targetType = Types.newParameterizedType(List::class.java, IpV4Address::class.java)

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi) =
        if (type == targetType) Ip4vAddressListAdapter else null
}

// Necessary because sometimes the list contains empty strings
private object Ip4vAddressListAdapter: JsonAdapter<List<IpV4Address>>() {
    override fun fromJson(reader: JsonReader) = buildList {
        reader.beginArray()
        while(reader.hasNext()) {
            val value = reader.nextString()
            if (value.isNotBlank()) {
                add(IpV4Address.parse(value))
            }
        }
        reader.endArray()
    }

    override fun toJson(writer: JsonWriter, values: List<IpV4Address>?) {
        if (values == null) {
            writer.nullValue()
            return
        }

        writer.beginArray()
        for (value in values) {
            writer.value(value.value)
        }
        writer.endArray()
    }
}

@KotshiJsonAdapterFactory
internal object EcsMetadataServiceAdapterFactory : JsonAdapter.Factory by KotshiEcsMetadataServiceAdapterFactory
