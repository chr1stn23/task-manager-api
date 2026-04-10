package com.christian.taskmanager.repository;

import com.christian.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickName);

    @Modifying
    @Query("UPDATE User u SET u.password = :encodedPassword WHERE u.id = :userId")
    int updatePassword(@Param("userId") Long userId, @Param("encodedPassword") String encodedPassword);

    @Modifying
    @Query("UPDATE User u SET u.profileImageId = :imageId, u.profileImageUrl = :imageUrl WHERE u.id = :userId")
    int updateProfileImage(
            @Param("userId") Long userId,
            @Param("imageId") String imageId,
            @Param("imageUrl") String imageUrl);

    long countByEnabledTrue();
}
