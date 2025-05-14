package org.example.repository;

import org.example.entity.Quote;
import org.example.entity.Rating;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserAndQuote(User user, Quote quote);
}
