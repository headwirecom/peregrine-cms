package com.peregrine.functions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyExecutable;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes WinterCG-shaped functions (`export default async (request, context)
 * => response`) on a shared GraalJS engine.
 *
 * The capability surface is deliberately tiny and host-controlled: functions
 * see the request facts, `context.fetch` (blocking, limited to the tenant's
 * configured host allowlist), `context.config` (entries from the tenant's
 * OSGi factory configuration - the place secrets live, never the source) and
 * `context.log`. No repository access, no IO, no threads, no host classes:
 * everything crosses the boundary as polyglot proxies, so the context runs
 * with HostAccess.NONE.
 *
 * Sources are cached content-addressed (SHA-256 of the text): the same node
 * path serves BOTH the draft and the Published frozen text depending on the
 * request's version label, so a path-keyed cache would poison one side.
 *
 * The async contract settles synchronously because every await ultimately
 * resolves a blocking host call; a promise still pending after the handler
 * returns is an error, reported as such. Runaway scripts are interrupted
 * after the configured timeout and reported as 504.
 */
@Component(service = FunctionRunner.class)
@Designate(ocd = FunctionRunner.Configuration.class)
public class FunctionRunner {

    @ObjectClassDefinition(
        name = "Peregrine: Function Runner",
        description = "Executes per-tenant server-side JavaScript functions (GraalJS)"
    )
    public @interface Configuration {
        @AttributeDefinition(
            name = "Timeout (ms)",
            description = "A function still running after this long is interrupted (HTTP 504)"
        )
        long timeoutMs() default 10_000;

        @AttributeDefinition(
            name = "Max upstream response bytes",
            description = "context.fetch responses larger than this are refused"
        )
        int maxFetchBytes() default 4_194_304;
    }

    private static final Logger LOG = LoggerFactory.getLogger(FunctionRunner.class);
    private static final int SOURCE_CACHE_MAX = 256;

    private Engine engine;
    private ScheduledExecutorService watchdog;
    private volatile long timeoutMs;
    private volatile int maxFetchBytes;

