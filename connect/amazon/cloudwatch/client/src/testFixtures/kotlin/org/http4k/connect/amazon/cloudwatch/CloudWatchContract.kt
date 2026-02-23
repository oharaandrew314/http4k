package org.http4k.connect.amazon.cloudwatch

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import com.natpryce.hamkrest.isEmpty
import org.http4k.connect.amazon.AwsContract
import org.http4k.connect.amazon.cloudwatch.action.DeleteAlarms
import org.http4k.connect.amazon.cloudwatch.action.DescribeAlarms
import org.http4k.connect.amazon.cloudwatch.action.DescribeAlarmsForMetric
import org.http4k.connect.amazon.cloudwatch.action.DisableAlarmActions
import org.http4k.connect.amazon.cloudwatch.action.EnableAlarmActions
import org.http4k.connect.amazon.cloudwatch.action.GetMetricData
import org.http4k.connect.amazon.cloudwatch.action.ListMetrics
import org.http4k.connect.amazon.cloudwatch.action.ListTagsForResource
import org.http4k.connect.amazon.cloudwatch.action.PutCompositeAlarm
import org.http4k.connect.amazon.cloudwatch.action.PutMetricAlarm
import org.http4k.connect.amazon.cloudwatch.action.PutMetricData
import org.http4k.connect.amazon.cloudwatch.action.SetAlarmState
import org.http4k.connect.amazon.cloudwatch.action.TagResource
import org.http4k.connect.amazon.cloudwatch.action.UntagResource
import org.http4k.connect.amazon.cloudwatch.model.AlarmName
import org.http4k.connect.amazon.cloudwatch.model.AlarmState
import org.http4k.connect.amazon.cloudwatch.model.AlarmType
import org.http4k.connect.amazon.cloudwatch.model.ComparisonOperator
import org.http4k.connect.amazon.cloudwatch.model.Metric
import org.http4k.connect.amazon.cloudwatch.model.MetricDataQuery
import org.http4k.connect.amazon.cloudwatch.model.MetricDatum
import org.http4k.connect.amazon.cloudwatch.model.MetricName
import org.http4k.connect.amazon.cloudwatch.model.MetricStat
import org.http4k.connect.amazon.cloudwatch.model.MetricUnit
import org.http4k.connect.amazon.cloudwatch.model.Namespace
import org.http4k.connect.amazon.cloudwatch.model.Statistic
import org.http4k.connect.amazon.core.model.Tag
import org.http4k.connect.model.Timestamp
import org.http4k.connect.successValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import java.time.Instant

interface CloudWatchContract : AwsContract {
    val cloudWatch get() = CloudWatch.Http(aws.region, { aws.credentials }, http)

    @Test
    fun `metric alarm lifecycle`() {
        val alarmName = AlarmName.of("http4k-connect-test-alarm")
        cloudWatch(PutMetricAlarm(
            AlarmName = alarmName,
            AlarmDescription = "Alarm for testing purposes",
            ActionsEnabled = false,
            ComparisonOperator = ComparisonOperator.GreaterThanThreshold,
            DatapointsToAlarm = 1,
            EvaluationPeriods = 1,
            Statistic = Statistic.Minimum,
            MetricName = MetricName.of("htt4k-connect-test-metric"),
            Namespace = Namespace.of("http4k-connect-test-alarms"),
            Period = 60,
            Threshold = 1.0,
        )).successValue()

        try {
            val metricAlarm = cloudWatch.waitForMetricAlarmCreation(alarmName)
            assertThat(metricAlarm.AlarmName, equalTo(alarmName))
            assertThat(metricAlarm.ActionsEnabled, equalTo(false))
            cloudWatch(EnableAlarmActions(
                AlarmNames = listOf(alarmName),
            ))
            cloudWatch(SetAlarmState(
                AlarmName = alarmName,
                StateValue = AlarmState.ALARM,
                StateReason = "Test alarm state ALARM",
            ))
            val alarmsDescribedForMetric = cloudWatch(DescribeAlarmsForMetric(
                MetricName = MetricName.of("htt4k-connect-test-metric"),
                Namespace = Namespace.of("http4k-connect-test-alarms"),
            )).successValue().MetricAlarms
            assertThat(alarmsDescribedForMetric, hasSize(equalTo(1)))
            assertThat(alarmsDescribedForMetric.first().AlarmName, equalTo(alarmName))
            assertThat(alarmsDescribedForMetric.first().ActionsEnabled, equalTo(true))
            assertThat(alarmsDescribedForMetric.first().StateValue, equalTo(AlarmState.ALARM))
        } finally {
            cloudWatch(DeleteAlarms(
                AlarmNames = listOf(alarmName),
            )).successValue()
            assertThat(
                cloudWatch(DescribeAlarms(
                    AlarmTypes = listOf(AlarmType.MetricAlarm, AlarmType.CompositeAlarm)
                )).successValue().MetricAlarms.orEmpty(),
                isEmpty,
            )
        }
    }

