package com.example.UserService;


//... imports ...
import com.example.UserService.SUser;
import com.example.UserService.SUser.*; // Pour les exceptions
import com.example.UserService.SUserRepository;
import com.example.UserService.CourseServiceClient; // Import Feign Client
import com.example.UserService.CourseDTO; // Import DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.*; // Pour List, Optional, Set etc.


@Service
public class SUserService {

 @Autowired
 private SUserRepository userRepository; // Renommé pour clarté

 @Autowired
 private PasswordEncoder passwordEncoder; // Inject PasswordEncoder

 @Autowired
 private JavaMailSender mailSender;

 @Autowired(required = false) // Mettre à false car course-service peut ne pas être démarré
 private CourseServiceClient courseServiceClient;

 public Optional<SUser> findByEmail(String email) {
     return userRepository.findByEmail(email);
 }

 public SUser findByID(int id) {
     // Utilise la méthode standard de JpaRepository et gère l'absence
     return userRepository.findById(id)
             .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
 }

 public List<SUser> findAllUsers() {
     return userRepository.findAll();
 }

 public SUser AddUser(SUser user) {
     if (userRepository.existsByEmail(user.getEmail())) {
         throw new EmailAlreadyExistsException("Email address already in use.");
     }
     // Hacher le mot de passe avant de sauvegarder
     user.setMdp(passwordEncoder.encode(user.getMdp()));
     return userRepository.save(user);
 }

 public SUser loginUser(String email, String rawPassword) throws UserNotFoundException, InvalidPasswordException {
     Optional<SUser> userOptional = userRepository.findByEmail(email);

     if (userOptional.isEmpty()) {
         throw new UserNotFoundException("Email not found.");
     }

     SUser user = userOptional.get();
     // Comparer le mot de passe brut fourni avec le mot de passe haché stocké
     if (!passwordEncoder.matches(rawPassword, user.getMdp())) {
         throw new InvalidPasswordException("Incorrect password.");
     }

     // IMPORTANT: Ne retournez JAMAIS l'objet SUser complet avec le mot de passe haché ici.
     // Retournez un DTO User ou seulement l'ID/email/roles si nécessaire.
     // Pour l'exemple, on retourne l'objet, mais attention en production.
     return user;
 }

 // --- Logique de cours déplacée ou modifiée ---

 // Cette méthode gère maintenant l'ajout de l'ID du cours à l'utilisateur localement
 // ET appelle potentiellement le service de cours pour enregistrer l'inscription là-bas aussi.
 public void enrollUserInCourse(int userId, int courseId) {
      SUser user = findByID(userId); // S'assure que l'utilisateur existe

      // Appelle le service de cours pour enregistrer l'inscription
      if (courseServiceClient != null) {
          try {
              courseServiceClient.enrollUserInCourse(courseId, userId);
              // Optionnel: Mettre à jour une liste locale d'ID de cours si nécessaire
              // user.getEnrolledCourseIds().add(courseId);
              // userRepository.save(user);
          } catch (Exception e) {
              // Gérer les erreurs de communication Feign (ex: course-service indisponible)
              System.err.println("Failed to enroll user via Course Service: " + e.getMessage());
              throw new RuntimeException("Enrollment failed due to communication error.", e);
          }
      } else {
           System.err.println("CourseServiceClient is not available.");
           throw new RuntimeException("Enrollment service is currently unavailable.");
      }
 }


 // Récupère les DTOs des cours via Feign en se basant sur une logique d'inscription
 // (Ici, on suppose que course-service peut nous donner les cours d'un user)
 public List<CourseDTO> getEnrolledCoursesForUser(int userId) {
     SUser user = findByID(userId); // Vérifie que l'user existe

     if (courseServiceClient != null) {
         try {
             // Supposons une méthode dans CourseServiceClient et CourseController
             // pour récupérer les cours auxquels un utilisateur est inscrit
             // (Ex: /api/courses/enrolled/{userId} dans course-service)
             // return courseServiceClient.getEnrolledCoursesByUserId(userId);
             // Pour l'instant, si l'inscription est gérée par CourseService:
             // Cela nécessiterait un endpoint dans CourseService du type:
             // @GetMapping("/api/courses/user/{userId}/enrolled") -> List<CourseDTO>
             // return courseServiceClient.getCoursesUserEnrolledIn(userId);

             // SI user-service maintenait la liste des IDs:
             // Set<Integer> courseIds = user.getEnrolledCourseIds();
             // List<CourseDTO> courses = new ArrayList<>();
             // for(int courseId : courseIds) {
             //     courses.add(courseServiceClient.getCourseById(courseId));
             // }
             // return courses;

              // Pour cet exemple, on retourne une liste vide car l'endpoint exact n'est pas défini
              System.out.println("Placeholder: Call CourseServiceClient to get enrolled courses for user " + userId);
              return new ArrayList<>();

         } catch (Exception e) {
             System.err.println("Failed to get enrolled courses via Course Service: " + e.getMessage());
             // Retourner liste vide ou lancer une exception
             return Collections.emptyList();
         }
     } else {
          System.err.println("CourseServiceClient is not available.");
          return Collections.emptyList();
     }
 }