    private final Map<String, Source> sources = java.util.Collections.synchronizedMap(
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Source> eldest) {
                    return size() > SOURCE_CACHE_MAX;
                }
            });

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** The outcome a function produced (or the error shape the servlet maps). */
    public static final class Outcome {
        public final int status;
        public final Map<String, String> headers;
        public final String body;

        Outcome(int status, Map<String, String> headers, String body) {
            this.status = status;
            this.headers = headers;
            this.body = body;
        }
    }

    /** Thrown for contract violations the servlet reports as 500/502/504. */
    public static final class FunctionException extends RuntimeException {
        public final int suggestedStatus;

        public FunctionException(int suggestedStatus, String message) {
            super(message);
            this.suggestedStatus = suggestedStatus;
        }

        public FunctionException(int suggestedStatus, String message, Throwable cause) {
            super(message, cause);
            this.suggestedStatus = suggestedStatus;
        }
    }

    @Activate
    protected void activate(Configuration configuration) {
        configure(configuration);
        watchdog = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread t = new Thread(r, "peregrine-functions-watchdog");
            t.setDaemon(true);
            return t;
        });
        // Truffle discovers languages via the context classloader; inside OSGi
        // that must be this bundle's loader (the jars are embedded here)
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            engine = Engine.newBuilder()
                    .option("engine.WarnInterpreterOnly", "false")
                    .build();
            LOG.info("functions engine up: {}", engine.getLanguages().keySet());
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    @Modified
    protected void configure(Configuration configuration) {
        timeoutMs = configuration.timeoutMs();
        maxFetchBytes = configuration.maxFetchBytes();
    }

    @Deactivate
    protected void deactivate() {
        if (watchdog != null) {
            watchdog.shutdownNow();
        }
        if (engine != null) {
            engine.close();
        }
    }

    /**
     * @param sourceText the function's ES module source (draft or frozen - the caller's resolver decided)
     * @param request    request facts, already reduced to plain maps/strings
     * @param settings   the tenant's resolved settings (fetch allowlist + config entries)
     * @param logName    tenant/function tag for the per-function logger
     */
    public Outcome run(String sourceText, Map<String, Object> request, TenantSettings settings, String logName) {
        final Source source = sourceFor(sourceText, logName);
        final ClassLoader previous = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        try (Context context = Context.newBuilder("js")
                .engine(engine)
                .allowHostAccess(HostAccess.NONE)
                .allowIO(false)
                .allowCreateThread(false)
                .allowNativeAccess(false)
                .option("js.esm-eval-returns-exports", "true")
                .build()) {
            final ScheduledFuture<?> kill = watchdog.schedule(() -> {
                try {
                    context.interrupt(Duration.ZERO);
                } catch (Exception e) {
                    LOG.warn("could not interrupt function {}", logName, e);
                }
            }, timeoutMs, TimeUnit.MILLISECONDS);
            try {
                final Value exports = context.eval(source);
                final Value handler = exports.getMember("default");
                if (handler == null || !handler.canExecute()) {
                    throw new FunctionException(500, "function has no executable default export");
                }
                final Value result = handler.execute(
                        ProxyObject.fromMap(request),
                        contextProxy(settings, logName));
                final Value settled = settle(result);
                if (settled == null) {
                    throw new FunctionException(500,
                            "function promise did not settle - timers are not available, use context.fetch for async work");
                }
                return toOutcome(settled);
            } finally {
                kill.cancel(false);
            }
        } catch (PolyglotException e) {
            if (e.isInterrupted() || e.isCancelled()) {
                throw new FunctionException(504, "function timed out after " + timeoutMs + " ms");
            }
            throw new FunctionException(500, "function failed: " + e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(previous);
        }
    }

    private Source sourceFor(String text, String name) {
        final String key = sha256(text);
        return sources.computeIfAbsent(key, k -> {
            try {
                return Source.newBuilder("js", text, name + ".mjs")
                        .mimeType("application/javascript+module")
                        .build();
            } catch (Exception e) {
                throw new FunctionException(500, "could not read function source", e);
            }
        });
    }

    private static String sha256(String text) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final StringBuilder sb = new StringBuilder(64);
            for (final byte b : digest.digest(text.getBytes(StandardCharsets.UTF_8))) {
                sb.append(Character.forDigit((b >> 4) & 0xf, 16)).append(Character.forDigit(b & 0xf, 16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Value settle(Value result) {
        if (result == null || !result.hasMember("then")) {
            return result;
        }
        final AtomicReference<Value> resolved = new AtomicReference<>();
        final AtomicReference<Value> rejected = new AtomicReference<>();
        result.invokeMember("then",
                (ProxyExecutable) args -> { resolved.set(args[0]); return null; },
                (ProxyExecutable) args -> { rejected.set(args[0]); return null; });
        if (rejected.get() != null) {
            throw new FunctionException(500, "function rejected: " + rejected.get());
        }
        return resolved.get();
    }

    private static Outcome toOutcome(Value settled) {
        if (!settled.hasMembers()) {
            throw new FunctionException(500, "function must return {status?, headers?, body}");
        }
        final int status = settled.hasMember("status") ? settled.getMember("status").asInt() : 200;
        final Map<String, String> headers = new LinkedHashMap<>();
        if (settled.hasMember("headers")) {
            final Value h = settled.getMember("headers");
            for (final String key : h.getMemberKeys()) {
                headers.put(key, h.getMember(key).asString());
            }
        }
        final String body = settled.hasMember("body") ? settled.getMember("body").asString() : "";
        return new Outcome(status, headers, body);
    }

    private ProxyObject contextProxy(TenantSettings settings, String logName) {
        final Logger fnLog = LoggerFactory.getLogger("com.peregrine.functions.fn." + logName);
        final Map<String, Object> ctx = new HashMap<>();
        ctx.put("fetch", (ProxyExecutable) args -> doFetch(settings, args));
        ctx.put("config", ProxyObject.fromMap(new HashMap<>(settings.entries())));
        final Map<String, Object> log = new HashMap<>();
        log.put("info", (ProxyExecutable) args -> { fnLog.info(join(args)); return null; });
        log.put("warn", (ProxyExecutable) args -> { fnLog.warn(join(args)); return null; });
        log.put("error", (ProxyExecutable) args -> { fnLog.error(join(args)); return null; });
        ctx.put("log", ProxyObject.fromMap(log));
        return ProxyObject.fromMap(ctx);
    }

    private static String join(Value[] args) {
        final StringBuilder sb = new StringBuilder();
        for (final Value arg : args) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(arg);
        }
        return sb.toString();
    }

    private Object doFetch(TenantSettings settings, Value[] args) {
        if (args.length < 1) {
            throw new FunctionException(500, "fetch(url[, {method, headers, body}])");
        }
        final String url = args[0].asString();
        final URI uri = URI.create(url);
        if (!settings.allowsHost(uri.getHost())) {
            throw new FunctionException(502,
                    "fetch to '" + uri.getHost() + "' is not in this site's allowlist");
        }
        String method = "GET";
        String body = null;
        final Map<String, String> headers = new LinkedHashMap<>();
        if (args.length > 1 && args[1].hasMembers()) {
            final Value opts = args[1];
            if (opts.hasMember("method")) {
                method = opts.getMember("method").asString().toUpperCase();
            }
            if (opts.hasMember("body")) {
                body = opts.getMember("body").asString();
            }
            if (opts.hasMember("headers")) {
                final Value h = opts.getMember("headers");
                for (final String key : h.getMemberKeys()) {
                    headers.put(key, h.getMember(key).asString());
                }
            }
        }
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(15))
                    .method(method, body == null
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(body));
            headers.forEach(builder::header);
            final HttpResponse<byte[]> upstream = http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            if (upstream.body().length > maxFetchBytes) {
                throw new FunctionException(502, "upstream response exceeds " + maxFetchBytes + " bytes");
            }
            final Map<String, Object> res = new HashMap<>();
            res.put("status", upstream.statusCode());
            res.put("ok", upstream.statusCode() >= 200 && upstream.statusCode() < 300);
            res.put("body", new String(upstream.body(), StandardCharsets.UTF_8));
            final Map<String, Object> responseHeaders = new LinkedHashMap<>();
            upstream.headers().map().forEach((k, v) -> responseHeaders.put(k, String.join(",", v)));
            res.put("headers", ProxyObject.fromMap(responseHeaders));
            return ProxyObject.fromMap(res);
        } catch (FunctionException e) {
            throw e;
        } catch (Exception e) {
            throw new FunctionException(502, "fetch failed: " + e.getMessage(), e);
        }
    }

    /** A tenant's resolved settings; see {@link TenantFunctionsConfig}. */
    public interface TenantSettings {
        boolean allowsHost(String host);
        Map<String, String> entries();
        List<String> hosts();
    }

}
