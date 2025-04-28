package com.example.UserService;

import com.example.UserService.SUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


@Repository
public interface SUserRepository extends JpaRepository<SUser, Integer>{
 Optional<SUser> findByEmail(String email);
 // findByID existe déjà via JpaRepository<SUser, Integer> findById(Integer id);
 // Utilisez findById(id).orElse(null); ou .orElseThrow() dans le service

 // Remplacer findByID personnalisé si besoin spécifique, sinon utiliser findById
 @Query("SELECT u FROM SUser u WHERE u.id = :id")
 Optional<SUser> findUserById(@Param("id") int id); // Renommer pour éviter conflit


 List<SUser> findAll(); // Existe déjà via JpaRepository

 @Transactional
 @Modifying
 @Query("delete from SUser u where u.id = :id")
 void deleteUserById(@Param("id") int id); // Nom OK

 boolean existsByEmail(String email);
}
