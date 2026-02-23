package org.http4k.connect.amazon

import org.http4k.client.JavaHttpClient
import org.http4k.filter.debug
import org.http4k.util.PortBasedTest
import java.util.UUID

interface RealAwsContract : AwsContract, PortBasedTest {
    override val aws get() = configAwsEnvironment()
    override val http get() = JavaHttpClient().debug()
    override fun uuid(seed: Int) = UUID.randomUUID()
}
