package com.interviai.backend.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date and time operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
public final class DateUtil {

    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    public static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATE_FORMAT);
    public static final DateTimeFormatter ISO_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(ISO_DATETIME_FORMAT);
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);
    public static final DateTimeFormatter DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATETIME_FORMAT);

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Get current date and time.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Get current date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Get current UTC date and time.
     */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * Convert LocalDateTime to UTC ZonedDateTime.
     */
    public static ZonedDateTime toUtc(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC"));
    }

    /**
     * Format LocalDateTime to ISO string.
     */
    public static String formatIso(LocalDateTime dateTime) {
        return dateTime.format(ISO_DATETIME_FORMATTER);
    }

    /**
     * Format LocalDate to ISO string.
     */
    public static String formatIso(LocalDate date) {
        return date.format(ISO_DATE_FORMATTER);
    }

    /**
     * Format LocalDateTime for display.
     */
    public static String formatDisplay(LocalDateTime dateTime) {
        return dateTime.format(DISPLAY_DATETIME_FORMATTER);
    }

    /**
     * Format LocalDate for display.
     */
    public static String formatDisplay(LocalDate date) {
        return date.format(DISPLAY_DATE_FORMATTER);
    }

    /**
     * Parse ISO date string to LocalDate.
     */
    public static LocalDate parseIsoDate(String dateString) {
        return LocalDate.parse(dateString, ISO_DATE_FORMATTER);
    }

    /**
     * Parse ISO datetime string to LocalDateTime.
     */
    public static LocalDateTime parseIsoDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, ISO_DATETIME_FORMATTER);
    }

    /**
     * Check if date is in the past.
     */
    public static boolean isPast(LocalDate date) {
        return date.isBefore(today());
    }

    /**
     * Check if datetime is in the past.
     */
    public static boolean isPast(LocalDateTime dateTime) {
        return dateTime.isBefore(now());
    }

    /**
     * Check if date is in the future.
     */
    public static boolean isFuture(LocalDate date) {
        return date.isAfter(today());
    }

    /**
     * Check if datetime is in the future.
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        return dateTime.isAfter(now());
    }

    /**
     * Calculate days between two dates.
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculate hours between two datetimes.
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * Calculate minutes between two datetimes.
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * Get start of day for given date.
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Get end of day for given date.
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(23, 59, 59, 999999999);
    }

    /**
     * Add days to date.
     */
    public static LocalDate addDays(LocalDate date, long days) {
        return date.plusDays(days);
    }

    /**
     * Add hours to datetime.
     */
    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * Add minutes to datetime.
     */
    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * Check if date is within range (inclusive).
     */
    public static boolean isWithinRange(LocalDate date, LocalDate start, LocalDate end) {
        return !date.isBefore(start) && !date.isAfter(end);
    }

    /**
     * Check if datetime is within range (inclusive).
     */
    public static boolean isWithinRange(LocalDateTime dateTime, LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    /**
     * Get age in years from birthdate.
     */
    public static int getAge(LocalDate birthDate) {
        return (int) ChronoUnit.YEARS.between(birthDate, today());
    }

    /**
     * Check if year is leap year.
     */
    public static boolean isLeapYear(int year) {
        return LocalDate.of(year, 1, 1).isLeapYear();
    }

    /**
     * Get first day of month.
     */
    public static LocalDate firstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    /**
     * Get last day of month.
     */
    public static LocalDate lastDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }
}