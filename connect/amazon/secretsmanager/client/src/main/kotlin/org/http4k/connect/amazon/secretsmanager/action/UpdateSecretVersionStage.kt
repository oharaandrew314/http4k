package org.http4k.connect.amazon.secretsmanager.action

import org.http4k.connect.Http4kConnectAction
import org.http4k.connect.amazon.core.model.ARN
import org.http4k.connect.amazon.secretsmanager.SecretsManagerAction
import org.http4k.connect.amazon.secretsmanager.model.SecretId
import org.http4k.connect.amazon.secretsmanager.model.VersionStage
import se.ansman.kotshi.JsonSerializable

@Http4kConnectAction
@JsonSerializable
data class UpdateSecretVersionStage(
    val SecretId: SecretId,
    val VersionStage: VersionStage,
    val MoveToVersionId: String?,
    val RemoveFromVersionId: String?
): SecretsManagerAction<VersionUpdatedSecret>(VersionUpdatedSecret::class)

@JsonSerializable
data class VersionUpdatedSecret(
    val ARN: ARN,
    val Name: String
)
