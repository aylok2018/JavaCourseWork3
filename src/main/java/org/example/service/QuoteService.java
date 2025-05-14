package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.util.*;

@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final CategoryRepository categoryRepository;
    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final UserQuoteRepository userQuoteRepository;

    @Autowired
    public QuoteService(QuoteRepository quoteRepository,
                        CategoryRepository categoryRepository,
                        RatingRepository ratingRepository,
                        UserRepository userRepository,
                        UserQuoteRepository userQuoteRepository) {
        this.quoteRepository = quoteRepository;
        this.categoryRepository = categoryRepository;
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.userQuoteRepository = userQuoteRepository;
    }

    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public Quote getRandomQuote() {
        List<Quote> quotes = quoteRepository.findAll();
        if (!quotes.isEmpty()) {
            Random rand = new Random();
            return quotes.get(rand.nextInt(quotes.size()));
        }
        return null;
    }

    public Quote createQuote(Quote quote, String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("Користувача не знайдено");
        }
        return quoteRepository.save(quote);
    }

    public List<Quote> getQuotesByCategory(String categoryName) {
        Category category = categoryRepository.findByName(categoryName);

        if (category == null) {
            category = new Category(categoryName);
            category = categoryRepository.save(category);
        }

        return quoteRepository.findAllByCategory(category);
    }

    public Quote rateQuote(Long quoteId, Long userId, int ratingValue) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Цитату не знайдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        Rating rating = ratingRepository.findByUserAndQuote(user, quote)
                .orElse(new Rating(user, quote, 0));
        rating.setValue(ratingValue);
        ratingRepository.save(rating);
        return quote;
    }


    public void addToFavorites(Long quoteId, Long userId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Цитату не знайдено"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        if (!userQuoteRepository.existsByUserAndQuote(user, quote)) {
            UserQuote userQuote = new UserQuote(user, quote);
            userQuoteRepository.save(userQuote);
        }
    }


    public void deleteQuote(Long quoteId, Long userId) throws AccessDeniedException {

        System.out.println("Видалення цитати. quoteId: " + quoteId + ", userId: " + userId);

        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Цитату не знайдено"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        System.out.println("Роль користувача: " + user.getRole());

        if (!user.getRole().equals("ROLE_ADMIN")) {
            throw new AccessDeniedException("Тільки адміністратор може видаляти цитати");
        }

        quoteRepository.delete(quote);

        System.out.println("Цитата успішно видалена");
    }



}
