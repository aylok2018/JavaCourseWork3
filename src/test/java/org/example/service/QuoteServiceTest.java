package org.example.service;

import org.example.entity.*;
import org.example.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class QuoteServiceTest {

    @InjectMocks
    private QuoteService quoteService;

    @Mock private QuoteRepository quoteRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RatingRepository ratingRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserQuoteRepository userQuoteRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllQuotes_returnsAllQuotes() {
        System.out.println("Тест: getAllQuotes_returnsAllQuotes\n" +
                "- Підготовка: створено список із 2-х цитат.\n" +
                "- Очікування: метод має повернути ті самі 2 цитати, що повертає репозиторій.\n" +
                "- Перевірка: результат має містити 2 елементи.");
        List<Quote> mockQuotes = List.of(new Quote(), new Quote());
        when(quoteRepository.findAll()).thenReturn(mockQuotes);
        List<Quote> result = quoteService.getAllQuotes();
        assertEquals(2, result.size());
    }

    @Test
    void getRandomQuote_returnsRandomQuote() {
        System.out.println("Тест: getRandomQuote_returnsRandomQuote\n" +
                "- Підготовка: репозиторій повертає список із 3 цитат.\n" +
                "- Очікування: метод має повернути одну з них (випадкову).\n" +
                "- Перевірка: результат не повинен бути null.");
        List<Quote> mockQuotes = List.of(new Quote(), new Quote(), new Quote());
        when(quoteRepository.findAll()).thenReturn(mockQuotes);
        Quote result = quoteService.getRandomQuote();
        assertNotNull(result);
    }

    @Test
    void createQuote_savesAndReturnsQuote() {
        System.out.println("Тест: createQuote_savesAndReturnsQuote\n" +
                "- Підготовка: створюється нова цитата, існує користувач з email.\n" +
                "- Дія: виклик методу createQuote.\n" +
                "- Очікування: цитата збережена і повернута без змін.");
        Quote quote = new Quote();
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);
        when(quoteRepository.save(quote)).thenReturn(quote);
        Quote result = quoteService.createQuote(quote, "test@example.com");
        assertEquals(quote, result);
    }

    @Test
    void getQuotesByCategory_existingCategory_returnsQuotes() {
        System.out.println("Тест: getQuotesByCategory_existingCategory_returnsQuotes\n" +
                "- Підготовка: існує категорія 'Motivation', вона містить 1 цитату.\n" +
                "- Дія: виклик методу з назвою категорії.\n" +
                "- Очікування: повертається список із 1 цитатою.");
        Category category = new Category("Motivation");
        List<Quote> quotes = List.of(new Quote());
        when(categoryRepository.findByName("Motivation")).thenReturn(category);
        when(quoteRepository.findAllByCategory(category)).thenReturn(quotes);
        List<Quote> result = quoteService.getQuotesByCategory("Motivation");
        assertEquals(1, result.size());
    }

    @Test
    void getQuotesByCategory_newCategory_createsAndReturnsQuotes() {
        System.out.println("Тест: getQuotesByCategory_newCategory_createsAndReturnsQuotes\n" +
                "- Підготовка: категорія 'Life' не існує.\n" +
                "- Дія: категорія створюється і зберігається.\n" +
                "- Очікування: метод повертає порожній список цитат.");
        Category newCategory = new Category("Life");
        when(categoryRepository.findByName("Life")).thenReturn(null);
        when(categoryRepository.save(any())).thenReturn(newCategory);
        when(quoteRepository.findAllByCategory(newCategory)).thenReturn(List.of());
        List<Quote> result = quoteService.getQuotesByCategory("Life");
        assertTrue(result.isEmpty());
    }

    @Test
    void rateQuote_savesRating() {
        System.out.println("Тест: rateQuote_savesRating\n" +
                "- Підготовка: існує цитата і користувач, у них уже є оцінка.\n" +
                "- Дія: виклик методу з новим рейтингом 5.\n" +
                "- Очікування: рейтинг збережено, цитата повернута.");
        Quote quote = new Quote();
        quote.setId(1L);
        User user = new User();
        user.setId(2L);
        Rating rating = new Rating(user, quote, 0);

        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(ratingRepository.findByUserAndQuote(user, quote)).thenReturn(Optional.of(rating));

        Quote result = quoteService.rateQuote(1L, 2L, 5);

        assertEquals(quote, result);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void addToFavorites_addsIfNotExists() {
        System.out.println("Тест: addToFavorites_addsIfNotExists\n" +
                "- Підготовка: цитата і користувач існують, улюблене ще не додано.\n" +
                "- Дія: виклик методу додавання до улюбленого.\n" +
                "- Очікування: створюється зв'язок UserQuote.");
        Quote quote = new Quote();
        User user = new User();

        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userQuoteRepository.existsByUserAndQuote(user, quote)).thenReturn(false);

        quoteService.addToFavorites(1L, 2L);

        verify(userQuoteRepository).save(any(UserQuote.class));
    }

    @Test
    void deleteQuote_asAdmin_deletesQuote() throws AccessDeniedException {
        System.out.println("Тест: deleteQuote_asAdmin_deletesQuote\n" +
                "- Підготовка: користувач є адміністратором.\n" +
                "- Дія: виклик методу видалення цитати.\n" +
                "- Очікування: цитата успішно видаляється.");
        Quote quote = new Quote();
        User admin = new User();
        admin.setRole("ROLE_ADMIN");

        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        quoteService.deleteQuote(1L, 2L);

        verify(quoteRepository).delete(quote);
    }

    @Test
    void deleteQuote_asUser_throwsAccessDeniedException() {
        System.out.println("Тест: deleteQuote_asUser_throwsAccessDeniedException\n" +
                "- Підготовка: користувач не є адміністратором.\n" +
                "- Дія: виклик методу видалення цитати.\n" +
                "- Очікування: викидається AccessDeniedException.");
        Quote quote = new Quote();
        User user = new User();
        user.setRole("ROLE_USER");

        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThrows(AccessDeniedException.class, () -> quoteService.deleteQuote(1L, 2L));
    }
}
