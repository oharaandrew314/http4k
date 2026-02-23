package org.http4k.connect.amazon.cloudwatch

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import org.http4k.connect.amazon.FakeAwsContract
import org.http4k.connect.amazon.cloudwatch.model.Dimension
import org.http4k.connect.amazon.cloudwatch.model.Metric
import org.http4k.connect.amazon.cloudwatch.model.MetricDataQuery
import org.http4k.connect.amazon.cloudwatch.model.MetricDataResult
import org.http4k.connect.amazon.cloudwatch.model.MetricDatum
import org.http4k.connect.amazon.cloudwatch.model.MetricName
import org.http4k.connect.amazon.cloudwatch.model.MetricStat
import org.http4k.connect.amazon.cloudwatch.model.MetricUnit
import org.http4k.connect.amazon.cloudwatch.model.Namespace
import org.http4k.connect.amazon.cloudwatch.model.StatusCode
import org.http4k.connect.model.Timestamp
import org.http4k.connect.storage.InMemory
import org.http4k.connect.storage.Storage
import org.http4k.connect.successValue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class FakeCloudWatchTest : CloudWatchContract, FakeAwsContract {
    private val metrics = Storage.InMemory<MutableList<MetricDatum>>()
    private val clock = object: Clock() {
        override fun instant() = Instant.EPOCH + Duration.ofDays(365)
        override fun getZone() = ZoneId.systemDefault()
        override fun withZone(zone: ZoneId?) = this
    }

    override val http = FakeCloudWatch(metrics = metrics, clock = clock)

    @Test
    fun `injected clock used for metric timestamps`() {
        val namespace = Namespace.of("http4k-connect-test-namespace")
        val metricName = MetricName.of("http4k-connect-test-metric-name")
        val time = clock.instant()

        cloudWatch.putMetricData(
            Namespace = namespace,
            EntityMetricData = null,
            MetricData = listOf(
                MetricDatum(
                    MetricName = metricName,
                    Unit = MetricUnit.Count_per_Second,
                    Value = 1.0,
                    Timestamp = null, // will be set by cloudwatch
                    Values = listOf(0.5, 1.0),
                    StorageResolution = 60,
                ),
            ),
            StrictEntityValidation = null,
        )

        val metricStatistics = cloudWatch.getMetricStatistics(
            MetricName = metricName,
            Namespace = namespace,
            StartTime = time.minusSeconds(60),
            EndTime = time.plusSeconds(60),
            Period = 60,
            Unit = MetricUnit.Count_per_Second,
        ).successValue()
        assertThat(metricStatistics.Datapoints, hasSize(equalTo(1)))
        assertThat(metricStatistics.Datapoints.first().Timestamp, equalTo(time))
    }

    @Test
    fun `get metric data`() {
        val namespace = Namespace.of("http4k-connect-test-namespace")
        val metricName = MetricName.of("http4k-connect-test-metric-name")
        val timestamp = Instant.now()

        cloudWatch.putMetricData(
            Namespace = namespace,
            EntityMetricData = null,
            MetricData = listOf(
                MetricDatum(
                    MetricName = metricName,
                    Timestamp = timestamp,
                    Unit = MetricUnit.Count_per_Second,
                    Value = 1.0,
                    Values = listOf(0.5, 1.0),
                    StorageResolution = 60,
                ),
            ),
            StrictEntityValidation = null,
        )

        cloudWatch.getMetricData(
            MetricDataQueries = listOf(
                MetricDataQuery(
                    Id = "http4k_connect_test_metric_data_query_id",
                    MetricStat = MetricStat(
                        Metric = Metric(
                            MetricName = metricName,
                            Namespace = namespace,
                        ),
                        Stat = "Maximum",
                        Unit = MetricUnit.Count_per_Second,
                    )
                )
            ),
            StartTime = Timestamp.of(timestamp.minusSeconds(120)),
            EndTime = Timestamp.of(timestamp.plusSeconds(60)),
        ).successValue {
            assertThat(it.MetricDataResults, hasSize(equalTo(1)))
            assertThat(it.MetricDataResults.first(), equalTo(MetricDataResult(
                Id = metricName.value,
                Values = listOf(0.5, 1.0),
                StatusCode = StatusCode.Complete,
                Timestamps = listOf(timestamp, timestamp)
            )))
        }
    }

    @Test
    fun `merge metrics`() {
        val namespace = Namespace.of("http4k-connect-test-namespace")
        val metricName = MetricName.of("http4k-connect-test-metric-name")

        val baseDatum = MetricDatum(
            MetricName = metricName,
            Unit = MetricUnit.Count,
            Value = 1.0
        )

        val datums = listOf(
            baseDatum,
            baseDatum.copy(
                Dimensions = listOf(
                    Dimension("dimension1", "value1")
                )
            ),
            baseDatum.copy(
                Dimensions = listOf(
                    Dimension("dimension1", "value2")
                )
            ),
            baseDatum.copy(
                Dimensions = listOf(
                    Dimension("dimension2", "value1")
                )
            )
        )

        val t0 = clock.instant() - Duration.ofSeconds(30)
        cloudWatch.putMetricData(
            Namespace = namespace,
            MetricData = datums.map {
                it.copy(Timestamp = t0)
            }
        ).successValue()

        val t1 = t0 + Duration.ofSeconds(10)
        cloudWatch.putMetricData(
            Namespace = namespace,
            MetricData = datums.map {
                it.copy(Value = 2.0, Timestamp = t1)
            }
        ).successValue()

        cloudWatch.getMetricData(
            MetricDataQueries = listOf(
                MetricDataQuery(
                    Id = "http4k_connect_test_metric_data_query_id",
                    MetricStat = MetricStat(
                        Metric = Metric(metricName, namespace),
                        Stat = "Sum"
                    )
                )
            ),
            StartTime = Timestamp.of(t0.minusSeconds(120)),
            EndTime = Timestamp.of(t1.plusSeconds(60)),
        ).successValue { data ->
            assertThat(data.MetricDataResults, hasSize(equalTo(1)))
            assertThat(data.MetricDataResults.first(), equalTo(MetricDataResult(
                Id = metricName.value,
                Values = listOf(3.0),
                StatusCode = StatusCode.Complete,
                Timestamps = listOf(t0, t1)
            )))
        }
    }

    @Test
    fun `dimension order does not matter for merging metrics`() {

    }
}
