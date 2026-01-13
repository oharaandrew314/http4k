package org.http4k.connect.amazon.ecsmetadata.model

import org.http4k.connect.amazon.core.model.ARN
import org.http4k.connect.amazon.core.model.VpcId
import se.ansman.kotshi.JsonSerializable
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@JsonSerializable
data class ECSTaskMetadata(
    val Cluster: String,
    val ServiceName: String,
    val VPCID: VpcId,
    val TaskARN: ARN,
    val Family: String,
    val Revision: String,
    val DesiredStatus: String,
    val KnownStatus: String,
    val Limits: ECSLimits,
    val PullStartedAt: Instant,
    val PullStoppedAt: Instant,
    val AvailabilityZone: String,
    val Errors: List<ECSError>?,
    val LaunchType: String?,
    val Containers: List<ECSContainerMetadata>
)

@JsonSerializable
data class ECSLimits(
    val CPU: BigDecimal?,
    val Memory: Int?
)

@JsonSerializable
data class ECSError(
    val ErrorField: String,
    val ErrorCode: String,
    val ErrorMessage: String,
    val StatusCode: Int,
    val RequestId: UUID,
    val ResourceARN: ARN
)