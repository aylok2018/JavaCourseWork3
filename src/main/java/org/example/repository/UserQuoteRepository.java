package org.example.repository;

import org.example.entity.Quote;
import org.example.entity.User;
import org.example.entity.UserQuote;  // Оновлений імпорт
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuoteRepository extends JpaRepository<UserQuote, Long> {
    boolean existsByUserAndQuote(User user, Quote quote);
}
