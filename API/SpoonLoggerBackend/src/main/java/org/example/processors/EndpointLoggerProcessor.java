package org.example.processors;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.*;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;
import spoon.reflect.factory.Factory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EndpointLoggerProcessor extends AbstractProcessor<CtMethod<?>> {

    private static final Set<String> CTRL_SIMPLE = Set.of("restcontroller", "controller");
    private static final Set<String> MAP_SIMPLE  = Set.of("getmapping","postmapping","putmapping",
            "deletemapping","patchmapping","requestmapping");

    private static final Pattern ANN_TOSTRING = Pattern.compile("@\\s*([A-Za-z0-9_$.]+)");
    private static final Pattern FIRST_QUOTED = Pattern.compile("\"([^\"]+)\"");

    // stats
    private int seen=0, inCtrl=0, hasMap=0, injected=0, skipNoBody=0, skipAlready=0, skipNotEligible=0, skipNoMap=0;

    @Override
    public boolean isToBeProcessed(CtMethod<?> m) {
        seen++;
        CtType<?> owner = m.getDeclaringType();
        if (owner == null) { skipNotEligible++; return false; }

        // DEBUG
        System.out.println("[PROC] Class " + qn(owner) + " anns=" + debugAnnList(owner.getAnnotations()));
        System.out.println("[PROC] Method " + qn(owner) + "." + m.getSimpleName() + " anns=" + debugAnnList(m.getAnnotations()));

        boolean classIsController = owner.getAnnotations().stream().map(this::simpleAnn).anyMatch(CTRL_SIMPLE::contains);
        boolean classHasMapping   = owner.getAnnotations().stream().map(this::simpleAnn).anyMatch(MAP_SIMPLE::contains);
        boolean methodHasMapping  = m.getAnnotations().stream().map(this::simpleAnn).anyMatch(MAP_SIMPLE::contains);

        // Critère élargi pragmatique
        boolean eligible = classIsController || classHasMapping || methodHasMapping;
        if (!eligible) {
            System.out.println("[PROC] " + qn(owner) + "." + m.getSimpleName() +
                    " -> skip (class not controller, no class-level mapping, no method mapping)");
            skipNotEligible++;
            return false;
        }
        inCtrl++;

        if (!methodHasMapping) {
            System.out.println("[PROC] " + qn(owner) + "." + m.getSimpleName() + " -> skip (no method-level mapping)");
            skipNoMap++;
            return false;
        }
        hasMap++;
        return true;
    }

    @Override
    public void process(CtMethod<?> m) {
        CtType<?> owner = m.getDeclaringType();
        String fqMethod = qn(owner) + "." + m.getSimpleName();
        Factory f = getFactory();

        // @Slf4j si absent (détection par simple name)
        final String SLF4J_ANN_FQCN = "lombok.extern.slf4j.Slf4j";
        boolean hasSlf4j = owner.getAnnotations().stream().anyMatch(a -> "slf4j".equals(simpleAnn(a)));
        if (!hasSlf4j) {
            owner.addAnnotation(f.createAnnotation(f.Type().createReference(SLF4J_ANN_FQCN)));
            System.out.println("[PROC] " + qn(owner) + " -> add @Slf4j");
        }

        CtBlock<?> body = m.getBody();
        if (body == null) { System.out.println("[PROC] " + fqMethod + " -> skip (no body)"); skipNoBody++; return; }

        String marker = "__instrumented_api_call__";
        boolean already = body.getStatements().stream().anyMatch(s -> s.toString().contains(marker));
        if (already) { System.out.println("[PROC] " + fqMethod + " -> skip (already instrumented)"); skipAlready++; return; }

        String http = inferHttpMethod(m);
        String path = inferFullPath(m); // *** SAFE ***
        String resource = inferResource(path);
        String opType = inferOpType(http);
        if (isExpensiveSearch(path)) opType = "SEARCH_EXPENSIVE";

        StringBuilder snippet = new StringBuilder();
        snippet.append("// ").append(marker).append("\n");
        snippet.append("org.slf4j.MDC.put(\"opType\", \"").append(opType).append("\");\n");
        snippet.append("org.slf4j.MDC.put(\"resource\", \"").append(resource).append("\");\n");
        snippet.append("org.slf4j.MDC.put(\"path\", \"").append(path).append("\");\n");
        snippet.append("log.info(\"api_call begin\");\n");

        CtStatement code = f.Code().createCodeSnippetStatement(snippet.toString());
        body.insertBegin(code);

        injected++;
        System.out.println("[PROC] " + fqMethod + " -> INJECTED {method=" + http
                + ", path=\"" + path + "\", resource=\"" + resource + "\", opType=" + opType + "}");
    }

    @Override
    public void processingDone() {
        System.out.println("----- [PROC SUMMARY] -----");
        System.out.println("seenMethods      = " + seen);
        System.out.println("inCtrlOrMapped   = " + inCtrl);
        System.out.println("hasMapping       = " + hasMap);
        System.out.println("injected         = " + injected);
        System.out.println("skippedNoBody    = " + skipNoBody);
        System.out.println("skippedAlready   = " + skipAlready);
        System.out.println("skippedNotEligible = " + skipNotEligible);
        System.out.println("skippedNoMapping = " + skipNoMap);
        System.out.println("--------------------------");
    }

    // ---------- helpers robustes ----------

    private String qn(CtType<?> t) { return (t != null && t.getQualifiedName() != null) ? t.getQualifiedName() : "<unnamed>"; }

    private String simpleAnn(CtAnnotation<?> a) {
        try {
            if (a.getAnnotationType() != null) {
                String q = a.getAnnotationType().getQualifiedName();
                if (q != null && !q.isBlank()) return lastSeg(q).toLowerCase(Locale.ROOT);
                String s = a.getAnnotationType().getSimpleName();
                if (s != null && !s.isBlank()) return s.toLowerCase(Locale.ROOT);
            }
            String raw = String.valueOf(a);
            Matcher m = ANN_TOSTRING.matcher(raw);
            if (m.find()) return lastSeg(m.group(1)).toLowerCase(Locale.ROOT);
        } catch (Throwable ignored) {}
        return "";
    }

    private List<String> debugAnnList(Collection<CtAnnotation<?>> as) {
        List<String> out = new ArrayList<>();
        for (CtAnnotation<?> a : as) {
            String q=null, s=null, raw=String.valueOf(a);
            try { if (a.getAnnotationType()!=null) q=a.getAnnotationType().getQualifiedName(); } catch(Throwable ignore){}
            try { if (a.getAnnotationType()!=null) s=a.getAnnotationType().getSimpleName(); } catch(Throwable ignore){}
            out.add("{q="+q+", s="+s+", raw="+raw+" → simple="+simpleAnn(a)+"}");
        }
        return out;
    }

    private static String lastSeg(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0 ? name.substring(i+1) : name);
    }

    private String inferHttpMethod(CtMethod<?> m) {
        return m.getAnnotations().stream().map(this::simpleAnn).filter(MAP_SIMPLE::contains).map(n -> {
            return switch (n) {
                case "getmapping" -> "GET";
                case "postmapping" -> "POST";
                case "putmapping" -> "PUT";
                case "deletemapping" -> "DELETE";
                case "patchmapping" -> "PATCH";
                default -> "REQUEST";
            };
        }).findFirst().orElse("REQUEST");
    }

    private String inferFullPath(CtMethod<?> m) {
        String classPath  = extractPathFromAnnotationsSafe(m.getDeclaringType().getAnnotations());
        String methodPath = extractPathFromAnnotationsSafe(m.getAnnotations());
        if (classPath == null) classPath = "";
        if (methodPath == null) methodPath = "";
        String path = ("/" + (classPath + "/" + methodPath)).replaceAll("/+", "/");
        return path.equals("/") ? "" : path;
    }

    /** SAFE: ne déclenche pas de chargement de classe; parse CtExpression ou fallback toString. */
    private String extractPathFromAnnotationsSafe(Collection<CtAnnotation<?>> annos) {
        for (CtAnnotation<?> a : annos) {
            String simple = simpleAnn(a);
            if (!MAP_SIMPLE.contains(simple)) continue;

            // 1) lire la map des expressions sans réflexion
            Map<String, CtExpression> values = a.getValues();
            CtExpression<?> expr = values.get("value");
            if (expr == null) expr = values.get("path");

            // 2) essayer d’extraire proprement
            String s = tryExtractStringFromExpr(expr);
            if (s != null) return trimSlashes(s);

            // 3) fallback: parse toString() ex: @GetMapping("/products")
            String raw = String.valueOf(a);
            Matcher m = FIRST_QUOTED.matcher(raw);
            if (m.find()) return trimSlashes(m.group(1));
        }
        return null;
    }

    private String tryExtractStringFromExpr(CtExpression<?> expr) {
        if (expr == null) return null;

        // ex: "products"
        if (expr instanceof CtLiteral<?> lit && lit.getValue() instanceof String str) {
            return str;
        }

        // ex: {"products", "/v1"} → prendre le premier
        if (expr instanceof CtNewArray<?> arr && !arr.getElements().isEmpty()) {
            CtExpression<?> e0 = arr.getElements().get(0);
            if (e0 instanceof CtLiteral<?> lit0 && lit0.getValue() instanceof String s0) {
                return s0;
            }
        }

        // ex: expression complexe → fallback sur toString() pour attraper "..."
        String txt = expr.toString();
        Matcher m = FIRST_QUOTED.matcher(txt);
        if (m.find()) return m.group(1);
        return null;
    }

    private String trimSlashes(String s) { return s == null ? null : s.replaceAll("^/+", "").replaceAll("/+$", ""); }

    private String inferResource(String path) {
        if (path == null || path.isBlank()) return "root";
        String p = path.startsWith("/") ? path.substring(1) : path;
        int idx = p.indexOf('/');
        return (idx < 0 ? p : p.substring(0, idx)).toLowerCase(Locale.ROOT);
    }

    private String inferOpType(String method) {
        return switch (method) {
            case "GET" -> "READ";
            case "POST", "PUT", "DELETE", "PATCH" -> "WRITE";
            default -> "READ";
        };
    }

    private boolean isExpensiveSearch(String path) {
        String p = Optional.ofNullable(path).orElse("").toLowerCase(Locale.ROOT);
        return p.contains("expensive") || p.contains("search");
    }
}
