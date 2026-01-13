package org.http4k.connect.amazon.ecsmetadata.actions

import org.http4k.connect.Http4kConnectAction
import org.http4k.connect.amazon.ecsmetadata.ECSMetadataAction
import org.http4k.connect.amazon.ecsmetadata.model.ECSTaskMetadata

@Http4kConnectAction
class GetTaskMetadata: ECSMetadataAction<ECSTaskMetadata>("/task", ECSTaskMetadata::class)