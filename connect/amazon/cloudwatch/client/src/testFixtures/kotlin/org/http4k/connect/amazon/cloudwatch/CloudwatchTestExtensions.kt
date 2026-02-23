package org.http4k.connect.amazon.cloudwatch

import org.http4k.connect.amazon.cloudwatch.action.AlarmsDescribed
import org.http4k.connect.amazon.cloudwatch.action.DescribeAlarms
import org.http4k.connect.amazon.cloudwatch.action.GetMetricStatistics
import org.http4k.connect.amazon.cloudwatch.action.ListMetrics
import org.http4k.connect.amazon.cloudwatch.model.AlarmName
import org.http4k.connect.amazon.cloudwatch.model.AlarmType
import org.http4k.connect.amazon.cloudwatch.model.MetricName
import org.http4k.connect.amazon.cloudwatch.model.MetricUnit
import org.http4k.connect.amazon.cloudwatch.model.Namespace
import org.http4k.connect.amazon.cloudwatch.model.Statistic
import org.http4k.connect.model.Timestamp
import org.http4k.connect.successValue
import org.junit.jupiter.api.fail
import java.time.Duration
import java.time.Instant

private val defaultTimeout = Duration.ofSeconds(10)
private val defaultPollInterval = Duration.ofSeconds(1)

fun CloudWatch.waitForMetricCreation(
    namespace: Namespace,
    metricName: MetricName,
    timeout: Duration = defaultTimeout,
    pollInterval: Duration = defaultPollInterval
) = waitFor(timeout, pollInterval) {
    invoke(ListMetrics(Namespace = namespace, MetricName = metricName)).successValue().Metrics.firstOrNull()
}

fun CloudWatch.waitForMetricStatistics(
    Namespace: Namespace,
    MetricName: MetricName,
    StartTime: Timestamp,
    EndTime: Timestamp,
    Period: Int,
    Unit: MetricUnit,
    Statistics: List<Statistic>,
    timeout: Duration = defaultTimeout.multipliedBy(2),
    pollInterval: Duration = defaultPollInterval.multipliedBy(2)
) = waitFor(timeout, pollInterval) {
    invoke(GetMetricStatistics(
        MetricName = MetricName,
        Namespace = Namespace,
        StartTime = StartTime,
        EndTime = EndTime,
        Period = Period,
        Unit = Unit,
        Statistics = Statistics
    )).successValue()
        .Datapoints
        .takeIf { it.isNotEmpty() }
}

fun CloudWatch.waitForMetricAlarmCreation(
    alarmName: AlarmName,
    timeout: Duration = defaultTimeout,
    pollInterval: Duration = defaultPollInterval
) = waitFor(timeout, pollInterval) {
    invoke(DescribeAlarms(AlarmNames = listOf(alarmName), AlarmTypes = listOf(AlarmType.MetricAlarm)))
        .successValue()
        .MetricAlarms
        ?.firstOrNull()
}

fun CloudWatch.waitForCompositeAlarmCreation(
    alarmName: AlarmName,
    timeout: Duration = defaultTimeout,
    pollInterval: Duration = defaultPollInterval
) = waitFor(timeout, pollInterval) {
    invoke(DescribeAlarms(AlarmNames = listOf(alarmName), AlarmTypes = listOf(AlarmType.CompositeAlarm)))
        .successValue()
        .CompositeAlarms
        ?.firstOrNull()
}

private fun <T: Any> waitFor(timeout: Duration, pollInterval: Duration, returnFn: () -> T?): T {
    val start = Instant.now()
    while (Duration.between(start, Instant.now()) < timeout) {
        returnFn()?.let { return it }
        Thread.sleep(pollInterval)
    }
    fail("Condition not met within $timeout")
}
