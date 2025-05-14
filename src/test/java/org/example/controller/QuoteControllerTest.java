package org.example.controller;

import org.example.repository.CategoryRepository;
import org.example.repository.QuoteRepository;
import org.example.repository.RatingRepository;
import org.example.repository.UserRepository;
import org.example.service.QuoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final String USER_TOKEN = "Bearer mocked_user_token";
    private final String ADMIN_TOKEN = "Bearer mocked_admin_token";

    // User Story 1 – Випадкова цитата
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void getRandomQuote_success() throws Exception {
        mockMvc.perform(get("/quotes/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").exists());
    }

    //  User Story 2 – Додавання власної цитати
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void addQuote_success() throws Exception {
        Long categoryId = 1L;

        String json = """
            {
                "text": "Моя власна цитата",
                "author": "Bob",
                "categoryId": 1
            }
    """;

        mockMvc.perform(post("/quotes")
                        .header("Authorization", USER_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());
    }

    @Test
    void addQuote_unauthorized() throws Exception {
        String json = """
    {
        "text": "Без авторизації",
        "author": "Хтось",
        "categoryId": "4"
    }
    """;

        mockMvc.perform(post("/quotes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is3xxRedirection())  // Перевірка на редирект (302)
                .andExpect(header().string("Location", "http://localhost/oauth2/authorization/google"));
    }


    //  User Story 3 – Фільтрація цитат за категорією
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void getQuotesByCategory_success() throws Exception {
        mockMvc.perform(get("/quotes/category/Motivation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void getQuotesByCategory_notFound() throws Exception {
        mockMvc.perform(get("/quotes/category/НевідомаКатегорія"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    //  User Story 4 – Улюблені цитати
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void addToFavorites_success() throws Exception {
        mockMvc.perform(post("/quotes/1/favorite")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void addToFavorites_unauthorized() throws Exception {
        mockMvc.perform(post("/quotes/1/favorite"))
                .andExpect(status().is3xxRedirection());
    }


    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void addToFavorites_invalidId() throws Exception {
        mockMvc.perform(post("/quotes/999/favorite")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isNotFound());
    }


    // User Story 5 – Оцінка цитат
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void rateQuote_notFound() throws Exception {
        String json = """
            {
                "rating": 3
            }
            """;

        mockMvc.perform(post("/quotes/10/rate")
                        .param("rating", "3")
                        .header("Authorization", "Bearer mocked_user_token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

    }

    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    public void testQuoteNotRatedYet() throws Exception {
        mockMvc.perform(get("/quotes/6/rated")
                        .with(user("bob@example.com").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void getRatedQuoteById_notFound() throws Exception {

        mockMvc.perform(get("/quotes/{id}/rated", 9999L))
                .andExpect(status().isNotFound());
    }


    // User Story 6 – Видалення цитати
    @Test
    @WithMockUser(username = "bob@example.com", roles = {"ADMIN"})
    void deleteQuote_asAdmin_success() throws Exception {
        mockMvc.perform(delete("/quotes/6")
                        .header("Authorization", ADMIN_TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "bob@example.com", roles = {"USER"})
    void deleteQuote_asUser_forbidden() throws Exception {
        mockMvc.perform(delete("/quotes/1")
                        .header("Authorization", USER_TOKEN))
                .andExpect(status().isForbidden());
    }


}
