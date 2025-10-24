package com.example.Socialmedia_bot.config;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacebookConfig {
    @Value("${facebook.page.access.token:${facebook_page_access_token:}}")
    private String pageAccessToken;

    @Value("${facebook.app.secret:${facebook_app_secret:}}")
    private String appSecret;

    @Bean
    public FacebookClient facebookClient() {
        if (pageAccessToken == null || pageAccessToken.isEmpty()) {
            throw new IllegalStateException("facebook.page.access.token is not set");
        }
        if (appSecret == null || appSecret.isEmpty()) {
            throw new IllegalStateException("facebook.app.secret is not set");
        }
        System.out.println("Creating FacebookClient with token: " + (pageAccessToken != null ? "present" : "null"));
        System.out.println("App Secret: " + (appSecret != null ? "present" : "null"));
        return new DefaultFacebookClient(pageAccessToken, appSecret, Version.LATEST);
    }
}
