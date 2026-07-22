package com.interviai.backend.common.util;

import org.slf4j.LoggerFactory;

/**
 * Simple logger utility to replace Lombok's @Slf4j
 */
public class Logger {
    
    public static org.slf4j.Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}