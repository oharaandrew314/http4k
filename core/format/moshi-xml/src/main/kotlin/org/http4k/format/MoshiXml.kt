package org.http4k.format

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Moshi.Builder
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.http4k.lens.BiDiMapping
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

private fun standardConfig(kotlinFactory: JsonAdapter.Factory = KotlinJsonAdapterFactory()) = Builder()
    .add(Boolean::class.java, CoercingBooleanAdapter)
    .add(Boolean::class.javaPrimitiveType!!, CoercingBooleanAdapter)
    .add(CoercingCollectionAdapterFactory)
    .addLast(EventAdapter)
    .addLast(ThrowableAdapter)
//    .addLast(ListAdapter)
//    .addLast(SetAdapter)
    .addLast(MapAdapter)
    .asConfigurable(kotlinFactory)
    .boolean(BiDiMapping({ it}, {it}))
    .withStandardMappings()

object MoshiXml : ConfigurableMoshiXml(standardConfig().done()) {
    fun custom(configureFn: AutoMappingConfiguration<Builder>.() -> AutoMappingConfiguration<Builder>) =
        ConfigurableMoshiXml(standardConfig().let(configureFn).done())
}

/**
 * A special Adapter to serialise nulls
 */
object NullSafeMapAdapter : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi) =
        MapAdapter.create(type, annotations, moshi)?.serializeNulls()
}

/**
 * Because XML makes no distinction between booleans and strings
 */
object CoercingBooleanAdapter : JsonAdapter<Boolean>() {
    override fun fromJson(reader: JsonReader): Boolean? {
        return when (reader.peek()) {
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.STRING -> reader.nextString().toBoolean()
            else -> null
        }
    }

    override fun toJson(writer: JsonWriter, value: Boolean?) {
        if (value == null) {
            writer.nullValue()
        } else {
            writer.value(value)
        }
    }
}

/**
 * Because XML "lists" with a single element can look like a string property
 */
object CoercingCollectionAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = Types.getRawType(type)
        val isArray = rawType.isArray
        val isList = rawType == List::class.java || rawType == Collection::class.java
        val isSet = rawType == Set::class.java

        val elementType = when {
            isArray -> rawType.componentType
            isList || isSet -> Types.collectionElementType(type, Collection::class.java)
            else -> return null
        }

        // We can't handle untyped collections
        if (elementType is TypeVariable<*>) return null

        val delegate = moshi.nextAdapter<Any>(this, type, annotations)
        val elementAdapter = moshi.adapter<Any>(elementType)

        return object : JsonAdapter<Any>() {
            override fun fromJson(reader: JsonReader): Any? {
                return if (reader.peek() != JsonReader.Token.BEGIN_ARRAY && reader.peek() != JsonReader.Token.NULL) {
                    // if this is a single element, handle it ourselves
                    val singleItem = elementAdapter.fromJson(reader)
                    when {
                        isList -> listOfNotNull(singleItem)
                        isSet -> setOfNotNull(singleItem)
                        isArray -> java.lang.reflect.Array.newInstance(rawType.componentType, 1).also {
                            java.lang.reflect.Array.set(it, 0, singleItem)
                        }
                        else -> listOfNotNull(singleItem)
                    }
                } else {
                    // otherwise, delegate to moshi
                    delegate.fromJson(reader)
                }
            }

            override fun toJson(writer: JsonWriter, value: Any?) = delegate.toJson(writer, value)
        }
    }
}
