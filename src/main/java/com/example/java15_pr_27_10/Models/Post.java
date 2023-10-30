package com.example.java15_pr_27_10.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import  java.util.Date;

@Entity
@Getter
@AllArgsConstructor @NoArgsConstructor
@Table(name = "Posts")
@Setter
public class Post  {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String header;
    @Column(columnDefinition = "TEXT")
    private String context;
    private String image;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private Timestamp date = new Timestamp(System.currentTimeMillis());
    @PrePersist
    protected void onCreate() {
        date = new Timestamp(new Date().getTime());
    }


}
