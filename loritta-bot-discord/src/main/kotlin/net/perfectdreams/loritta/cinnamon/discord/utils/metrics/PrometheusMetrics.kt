package net.perfectdreams.loritta.cinnamon.discord.utils.metrics

import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.Histogram

open class PrometheusMetrics {
    fun createCounterWithLabels(name: String, help: String, vararg labels: String, action: Counter.Builder.() -> (Unit) = {}): Counter = Counter.build(name, help)
        .labelNames(*labels)
        .apply(action)
        .register()

    fun createGaugeWithLabels(name: String, help: String, vararg labels: String, action: Gauge.Builder.() -> (Unit) = {}): Gauge = Gauge.build(name, help)
        .labelNames(*labels)
        .apply(action)
        .register()

    fun createHistogramWithLabels(name: String, help: String, vararg labels: String, action: Histogram.Builder.() -> (Unit) = {}): Histogram = Histogram.build(name, help)
        .labelNames(*labels)
        .apply(action)
        .register()
}