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
    private String accessToken;

    @Bean
    public FacebookClient facebookClient() {
        return new DefaultFacebookClient(accessToken, Version.LATEST);
    }
}
