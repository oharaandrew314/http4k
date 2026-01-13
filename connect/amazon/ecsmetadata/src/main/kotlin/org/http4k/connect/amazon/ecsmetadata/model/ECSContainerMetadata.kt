package org.http4k.connect.amazon.ecsmetadata.model

import org.http4k.connect.amazon.core.model.ARN
import org.http4k.connect.amazon.model.IpV4Address
import se.ansman.kotshi.JsonSerializable
import java.time.Instant

@JsonSerializable
data class ECSContainerMetadata(
    val DockerId: String,
    val Name: String,
    val DockerName: String,
    val Image: String,
    val ImageID: String,
    val Ports: List<ECSContainerPort>?,
    val Labels: Map<String, String>?,
    val DesiredStatus: String,
    val KnownStatus: String,
    val ExitCode: Int?,
    val Limits: ECSLimits?,
    val CreatedAt: Instant,
    val StartedAt: Instant?,
    val FinishedAt: Instant?,
    val Type: String,
    val LogDriver: String?,
    val LogOptions: Map<String, String>?,
    val ContainerARN: ARN?,
    val Networks: List<ECSNetwork>?,
    val RestartCount: Int?,
    val ExecutionStoppedAt: Instant?,
    val Health: ECSContainerHealth?
)

@JsonSerializable
data class ECSNetwork(
    val NetworkMode: String,
    val IPv4Addresses: List<IpV4Address>,
    val AttachmentIndex: Int?,
    val MACAddress: String?,
    val IPv4SubnetCIDRBlock: String?,
    val PrivateDNSName: String?,
    val SubnetGatewayIpv4Address: String?
)

@JsonSerializable
data class ECSContainerPort(
    val ContainerPort: Int,
    val Protocol: String,
    val HostPort: Int?,
    val HostIp: IpV4Address?
)

@JsonSerializable
data class ECSContainerHealth(
    val status: String?,
    val statusSince: Instant?,
    val output: String?
)