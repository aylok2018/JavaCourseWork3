package org.example.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_quotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserQuote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quote_id", nullable = false)
    private Quote quote;

    public UserQuote(User user, Quote quote) {
        this.user = user;
        this.quote = quote;
    }

}