 // Récupère les DTOs des cours créés par l'utilisateur via Feign
 public List<CourseDTO> getOwnedCoursesForUser(int userId) {
      SUser user = findByID(userId); // Vérifie que l'user existe
      if (courseServiceClient != null) {
          try {
             return courseServiceClient.getCoursesByOwnerId(userId);
          } catch (Exception e) {
              System.err.println("Failed to get owned courses via Course Service: " + e.getMessage());
             return Collections.emptyList();
          }
      } else {
           System.err.println("CourseServiceClient is not available.");
           return Collections.emptyList();
      }
 }


 public void deleteUserById(int id) {
      // Ajouter logique métier si nécessaire (ex: désinscrire des cours avant suppression)
      // foreach(CourseDTO course : getEnrolledCoursesForUser(id)) {
      //    courseServiceClient.unenrollUserFromCourse(course.getId_c(), id);
      // }
     userRepository.deleteUserById(id);
 }


 public SUser updateUser(int userId, SUser updatedUserData) {
     SUser user = findByID(userId); // Trouve l'utilisateur existant

     // Met à jour les champs fournis (vérifie la nullité)
     if (updatedUserData.getNom() != null) user.setNom(updatedUserData.getNom());
     if (updatedUserData.getPrenom() != null) user.setPrenom(updatedUserData.getPrenom());
     if (updatedUserData.getAdresse() != null) user.setAdresse(updatedUserData.getAdresse());
     if (updatedUserData.getDate_birth() != null) user.setDate_birth(updatedUserData.getDate_birth());
     if (updatedUserData.getPhone_number() != null) user.setPhone_number(updatedUserData.getPhone_number());
     if (updatedUserData.getPersonalWebsite() != null) user.setPersonalWebsite(updatedUserData.getPersonalWebsite());
     if (updatedUserData.getDescriptionProfile() != null) user.setDescriptionProfile(updatedUserData.getDescriptionProfile());
     if (updatedUserData.getFacebbok() != null) user.setFacebbok(updatedUserData.getFacebbok());
     if (updatedUserData.getLinkedin() != null) user.setLinkedin(updatedUserData.getLinkedin());
     if (updatedUserData.getInstagram() != null) user.setInstagram(updatedUserData.getInstagram());
     // Ne pas autoriser la modification de l'email ou du mdp via cette méthode ?
     // Si oui, ajouter des vérifications (unicité email, hachage mdp)

     return userRepository.save(user);
 }

 // Reset Password: Génère un mot de passe temporaire (ou un lien/token),
 // le hache, met à jour l'utilisateur et envoie l'email.
 public void resetPassword(String email) throws UserNotFoundException {
     Optional<SUser> optionalUser = userRepository.findByEmail(email);
     if (optionalUser.isEmpty()) {
         throw new UserNotFoundException("User not found with email: " + email);
     }
     SUser user = optionalUser.get();

     String tempPassword = generateRandomString(10); // Génère un mdp temporaire
     user.setMdp(passwordEncoder.encode(tempPassword)); // Hache et met à jour le mdp

     userRepository.save(user); // Sauvegarde l'utilisateur avec le nouveau mdp haché

     // Envoie l'email avec le mot de passe temporaire EN CLAIR
     sendPasswordResetEmail(user.getEmail(), user.getPrenom(), user.getNom(), tempPassword);
 }

 // Méthode d'envoi d'email (privée ou publique selon besoin)
 private void sendPasswordResetEmail(String recipientEmail, String prenom, String nom, String tempPassword) {
     SimpleMailMessage message = new SimpleMailMessage();
     message.setFrom("extraaccforff1@gmail.com"); // Configurer l'expéditeur
     message.setTo(recipientEmail);
     message.setSubject("Réinitialisation de votre mot de passe");
     message.setText("Cher " + prenom + " " + nom + ",\n\n" +
                     "Vous avez demandé une réinitialisation de mot de passe.\n\n" +
                     "Votre mot de passe temporaire est : " + tempPassword + "\n\n" +
                     "Veuillez vous connecter et changer votre mot de passe dès que possible.\n\n" +
                     "Cordialement,\nL'équipe App"); // Adapter le message
     try {
         mailSender.send(message);
     } catch (Exception e) {
         // Log l'erreur, mais ne bloque pas forcément le processus si l'email échoue
          System.err.println("Error sending password reset email to " + recipientEmail + ": " + e.getMessage());
     }
 }

 // Générateur de chaîne aléatoire (gardé tel quel)
 private static String generateRandomString(int length) {
      int leftLimit = 48; // '0'
      int rightLimit = 122; // 'z'
      Random random = new Random();
      StringBuilder builder = new StringBuilder(length);
      for (int i = 0; i < length; i++) {
          int randomInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
          // Filtre pour éviter les caractères non alphanumériques entre Z et a, etc.
          if ((randomInt > 57 && randomInt < 65) || (randomInt > 90 && randomInt < 97)) {
              i--; // Réessayer
              continue;
          }
          builder.append((char) randomInt);
      }
      return builder.toString();
  }

}