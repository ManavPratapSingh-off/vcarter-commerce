package com.vcarter.ecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter @Setter
@Table(name = "products")
public class Product {
    @Id
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Price cannot be null")
    private Double price;

    @Column(nullable = false)
    @NotNull(message = "Availability status cannot be null")
    private AvailabilityStatus availabilityStatus;

    @Column(nullable = false)
    @NotBlank(message = "Category cannot be blank")
    private String category;

    @ManyToMany
    @JoinTable(
        name = "order_products",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private List<Order> orders;
}
