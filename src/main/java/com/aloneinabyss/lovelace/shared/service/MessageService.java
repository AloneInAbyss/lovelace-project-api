package com.aloneinabyss.lovelace.shared.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for retrieving internationalized messages.
 * Provides convenient methods to get localized messages based on the current locale.
 */
@Service
@RequiredArgsConstructor
public class MessageService {
    
    private final MessageSource messageSource;
    
    /**
     * Get a localized message for the given key using the current locale from the request context.
     *
     * @param key The message key
     * @return The localized message
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }
    
    /**
     * Get a localized message for the given key with arguments using the current locale.
     *
     * @param key The message key
     * @param args Arguments to be inserted into the message
     * @return The localized message with arguments
     */
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * Get a localized message for the given key with a default message fallback.
     *
     * @param key The message key
     * @param defaultMessage Default message if key is not found
     * @return The localized message or default message
     */
    public String getMessage(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }
    
    /**
     * Get a localized message for the given key with arguments and default message fallback.
     *
     * @param key The message key
     * @param defaultMessage Default message if key is not found
     * @param args Arguments to be inserted into the message
     * @return The localized message with arguments or default message
     */
    public String getMessage(String key, String defaultMessage, Object... args) {
        return messageSource.getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }
    
    /**
     * Get a localized message for the given key using a specific locale.
     *
     * @param key The message key
     * @param locale The locale to use
     * @return The localized message
     */
    public String getMessage(String key, Locale locale) {
        return messageSource.getMessage(key, null, locale);
    }
    
    /**
     * Get a localized message for the given key with arguments using a specific locale.
     *
     * @param key The message key
     * @param locale The locale to use
     * @param args Arguments to be inserted into the message
     * @return The localized message with arguments
     */
    public String getMessage(String key, Locale locale, Object... args) {
        return messageSource.getMessage(key, args, locale);
    }
}
