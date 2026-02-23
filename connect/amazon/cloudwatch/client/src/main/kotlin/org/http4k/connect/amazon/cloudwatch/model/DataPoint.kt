package org.http4k.connect.amazon.cloudwatch.model

import org.http4k.connect.model.Timestamp
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class DataPoint(
    val Average: Double? = null,
    val ExtendedStatistics: Map<String, Double>? = null,
    val Maximum: Double? = null,
    val Minimum: Double? = null,
    val SampleCount: Double? = null,
    val Sum: Double? = null,
    val Timestamp: Timestamp? = null,
    val Unit: MetricUnit? = null,
)
