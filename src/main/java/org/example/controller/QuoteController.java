package org.example.controller;

import org.example.entity.User;

import org.example.entity.Quote;
import org.example.repository.UserRepository;
import org.example.repository.QuoteRepository;
import org.example.service.QuoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Tag(name = "Quotes", description = "Операції з цитатами")
@RestController
@RequestMapping("/quotes")
public class QuoteController {

    private final QuoteService quoteService;
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;


    @Autowired
    public QuoteController(UserRepository userRepository, QuoteRepository quoteRepository, QuoteService quoteService) {
        this.userRepository = userRepository;
        this.quoteRepository = quoteRepository;
        this.quoteService = quoteService;
    }

    @Operation(summary = "Отримати всі цитати")
    @GetMapping
    public List<Quote> getAllQuotes() {
        return quoteService.getAllQuotes();
    }

    @Operation(summary = "Отримати випадкову цитату")
    @ApiResponse(responseCode = "200", description = "Успішно отримано випадкову цитату")
    @GetMapping("/random")
    public Quote getRandomQuote() {
        return quoteService.getRandomQuote();
    }

    @Operation(summary = "Додати нову цитату")
    @ApiResponse(responseCode = "201", description = "Цитату створено")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quote createQuote(@RequestBody Quote quote, Principal principal) {
        String email = principal.getName();
        System.out.println("EMAIL: " + email);
        return quoteService.createQuote(quote, email);
    }


    @Operation(summary = "Отримати цитати за категорією")
    @GetMapping("/category/{category}")
    public List<Quote> getQuotesByCategory(@PathVariable String category) {
        return quoteService.getQuotesByCategory(category);
    }

    @Operation(summary = "Оцінити цитату")
    @PostMapping("/{id}/rate")
    public Quote rateQuote(@PathVariable Long id,
                           @RequestParam int rating,
                           Principal principal) {
        String userEmail = principal.getName();
        User user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found"));

        return quoteService.rateQuote(id, user.getId(), rating);
    }


    @Operation(summary = "Додати до обраних")
    @PostMapping("/{id}/favorite")
    public ResponseEntity<Void> addToFavorites(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        System.out.println("User email: " + email);

        User user = Optional.ofNullable(userRepository.findByEmail(email))
                .orElseThrow(() -> {
                    System.err.println("User not found: " + email);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        System.out.println("Found user: " + user);

        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> {
                    System.err.println("Quote not found for id: " + id);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quote not found");
                });

        System.out.println("Found quote: " + quote);

        quoteService.addToFavorites(quote.getId(), user.getId());

        System.out.println("Quote with id " + id + " added to favorites for user " + email);

        return ResponseEntity.ok().build();
    }





    @Operation(summary = "Видалити цитату (тільки для адміністратора)")
    @DeleteMapping("/{id}")
    public void deleteQuote(@PathVariable Long id, Principal principal) throws AccessDeniedException {
        // Отримуємо email з Principal
        String email = principal.getName();
        System.out.println("Користувач " + email + " намагається видалити цитату з ID: " + id);

        // Отримуємо користувача за email з бази даних
        User user = userRepository.findByEmail(email);
        if (user == null) {
            System.out.println("Користувач з email " + email + " не знайдений");
            throw new AccessDeniedException("Користувача не знайдено");
        }

        // Перевіряємо, чи є в користувача права адміністратора
        if (!user.getRole().equalsIgnoreCase("ROLE_ADMIN")) {
            System.out.println("Користувач " + email + " не має прав адміністратора");
            throw new AccessDeniedException("Доступ заборонено");
        }

        System.out.println("Користувач " + email + " має права адміністратора, спробує видалити цитату");
        quoteService.deleteQuote(id, user.getId());
    }




}