    @Test
    fun `composite alarm lifecycle`() {
        val alarmName = AlarmName.of("http4k-connect-test-alarm")
        cloudWatch(PutCompositeAlarm(
            AlarmName = alarmName,
            AlarmRule = "FALSE",
            AlarmDescription = "Alarm for testing purposes, set to false",
            ActionsEnabled = true,
        )).successValue()

        try {
            val compositeAlarm = cloudWatch.waitForCompositeAlarmCreation(alarmName)
            assertThat(compositeAlarm.AlarmName, equalTo(alarmName))
            assertThat(compositeAlarm.ActionsEnabled, equalTo(true))
            val alarmsDescribedForMetric = cloudWatch(DescribeAlarmsForMetric(
                MetricName = MetricName.of("htt4k-connect-test-metric"),
                Namespace = Namespace.of("http4k-connect-test-alarms"),
            )).successValue().MetricAlarms
            assertThat(alarmsDescribedForMetric, hasSize(equalTo(0)))
            cloudWatch(DisableAlarmActions(
                AlarmNames = listOf(alarmName),
            ))
            val compositeAlarmsWithDisabledActions = cloudWatch(DescribeAlarms(
                AlarmTypes = listOf(AlarmType.CompositeAlarm)
            )).successValue().CompositeAlarms
            assertNotNull(compositeAlarmsWithDisabledActions)
            assertThat(compositeAlarmsWithDisabledActions, hasSize(equalTo(1)))
            assertThat(compositeAlarmsWithDisabledActions.first().AlarmName, equalTo(alarmName))
            assertThat(compositeAlarmsWithDisabledActions.first().ActionsEnabled, equalTo(false))
        } finally {
            cloudWatch(DeleteAlarms(
                AlarmNames = listOf(alarmName),
            ))
            assertThat(
                cloudWatch(DescribeAlarms(
                    AlarmTypes = listOf(AlarmType.MetricAlarm, AlarmType.CompositeAlarm)
                )).successValue().MetricAlarms.orEmpty(),
                isEmpty,
            )
        }
    }

