package org.http4k.connect.amazon.cloudwatch.model

import org.http4k.connect.model.Timestamp
import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class MetricDataResult(
    val Id: String? = null,
    val Label: String? = null,
    val Messages: List<MessageData>? = null,
    val StatusCode: StatusCode? = null,
    val Timestamps: List<Timestamp>? = null,
    val Values: List<Double>? = null,
)
