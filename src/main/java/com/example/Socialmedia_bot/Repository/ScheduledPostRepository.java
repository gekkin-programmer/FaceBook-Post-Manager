package com.example.Socialmedia_bot.Repository;


import com.example.Socialmedia_bot.model.ScheduledPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ScheduledPostRepository extends JpaRepository<ScheduledPost, Long> {

    /**
     * Find all posts that are NOT posted yet
     * and whose scheduled time is BEFORE (or equal to) the given instant.
     */
    List<ScheduledPost> findByPostedFalseAndScheduledTimeBefore(Instant now);
}
