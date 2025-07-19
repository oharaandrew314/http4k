package org.http4k.connect.amazon.secretsmanager.action

import org.http4k.connect.Http4kConnectAction
import org.http4k.connect.amazon.secretsmanager.SecretsManagerAction
import org.http4k.connect.amazon.secretsmanager.model.Secret
import org.http4k.connect.amazon.secretsmanager.model.SecretId
import se.ansman.kotshi.JsonSerializable

@Http4kConnectAction
@JsonSerializable
data class DescribeSecret(
    val SecretId: SecretId
): SecretsManagerAction<Secret>(Secret::class)
