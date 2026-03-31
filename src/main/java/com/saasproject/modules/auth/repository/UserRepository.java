package com.saasproject.modules.auth.repository;

import com.saasproject.modules.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * User repository.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByEmailAndTenantIdAndDeletedFalse(String email, String tenantId);

    boolean existsByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndTenantIdAndDeletedFalse(String email, String tenantId);

    boolean existsByTenantIdAndDeletedFalse(String tenantId);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.tenantId = :tenantId AND u.deleted = false")
    Optional<User> findByIdAndTenant(@Param("id") UUID id, @Param("tenantId") String tenantId);

    @Query(value = "SELECT * FROM users u WHERE u.email = :email AND u.deleted = false ORDER BY u.created_at DESC LIMIT 1", nativeQuery = true)
    Optional<User> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.tenantId = :tenantId AND u.deleted = false ORDER BY u.email")
    org.springframework.data.domain.Page<User> findByTenant(@Param("tenantId") String tenantId,
            org.springframework.data.domain.Pageable pageable);
}
