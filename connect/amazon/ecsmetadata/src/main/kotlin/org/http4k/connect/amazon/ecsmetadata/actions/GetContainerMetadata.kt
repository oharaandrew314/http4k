package org.http4k.connect.amazon.ecsmetadata.actions

import org.http4k.connect.Http4kConnectAction
import org.http4k.connect.amazon.ecsmetadata.ECSMetadataAction
import org.http4k.connect.amazon.ecsmetadata.model.ECSContainerMetadata

@Http4kConnectAction
class GetContainerMetadata: ECSMetadataAction<ECSContainerMetadata>("", ECSContainerMetadata::class)