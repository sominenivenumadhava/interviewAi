package com.interviai.backend.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StringUtil class.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
class StringUtilTest {

    @Test
    void testIsEmpty() {
        assertTrue(StringUtil.isEmpty(null));
        assertTrue(StringUtil.isEmpty(""));
        assertFalse(StringUtil.isEmpty(" "));
        assertFalse(StringUtil.isEmpty("test"));
    }

    @Test
    void testIsNotEmpty() {
        assertFalse(StringUtil.isNotEmpty(null));
        assertFalse(StringUtil.isNotEmpty(""));
        assertTrue(StringUtil.isNotEmpty(" "));
        assertTrue(StringUtil.isNotEmpty("test"));
    }

    @Test
    void testIsBlank() {
        assertTrue(StringUtil.isBlank(null));
        assertTrue(StringUtil.isBlank(""));
        assertTrue(StringUtil.isBlank(" "));
        assertTrue(StringUtil.isBlank("   "));
        assertFalse(StringUtil.isBlank("test"));
        assertFalse(StringUtil.isBlank(" test "));
    }

    @Test
    void testIsNotBlank() {
        assertFalse(StringUtil.isNotBlank(null));
        assertFalse(StringUtil.isNotBlank(""));
        assertFalse(StringUtil.isNotBlank(" "));
        assertTrue(StringUtil.isNotBlank("test"));
    }

    @Test
    void testCapitalize() {
        assertNull(StringUtil.capitalize(null));
        assertEquals("", StringUtil.capitalize(""));
        assertEquals("Test", StringUtil.capitalize("test"));
        assertEquals("Test", StringUtil.capitalize("TEST"));
        assertEquals("Test", StringUtil.capitalize("Test"));
    }

    @Test
    void testToCamelCase() {
        assertEquals("", StringUtil.toCamelCase(""));
        assertEquals("test", StringUtil.toCamelCase("test"));
        assertEquals("testString", StringUtil.toCamelCase("test string"));
        assertEquals("testStringValue", StringUtil.toCamelCase("test_string_value"));
        assertEquals("testStringValue", StringUtil.toCamelCase("test-string-value"));
    }

    @Test
    void testToSnakeCase() {
        assertEquals("", StringUtil.toSnakeCase(""));
        assertEquals("test", StringUtil.toSnakeCase("test"));
        assertEquals("test_string", StringUtil.toSnakeCase("testString"));
        assertEquals("test_string_value", StringUtil.toSnakeCase("TestStringValue"));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(StringUtil.isValidEmail("test@example.com"));
        assertTrue(StringUtil.isValidEmail("user.name+tag@domain.co.uk"));
        assertFalse(StringUtil.isValidEmail(""));
        assertFalse(StringUtil.isValidEmail("invalid-email"));
        assertFalse(StringUtil.isValidEmail("@domain.com"));
        assertFalse(StringUtil.isValidEmail("user@"));
    }

    @Test
    void testIsValidPhoneNumber() {
        assertTrue(StringUtil.isValidPhoneNumber("+1234567890"));
        assertTrue(StringUtil.isValidPhoneNumber("1234567890"));
        assertFalse(StringUtil.isValidPhoneNumber(""));
        assertFalse(StringUtil.isValidPhoneNumber("+"));
        assertFalse(StringUtil.isValidPhoneNumber("abc123"));
    }

    @Test
    void testMaskEmail() {
        assertEquals("te******@example.com", StringUtil.maskEmail("testuser@example.com"));
        assertEquals("**@example.com", StringUtil.maskEmail("ab@example.com"));
        assertEquals("invalid-email", StringUtil.maskEmail("invalid-email"));
        assertEquals("", StringUtil.maskEmail(""));
        assertNull(StringUtil.maskEmail(null));
    }

    @Test
    void testMaskPhoneNumber() {
        assertEquals("******7890", StringUtil.maskPhoneNumber("1234567890"));
        assertEquals("****4567", StringUtil.maskPhoneNumber("+1234567"));
        assertEquals("123", StringUtil.maskPhoneNumber("123"));
        assertEquals("", StringUtil.maskPhoneNumber(""));
        assertNull(StringUtil.maskPhoneNumber(null));
    }

    @Test
    void testTruncate() {
        assertEquals("test", StringUtil.truncate("test", 10));
        assertEquals("test...", StringUtil.truncate("test string", 7));
        assertEquals("", StringUtil.truncate("", 5));
        assertNull(StringUtil.truncate(null, 5));
    }

    @Test
    void testToSlug() {
        assertEquals("hello-world", StringUtil.toSlug("Hello World"));
        assertEquals("test-string-123", StringUtil.toSlug("Test String 123!"));
        assertEquals("", StringUtil.toSlug(""));
        assertEquals("", StringUtil.toSlug("!@#$%"));
    }
}