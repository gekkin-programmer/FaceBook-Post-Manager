package com.example.Socialmedia_bot.config;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Version;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FacebookConfig {
    @Value("${facebook.page.access.token}")
    private String pageAccessToken;

    @Value("${facebook.app.secret}")
    private String appSecret;

    @Bean
    public FacebookClient facebookClient() {
        System.out.println("Creating FacebookClient with token: " + (pageAccessToken != null ? "present" : "null"));
        System.out.println("App Secret: " + (appSecret != null ? "present" : "null"));
        return new DefaultFacebookClient(pageAccessToken, appSecret, Version.LATEST);
    }
}