    @Test
    fun `alarm tags lifecycle`() {
        val alarmName = AlarmName.of("http4k-connect-test-alarm")
        cloudWatch(PutMetricAlarm(
            AlarmName = alarmName,
            AlarmDescription = "Alarm for testing purposes",
            ComparisonOperator = ComparisonOperator.GreaterThanThreshold,
            DatapointsToAlarm = 1,
            EvaluationPeriods = 1,
            Statistic = Statistic.Minimum,
            MetricName = MetricName.of("htt4k-connect-test-metric"),
            Namespace = Namespace.of("http4k-connect-test-alarms"),
            Period = 60,
            Threshold = 1.0,
        )).successValue()

        try {
            val metricAlarm = cloudWatch.waitForMetricAlarmCreation(alarmName)
            val alarmArn = metricAlarm.AlarmArn
            cloudWatch(TagResource(
                ResourceARN = alarmArn,
                Tags = listOf(
                    Tag("http4k-connect-test-tag-key-1", "http4k-connect-test-tag-value-1"),
                    Tag("http4k-connect-test-tag-key-2", "http4k-connect-test-tag-value-2"),
                )
            ))
            val listedTags = cloudWatch(ListTagsForResource(
                ResourceARN = alarmArn,
            )).successValue()
            assertThat(
                listedTags.Tags.sortedBy { it.Key }, equalTo(
                    listOf(
                        Tag("http4k-connect-test-tag-key-1", "http4k-connect-test-tag-value-1"),
                        Tag("http4k-connect-test-tag-key-2", "http4k-connect-test-tag-value-2"),
                    )
                )
            )
            cloudWatch(UntagResource(
                ResourceARN = alarmArn,
                TagKeys = listOf("http4k-connect-test-tag-key-1")
            ))
            val reducedListedTags = cloudWatch(ListTagsForResource(
                ResourceARN = alarmArn,
            )).successValue()
            assertThat(
                reducedListedTags.Tags, equalTo(
                    listOf(
                        Tag("http4k-connect-test-tag-key-2", "http4k-connect-test-tag-value-2"),
                    )
                )
            )
        } finally {
            cloudWatch(DeleteAlarms(
                AlarmNames = listOf(alarmName),
            ))
            assertThat(
                cloudWatch(DescribeAlarms(
                    AlarmTypes = listOf(AlarmType.MetricAlarm, AlarmType.CompositeAlarm)
                )).successValue().MetricAlarms.orEmpty(),
                isEmpty,
            )
        }
    }

    @Test
    fun `metric data lifecycle`() {
        val namespace = Namespace.of("http4k-connect-test-namespace")
        val metricName = MetricName.of("http4k-connect-test-metric-name")
        val timestamp = Instant.now()
        cloudWatch(PutMetricData(
            Namespace = namespace,
            EntityMetricData = null,
            MetricData = listOf(
                MetricDatum(
                    MetricName = metricName,
                    Timestamp = Timestamp.of(timestamp),
                    Unit = MetricUnit.Count_per_Second,
                    Values = listOf(0.5, 1.0),
                    StorageResolution = 60,
                ),
            ),
            StrictEntityValidation = null,
        )).successValue()
        cloudWatch.waitForMetricCreation(namespace, metricName)

        cloudWatch(GetMetricData(
            MetricDataQueries = listOf(
                MetricDataQuery(
                    Id = "http4k_connect_test_metric_data_query_id",
                    MetricStat = MetricStat(
                        Metric = Metric(
                            MetricName = metricName,
                            Namespace = namespace,
                        ),
                        Stat = "Maximum",
                        Period = 60,
                        Unit = MetricUnit.Count_per_Second,
                    )
                )
            ),
            StartTime = Timestamp.of(timestamp.minusSeconds(120)),
            EndTime = Timestamp.of(timestamp.plusSeconds(60)),
        )).successValue()
        val metricsList = cloudWatch(ListMetrics(
            MetricName = metricName,
            Namespace = namespace,
        )).successValue()
        assertThat(metricsList.Metrics, hasSize(equalTo(1)))
        val metric = metricsList.Metrics.first()
        assertThat(metric.MetricName, equalTo(metricName))
        assertThat(metric.Namespace, equalTo(namespace))
        val metricStatistics = cloudWatch.waitForMetricStatistics(
            MetricName = metricName,
            Namespace = namespace,
            StartTime = Timestamp.of(timestamp.minusSeconds(120)),
            EndTime = Timestamp.of(timestamp.plusSeconds(60)),
            Period = 60,
            Unit = MetricUnit.Count_per_Second,
            Statistics = listOf(Statistic.Maximum)
        )
        assertThat(metricStatistics.first().Maximum, equalTo(1.0))
    }
}
