package com.example.UserService;


//Ce DTO représente les informations d'un cours que user-service pourrait vouloir obtenir
public class CourseDTO {
 private int id_c;
 private String title;
 private String description;
 // Ajoutez d'autres champs si nécessaire (ex: price, categorie)

 // Constructeur vide, Getters et Setters
 public CourseDTO() {}

 public int getId_c() { return id_c; }
 public void setId_c(int id_c) { this.id_c = id_c; }
 public String getTitle() { return title; }
 public void setTitle(String title) { this.title = title; }
 public String getDescription() { return description; }
 public void setDescription(String description) { this.description = description; }
}