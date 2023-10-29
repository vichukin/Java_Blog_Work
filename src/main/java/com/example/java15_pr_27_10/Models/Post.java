package com.example.java15_pr_27_10.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.sql.Timestamp;

import static java.lang.System.currentTimeMillis;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor
@Table(name = "Posts")
public class Post  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String header;
    private String context;
    private Timestamp date = new Timestamp(currentTimeMillis());


}
