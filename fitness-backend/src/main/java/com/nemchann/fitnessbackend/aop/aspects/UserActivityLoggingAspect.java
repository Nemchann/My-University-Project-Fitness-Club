package com.nemchann.fitnessbackend.aop.aspects;

import com.nemchann.fitnessbackend.aop.annotations.LogActivity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class UserActivityLoggingAspect {

    @Before("@annotation(logActivity)")
    public void logAdminAction(JoinPoint joinPoint, LogActivity logActivity) {
        // Получаем имя метода бэкенда
        String methodName = joinPoint.getSignature().getName();

        // Получаем аргументы, которые пришли в метод
        Object[] args = joinPoint.getArgs();

        // Достаем текст из аннотации
        String actionDescription = logActivity.value();

        log.info("Действие: {}", actionDescription);
        log.info("Вызван метод сервиса: {}", methodName);
        log.info("Входные параметры: {}", Arrays.toString(args));
    }
}
