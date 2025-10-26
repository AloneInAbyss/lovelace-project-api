package com.aloneinabyss.lovelace.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Configuration for internationalization (i18n) support.
 * Configures locale resolution based on Accept-Language header and message source for translations.
 */
@Configuration
public class LocaleConfig {
    
    /**
     * Supported locales for the application
     */
    private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
        Locale.US,                    // en-US
        new Locale("pt", "BR")        // pt-BR
    );
    
    /**
     * Default locale when no Accept-Language header is provided or locale is not supported
     */
    private static final Locale DEFAULT_LOCALE = Locale.US; // en-US
    
    /**
     * Configure locale resolver to use Accept-Language header.
     * Falls back to default locale if header is missing or contains unsupported locale.
     *
     * @return LocaleResolver that reads from Accept-Language header
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(SUPPORTED_LOCALES);
        localeResolver.setDefaultLocale(DEFAULT_LOCALE);
        return localeResolver;
    }
    
    /**
     * Configure message source for loading internationalized messages.
     * Messages are loaded from messages_*.properties files in the resources/i18n folder.
     *
     * @return MessageSource for loading translations
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setCacheSeconds(3600); // Cache for 1 hour
        return messageSource;
    }
}
