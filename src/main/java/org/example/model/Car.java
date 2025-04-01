package org.example.model;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String country;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate parseDate;

    private String url;
}