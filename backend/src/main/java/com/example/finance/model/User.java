package com.example.finance.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity @Table(name="users")

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor 
@Builder
@ToString(exclude = "hashedPassword")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Email
    @Column(nullable=false, unique=true, length=150)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String hashedPassword;

    @Column(nullable=false, length=30)
    private String role;
}
