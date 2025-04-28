package com.example.UserService;


//... imports ...
import com.example.UserService.SUser;
import com.example.UserService.SUser.*; // Exceptions
import com.example.UserService.SUserService;
import com.example.UserService.CourseDTO; // Import DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*; // Import annotations

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@RestController
@RequestMapping("/api/users") // Préfixe pour les routes de ce service
@CrossOrigin("*") // Configurez CORS plus précisément en production
public class SUserController {

 @Autowired
 private SUserService userService; // Renommé pour clarté


 // GET /api/users/email/{mail}
 @GetMapping("/email/{mail}")
 public ResponseEntity<SUser> findByEmail(@PathVariable String mail) {
      // Retourne l'utilisateur SANS mot de passe (JsonIgnore sur le champ mdp)
     return userService.findByEmail(mail)
             .map(ResponseEntity::ok) // Si trouvé, retourne 200 OK avec l'user
             .orElse(ResponseEntity.notFound().build()); // Sinon retourne 404 Not Found
 }

 // GET /api/users/{id}
 @GetMapping("/{id}")
 public ResponseEntity<SUser> findById(@PathVariable int id) {
     try {
          // Retourne l'utilisateur SANS mot de passe
         SUser user = userService.findByID(id);
         return ResponseEntity.ok(user);
     } catch (UserNotFoundException e) {
         return ResponseEntity.notFound().build();
     }
 }

 // GET /api/users
 @GetMapping
 public List<SUser> findAllUsers() {
      // Retourne la liste des utilisateurs SANS mot de passe
     return userService.findAllUsers();
 }

 // POST /api/users/add
 @PostMapping("/add")
 public ResponseEntity<?> addUser(@RequestBody SUser user) {
     try {
         // Le mot de passe est haché dans le service
         SUser newUser = userService.AddUser(user);
         // Retourne l'utilisateur créé (SANS mdp) avec statut 201 Created
         return new ResponseEntity<>(newUser, HttpStatus.CREATED);
     } catch (EmailAlreadyExistsException e) {
         return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
     } catch (Exception e) {
          // Autres erreurs possibles
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating user.");
     }
 }

 // POST /api/users/login
 @PostMapping("/login")
 public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
     String email = credentials.get("email");
     String rawPassword = credentials.get("password"); // Utiliser "password" est plus standard

     if (email == null || rawPassword == null) {
          return ResponseEntity.badRequest().body("Email and password are required.");
     }

     try {
         SUser user = userService.loginUser(email, rawPassword);
          // Ne pas retourner l'utilisateur complet ici en production.
          // Retourner un DTO, un token JWT, ou juste un statut OK.
          // Exemple simple : retourner l'ID et l'email
          Map<String, Object> loginResponse = Map.of(
                  "message", "Login successful",
                  "userId", user.getId(),
                  "email", user.getEmail()
          );
         return ResponseEntity.ok(loginResponse);
     } catch (UserNotFoundException e) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
     } catch (InvalidPasswordException e) {
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
     } catch (Exception e) {
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login.");
     }
 }

 // POST /api/users/{userId}/enroll/{courseId} -> Nouvelle route pour l'inscription
 @PostMapping("/{userId}/enroll/{courseId}")
  public ResponseEntity<?> enrollUserInCourse(@PathVariable int userId, @PathVariable int courseId) {
      try {
          userService.enrollUserInCourse(userId, courseId);
          return ResponseEntity.ok("User enrolled successfully in course " + courseId);
      } catch (UserNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } catch (RuntimeException e) { // Gère les erreurs Feign ou autres
          return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
      } catch (Exception e){
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to enroll user.");
      }
  }


 // GET /api/users/{userId}/enrolled-courses -> Récupère les cours auxquels l'utilisateur est inscrit
 @GetMapping("/{userId}/enrolled-courses")
 public ResponseEntity<List<CourseDTO>> getEnrolledCourses(@PathVariable int userId) {
     try {
          // Appelle le service qui utilise Feign pour obtenir les DTOs des cours
         List<CourseDTO> courses = userService.getEnrolledCoursesForUser(userId);
         return ResponseEntity.ok(courses);
     } catch (UserNotFoundException e) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Ou une liste vide avec OK ?
     } catch (Exception e) {
          System.err.println("Error fetching enrolled courses for user " + userId + ": " + e.getMessage());
          // Peut-être SERVICE_UNAVAILABLE si Feign échoue
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
     }
 }

  // GET /api/users/{userId}/owned-courses -> Récupère les cours créés par l'utilisateur
  @GetMapping("/{userId}/owned-courses")
  public ResponseEntity<List<CourseDTO>> getOwnedCourses(@PathVariable int userId) {
      try {
          // Appelle le service qui utilise Feign pour obtenir les DTOs des cours
          List<CourseDTO> courses = userService.getOwnedCoursesForUser(userId);
          return ResponseEntity.ok(courses);
      } catch (UserNotFoundException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
      } catch (Exception e) {
          System.err.println("Error fetching owned courses for user " + userId + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
      }
  }


 // DELETE /api/users/{id}
 @DeleteMapping("/{id}")
 public ResponseEntity<Void> deleteUserById(@PathVariable int id) {
      try {
          userService.deleteUserById(id);
          return ResponseEntity.noContent().build(); // 204 No Content si succès
      } catch (Exception e) { // Attraper exception si l'ID n'existe pas pourrait être mieux
          System.err.println("Error deleting user " + id + ": " + e.getMessage());
          // On pourrait retourner 404 si l'utilisateur n'est pas trouvé
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
 }

 // PUT /api/users/edit/{userId}
 @PutMapping("/edit/{userId}")
 public ResponseEntity<?> updateUser(@PathVariable int userId, @RequestBody SUser updatedUserData) {
     try {
          // Le service gère la logique de mise à jour et la sauvegarde
         SUser updatedUser = userService.updateUser(userId, updatedUserData);
          // Retourne l'utilisateur mis à jour (SANS mdp)
         return ResponseEntity.ok(updatedUser);
     } catch (UserNotFoundException e) {
         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
     } catch (Exception e) {
          System.err.println("Error updating user " + userId + ": " + e.getMessage());
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating user profile.");
     }
 }

 // POST /api/users/reset-password
 @PostMapping("/reset-password") // Utiliser le corps de la requête est souvent mieux
 public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> payload) {
      String email = payload.get("email");
      if (email == null || email.trim().isEmpty()) {
          return ResponseEntity.badRequest().body("Email is required.");
      }
      try {
          userService.resetPassword(email); // Le service gère la logique et l'envoi d'email
          return ResponseEntity.ok("Password reset instructions sent to your email.");
      } catch (UserNotFoundException e) {
          // Ne pas révéler si l'email existe ou non pour des raisons de sécurité
          // Toujours retourner un message générique
          return ResponseEntity.ok("If an account exists for this email, password reset instructions have been sent.");
          // Ou si vous préférez être explicite (moins sécurisé) :
          // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
      } catch (Exception e) {
          System.err.println("Password reset error for email " + email + ": " + e.getMessage());
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Password reset failed. Please try again later.");
      }
 }
}
