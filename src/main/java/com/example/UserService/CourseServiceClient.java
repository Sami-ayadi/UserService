package com.example.UserService;


import com.example.UserService.CourseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping; // Exemple si besoin d'ajouter
import org.springframework.web.bind.annotation.RequestBody; // Exemple si besoin d'ajouter
import java.util.List;
import java.util.Set;


// Le nom "course-service" DOIT correspondre au spring.application.name du service cible
@FeignClient(name = "course-service")
public interface CourseServiceClient {

    // Exemple : Récupérer les détails d'un cours par son ID
    @GetMapping("/api/courses/{id}") // Doit correspondre à un endpoint DANS course-service
    CourseDTO getCourseById(@PathVariable("id") int courseId);

    // Exemple : Récupérer les cours créés par un utilisateur spécifique
    @GetMapping("/api/courses/owner/{userId}") // Doit correspondre à un endpoint DANS course-service
    List<CourseDTO> getCoursesByOwnerId(@PathVariable("userId") int userId);

    // Exemple : Récupérer les IDs des utilisateurs inscrits à un cours
    // (Cet endpoint devra exister dans course-service)
    @GetMapping("/api/courses/{courseId}/enrolled-users/ids")
    Set<Integer> getEnrolledUserIdsForCourse(@PathVariable("courseId") int courseId);


   // Hypothetical endpoint in course-service to manage enrollments
   // This assumes Course Service handles the enrollment record directly
   @PostMapping("/api/courses/{courseId}/enroll/{userId}")
   void enrollUserInCourse(@PathVariable("courseId") int courseId, @PathVariable("userId") int userId);

   @PostMapping("/api/courses/{courseId}/unenroll/{userId}")
   void unenrollUserFromCourse(@PathVariable("courseId") int courseId, @PathVariable("userId") int userId);


}