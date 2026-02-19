package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP Aspect for cross-cutting logging on all Service methods.
 * Logs: Entry/Exit, Execution Time, Exceptions (Type + Message).
 * Does NOT log sensitive data (passwords, tokens).
 * Beware of LazyLoading — only logs method name and safe parameters.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger LOG = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Pointcut targeting all methods in the service package.
     */
    @Pointcut("execution(* org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.*.*(..))")
    public void serviceMethods() {}

    /**
     * Around advice: logs entry, exit, execution time, and exceptions.
     */
    @Around("serviceMethods()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String safeArgs = sanitizeArgs(joinPoint.getArgs());

        LOG.info(">> ENTER {} with args [{}]", methodName, safeArgs);
        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            LOG.info("<< EXIT {} | duration={}ms", methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            LOG.error("!! EXCEPTION in {} | duration={}ms | type={} | message={}",
                    methodName, duration, ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * Sanitize arguments to avoid logging sensitive data like passwords or tokens.
     * Also avoids triggering LazyLoading by only logging toString of simple types.
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    String className = arg.getClass().getSimpleName();
                    // Don't log DTOs containing passwords
                    if (className.contains("Login") || className.contains("Password") || className.contains("Token")) {
                        return className + "{***}";
                    }
                    // For JPA entities, don't call toString to avoid LazyLoading
                    if (className.equals("Annonce") || className.equals("User") || className.equals("Category")) {
                        return className + "{id=?}";
                    }
                    return arg.toString();
                })
                .collect(Collectors.joining(", "));
    }
}
