package org.http4k.connect.amazon.ecsmetadata

import org.http4k.connect.amazon.core.model.ARN
import org.http4k.connect.amazon.core.model.VpcId
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerHealth
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerMetadata
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerPort
import org.http4k.connect.amazon.ecsmetadata.model.ECSLimits
import org.http4k.connect.amazon.ecsmetadata.model.ECSNetwork
import org.http4k.connect.amazon.ecsmetadata.model.ECSTaskMetadata
import org.http4k.connect.amazon.model.IpV4Address
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class DeserializationTest {

    @Test
    fun `deserialize ec2`() {
        assertEquals(
            expected = ECSTaskMetadata(
                Cluster="cluster",
                TaskARN = ARN.parse("arn:aws:ecs:us-east-1:1234567890:task/service/12345678"),
                Family = "myFamily",
                Revision = "1",
                DesiredStatus = "RUNNING",
                KnownStatus = "RUNNING",
                Limits = ECSLimits(
                    CPU = BigDecimal("0.25"),
                    Memory = 512
                ),
                PullStartedAt = Instant.parse("2025-11-30T12:14:19.583664619Z"),
                PullStoppedAt = Instant.parse("2025-11-30T12:14:23.116594205Z"),
                AvailabilityZone = "us-east-1a",
                LaunchType = "EC2",
                Containers = listOf(
                    ECSContainerMetadata(
                        DockerId = "0123456789abcdef",
                        Name = "app",
                        DockerName = "dockername",
                        Image = "sample:latest",
                        ImageID = "sha256:0123456789abcdef",
                        Ports = listOf(
                            ECSContainerPort(
                                ContainerPort = 88,
                                Protocol = "tcp",
                                HostPort = 32769,
                                HostIp = IpV4Address.parse("10.0.0.1")
                            )
                        ),
                        Labels = mapOf(
                            "com.amazonaws.ecs.cluster" to "cluster",
                            "com.amazonaws.ecs.container-name" to "app",
                            "com.amazonaws.ecs.task-arn" to "arn:aws:ecs:us-east-1:1234567890:task/service/12345678",
                            "com.amazonaws.ecs.task-definition-family" to "myFamily",
                            "com.amazonaws.ecs.task-definition-version" to "1"
                        ),
                        DesiredStatus = "RUNNING",
                        KnownStatus = "RUNNING",
                        Limits = ECSLimits(
                            CPU = 2.toBigDecimal(),
                            Memory =  0
                        ),
                        CreatedAt = Instant.parse("2025-11-30T12:14:23.131248415Z"),
                        StartedAt = Instant.parse("2025-11-30T12:14:28.105989689Z"),
                        Type = "NORMAL",
                        Health = ECSContainerHealth(
                            status = "HEALTHY",
                            statusSince = Instant.parse("2025-11-30T12:14:58.54837945Z"),
                            output = "  % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current\n                                 Dload  Upload   Total   Spent    Left  Speed\n\r  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0\r  0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0\n"
                        ),
                        LogDriver = "awslogs",
                        LogOptions = mapOf(
                            "awslogs-group" to "service-logs",
                            "awslogs-region" to "us-east-1",
                            "awslogs-stream" to "ecs/app/12345678",
                            "mode" to "non-blocking"
                        ),
                        ContainerARN = ARN.parse("arn:aws:ecs:us-east-1:1234567890:container/myService/12345678/18cf05f5-0544-41c5-989f-8430828b8753"),
                        Networks = listOf(
                            ECSNetwork(
                                NetworkMode = "bridge",
                                IPv4Addresses = listOf(IpV4Address.parse("172.17.0.2")),
                                AttachmentIndex = null,
                                MACAddress = null,
                                IPv4SubnetCIDRBlock = null,
                                PrivateDNSName = null,
                                SubnetGatewayIpv4Address = null
                            )
                        ),
                        ExitCode = null,
                        FinishedAt = null,
                        RestartCount = null,
                        ExecutionStoppedAt = null
                    )
                ),
                VPCID = VpcId.parse("vpc-0123456789asbcdef"),
                ServiceName = "service",
                Errors = null
            ),
            actual = javaClass.classLoader.getResourceAsStream("sample-ec2.json")!!.use {
                ECSMetadataServiceMoshi.asA<ECSTaskMetadata>(it)
            }
        )
    }

    @Test
    fun `deserialize fargate`() {
        assertEquals(
            expected = ECSContainerMetadata(
                DockerId = "0123456789abcdef",
                Name = "app",
                DockerName = "dockerName",
                Image = "sample:latest",
                ImageID = "sha256:0123456789abcdef",
                Ports = null,
                Labels = mapOf(
                    "com.amazonaws.ecs.cluster" to "arn:aws:ecs:us-east-1:0123456789:cluster/myCluster",
                    "com.amazonaws.ecs.container-name" to "app",
                    "com.amazonaws.ecs.task-arn" to "arn:aws:ecs:us-east-1:1234567890:task/myService/myTaskId",
                    "com.amazonaws.ecs.task-definition-family" to "myFamily",
                    "com.amazonaws.ecs.task-definition-version" to "8"
                ),
                DesiredStatus = "RUNNING",
                KnownStatus = "RUNNING",
                Limits = ECSLimits(
                    CPU = 2.toBigDecimal(),
                    Memory =  null
                ),
                CreatedAt = Instant.parse("2025-12-17T19:56:30.091699251Z"),
                StartedAt = Instant.parse("2025-12-17T19:56:30.091699251Z"),
                Type = "NORMAL",
                Health = ECSContainerHealth(
                    status = "UNKNOWN",
                    statusSince = Instant.parse("2025-12-17T19:56:30.092372590Z"),
                    output = "fun stuff"
                ),
                LogDriver = "awslogs",
                LogOptions = mapOf(
                    "awslogs-group" to "service-logs",
                    "awslogs-region" to "us-east-1",
                    "awslogs-stream" to "ecs/app/myTaskId",
                    "mode" to "non-blocking"
                ),
                ContainerARN = ARN.parse("arn:aws:ecs:us-east-1:1234567890:container/myService/myTaskId/868c105d-ec7b-4bb6-a29f-91414569f180"),
                Networks = listOf(
                    ECSNetwork(
                        NetworkMode = "awsvpc",
                        IPv4Addresses = listOf(IpV4Address.parse("10.192.11.118")),
                        AttachmentIndex = 0,
                        MACAddress = "ab:cd:ef:12:34:56",
                        IPv4SubnetCIDRBlock = "10.192.11.0/24",
                        PrivateDNSName = "ip-10-192-11-118.ec2.internal",
                        SubnetGatewayIpv4Address = "10.192.11.1/24"
                    )
                ),
                ExitCode = null,
                FinishedAt = null,
                RestartCount = null,
                ExecutionStoppedAt = null
            ),
            actual = javaClass.classLoader.getResourceAsStream("sample-fargate.json")!!.use {
                ECSMetadataServiceMoshi.asA<ECSContainerMetadata>(it)
            }
        )
    }

    @Test
    fun `deserialize ec2 host container`() {
        val container = javaClass.classLoader.getResourceAsStream("sample-ec2-host-container.json")!!.use {
            ECSMetadataServiceMoshi.asA<ECSContainerMetadata>(it)
        }

        assertEquals(
            listOf(
                ECSNetwork(
                    NetworkMode = "host",
                    IPv4Addresses = emptyList(),
                    AttachmentIndex = null,
                    MACAddress = null,
                    IPv4SubnetCIDRBlock = null,
                    PrivateDNSName = null,
                    SubnetGatewayIpv4Address = null
                )
            ),
            container.Networks
        )
    }
}