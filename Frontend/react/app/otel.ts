import { diag, DiagConsoleLogger, DiagLogLevel } from "@opentelemetry/api";
import { WebTracerProvider } from "@opentelemetry/sdk-trace-web";
import { BatchSpanProcessor } from "@opentelemetry/sdk-trace-base";
import { OTLPTraceExporter } from "@opentelemetry/exporter-trace-otlp-http";

import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { getWebAutoInstrumentations } from "@opentelemetry/auto-instrumentations-web";

import { resourceFromAttributes } from "@opentelemetry/resources";
import { SEMRESATTRS_SERVICE_NAME } from "@opentelemetry/semantic-conventions";

import {
    CompositePropagator,
    W3CBaggagePropagator,
    W3CTraceContextPropagator,
} from "@opentelemetry/core";

diag.setLogger(new DiagConsoleLogger(), DiagLogLevel.INFO);

export async function initOtel() {
    const { ZoneContextManager } = await import("@opentelemetry/context-zone");

    const provider = new WebTracerProvider({
        resource: resourceFromAttributes({
            [SEMRESATTRS_SERVICE_NAME]: "frontend-react",
        }),
        spanProcessors: [
            new BatchSpanProcessor(
                new OTLPTraceExporter({
                    // ton Collector (ex: 4319 si tu l’as décalé)
                    url: "/otel/v1/traces",
                })
            ),
        ],
    });

    provider.register({
        contextManager: new ZoneContextManager(),
        propagator: new CompositePropagator({
            propagators: [new W3CBaggagePropagator(), new W3CTraceContextPropagator()],
        }),
    });

    registerInstrumentations({
        tracerProvider: provider,
        instrumentations: [
            getWebAutoInstrumentations({
                "@opentelemetry/instrumentation-fetch": {
                    propagateTraceHeaderCorsUrls: [/^http:\/\/localhost:8080\/api\/.*/],
                    clearTimingResources: true,
                    // “logs” visibles dans Jaeger sous forme d’events
                    applyCustomAttributesOnSpan(span) {
                        span.addEvent("frontend.fetch.instrumented");
                    },
                },
            }),
        ],
    });
}
