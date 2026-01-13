package org.http4k.connect.amazon.ecsmetadata

import org.http4k.chaos.ChaoticHttpHandler
import org.http4k.chaos.start
import org.http4k.config.Environment
import org.http4k.connect.amazon.core.model.ARN
import org.http4k.connect.amazon.core.model.AwsAccount
import org.http4k.connect.amazon.core.model.Region
import org.http4k.connect.amazon.core.model.VpcId
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerHealth
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerMetadata
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerPort
import org.http4k.connect.amazon.ecsmetadata.model.ECSLimits
import org.http4k.connect.amazon.ecsmetadata.model.ECSNetwork
import org.http4k.connect.amazon.ecsmetadata.model.ECSTaskMetadata
import org.http4k.connect.amazon.model.IpV4Address
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Uri
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Instant
import kotlin.String

class FakeECSMetadataService(
    metadata: ECSTaskMetadata = fakeEcsTaskMetadata(Instant.now())
) : ChaoticHttpHandler() {

    override val app = routes(
        "" bind Method.GET to { request ->
            if (request.uri.path.endsWith("/")) {
                Response(Status.NOT_FOUND)
            } else {
                getContainerMetadata(metadata.Containers.first())(request)
            }
        },
        "/task" bind Method.GET to getMetadata(metadata)
    )

    fun client() = ECSMetadataService.Http(Environment.defaults(ECS_CONTAINER_METADATA_URI_V4 of Uri.of("")), this)
}

fun main() {
    FakeECSMetadataService().start()
}

fun fakeEcsTaskMetadata(
    time: Instant,
    region: Region = Region.CA_CENTRAL_1,
    accountId: AwsAccount = AwsAccount.parse("1234567890"), // TODO use fakeAccount
    serviceName: String = "defaultService",
    taskId: String = "0000000001",
    privateIp: IpV4Address = IpV4Address.parse("10.0.0.1"),
    dockerId: String = "12345"
) = ECSTaskMetadata(
    Cluster = "defaultCluster",
    ServiceName = serviceName,
    VPCID = VpcId.parse("vpc-123"),
    TaskARN = ARN.parse("arn:aws:ecs:$region:$accountId:task/$serviceName/$taskId"),
    Family = "defaultFamily",
    Revision = "1",
    DesiredStatus = "RUNNING",
    KnownStatus = "RUNNING",
    PullStartedAt = time,
    PullStoppedAt = time.plusSeconds(1),
    AvailabilityZone = "${region}a",
    Limits = ECSLimits(
        CPU = 1.toBigDecimal(),
        Memory = 1024
    ),
    LaunchType = "EC2",
    Errors = null,
    Containers = listOf(
        ECSContainerMetadata(
            DockerId = dockerId,
            Name = "defaultContainer",
            DockerName = "defaultDocker",
            Image = "testApp:latest",
            ImageID = "sha256:123456789",
            Health = ECSContainerHealth(
                status = "HEALTHY",
                statusSince = time,
                output = "details"
            ),
            Ports = listOf(
                ECSContainerPort(
                    ContainerPort = 80,
                    Protocol = "tcp",
                    HostPort = 1234,
                    HostIp = privateIp
                )
            ),
            Labels = mapOf("label" to "value"),
            DesiredStatus = "RUNNING",
            KnownStatus = "RUNNING",
            ExitCode = null,
            CreatedAt = time,
            StartedAt = time.plusSeconds(20),
            FinishedAt = null,
            Limits = null,
            Type = "NORMAL",
            LogDriver = "awslogs",
            LogOptions = mapOf(
                "awslogs-group" to "defaultApp-LogGroup",
                "awslogs-region" to region.value,
                "awslogs-stream" to "ecs/app/$taskId",
                "mode" to "non-blocking"
            ),
            ContainerARN = ARN.parse("arn:aws:ecs:$region:$accountId:task/$serviceName/$taskId/0001"),
            Networks = listOf(
                ECSNetwork(
                    NetworkMode = "bridge",
                    IPv4Addresses = listOf(privateIp),
                    AttachmentIndex = null,
                    MACAddress = null,
                    IPv4SubnetCIDRBlock = null,
                    PrivateDNSName = null,
                    SubnetGatewayIpv4Address = null
                )
            ),
            RestartCount = 1,
            ExecutionStoppedAt = null
        )
    )
)