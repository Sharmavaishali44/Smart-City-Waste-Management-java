package utils;

import java.util.regex.Pattern;

/**
 * Validation utilities for municipal complaint forms and user registrations.
 */
public class ValidationUtils {

    // Indian phone numbers (10 digits optionally prefixed with +91, 91, or 0, starting with 6, 7, 8, or 9)
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:\\+91|91|0)?[6-9]\\d{9}$");

    // Standard RFC 5322 compatible email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    public static boolean isValidMobile(String phone) {
        if (phone == null) return false;
        String clean = phone.trim().replaceAll("[\\s-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    public static String sanitizePhone(String phone) {
        if (phone == null) return "";
        String clean = phone.trim().replaceAll("[\\s-]", "");
        if (clean.startsWith("+91")) clean = clean.substring(3);
        else if (clean.startsWith("91") && clean.length() == 12) clean = clean.substring(2);
        else if (clean.startsWith("0") && clean.length() == 11) clean = clean.substring(1);
        return clean;
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return true; // Optional field
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isNotBlank(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean hasMinLength(String text, int minLength) {
        return text != null && text.trim().length() >= minLength;
    }
}
