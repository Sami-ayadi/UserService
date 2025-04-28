package com.example.UserService;

import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore; // Important pour le mot de passe
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="suser") // Assurez-vous que cette table existe dans la BDD de user-service
public class SUser {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String nom ;
    private String prenom ;
    private String descriptionProfile;
    private String adresse;
    private String phone_number;
    private String date_birth; // Consider using java.time.LocalDate for better type safety
    private String personalWebsite;
    private String Facebbok; // Typo: should be Facebook
    private String Instagram;
    private String Linkedin;

    @Column(unique = true, nullable = false) // Email doit être unique
    private String email ;

    @Column(nullable = false)
    @JsonProperty("mdp")
    private String motDePasse;
    // Gardez les constructeurs, getters, setters et exceptions

    // Constructors
    public SUser() {}

    // You might want a constructor for creating a new user (excluding id)
    public SUser(String nom, String prenom, String descriptionProfile, String adresse, String phone_number,
                 String date_birth, String personalWebsite, String facebbok, String instagram, String linkedin,
                 String email, String mdp) {
        this.nom = nom;
        this.prenom = prenom;
        this.descriptionProfile = descriptionProfile;
        this.adresse = adresse;
        this.phone_number = phone_number;
        this.date_birth = date_birth;
        this.personalWebsite = personalWebsite;
        this.Facebbok = facebbok;
        this.Instagram = instagram;
        this.Linkedin = linkedin;
        this.email = email;
        this.motDePasse = mdp; // Ensure this 'mdp' is ALREADY HASHED when passed here
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getDescriptionProfile() {
        return descriptionProfile;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getDate_birth() {
        return date_birth;
    }

    public String getPersonalWebsite() {
        return personalWebsite;
    }

    public String getFacebbok() { // Typo preserved from your code
        return Facebbok;
    }

    public String getInstagram() {
        return Instagram;
    }

    public String getLinkedin() {
        return Linkedin;
    }

    public String getEmail() {
        return email;
    }

    // Getter for password - kept for internal use, but ignored by JSON serialization
    public String getMdp() {
        return motDePasse;
    }

    // --- Setters ---
    // Setter for ID is usually not needed as it's auto-generated
    // public void setId(int id) { this.id = id; }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setDescriptionProfile(String descriptionProfile) {
        this.descriptionProfile = descriptionProfile;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setDate_birth(String date_birth) {
        this.date_birth = date_birth;
    }

    public void setPersonalWebsite(String personalWebsite) {
        this.personalWebsite = personalWebsite;
    }

    public void setFacebbok(String facebbok) { // Typo preserved
        Facebbok = facebbok;
    }

    public void setInstagram(String instagram) {
        Instagram = instagram;
    }

    public void setLinkedin(String linkedin) {
        Linkedin = linkedin;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Setter for password - expects an ALREADY ENCODED/HASHED password
    public void setMdp(String encodedPassword) {
        this.motDePasse = encodedPassword;
    }

    // As noted in your original code, if you want a setter that encodes a raw password:
    // You would need a PasswordEncoder injected into the Service layer, not usually the Entity itself.
    // public void setRawPasswordAndEncode(String rawPassword, PasswordEncoder encoder) {
    //     this.mdp = encoder.encode(rawPassword);
    // }


    // ... exceptions UserNotFoundException, InvalidPasswordException, EmailAlreadyExistsException ...
    // These can remain inside or outside the SUser class. Keeping them outside is often cleaner.
    // For now, keeping them as per your provided code structure.

    @SuppressWarnings("serial")
    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }
    @SuppressWarnings("serial")
    public static class InvalidPasswordException extends RuntimeException {
        public InvalidPasswordException(String message) { super(message); }
    }
    @SuppressWarnings("serial")
    public static class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) { super(message); }
    }

    public boolean isEmpty() {
        // You need to define what "empty" means for a user object.
        // A common check might be if essential fields like email, nom, prenom are null/empty.
        // For a newly instantiated, non-persisted object, this might return true.
        // If it's meant to check if a fetched user object is null, you should just check for null directly.
        // Returning false unconditionally is probably not the intended logic.
        // Example: return this.email == null || this.email.isEmpty(); // Simple check
        // Or perhaps: return this.id == 0; // If 0 means not persisted
        // Or better: Remove this method unless you have a specific use case for it.
        // Keeping it as provided, but note it needs proper implementation based on requirements.
        return false;
    }
}