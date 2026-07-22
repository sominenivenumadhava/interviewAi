package com.interviai.backend.module.user.repository;

import com.interviai.backend.module.user.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserPreference entity operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, UUID> {

    /**
     * Find all preferences for a user.
     */
    List<UserPreference> findByUserIdOrderByKey(UUID userId);

    /**
     * Find specific preference by user and key.
     */
    Optional<UserPreference> findByUserIdAndKey(UUID userId, String key);

    /**
     * Find user preferences by type.
     */
    List<UserPreference> findByUserIdAndType(UUID userId, UserPreference.PreferenceType type);

    /**
     * Find system preferences for user.
     */
    List<UserPreference> findByUserIdAndIsSystemTrue(UUID userId);

    /**
     * Find user preferences (non-system).
     */
    List<UserPreference> findByUserIdAndIsSystemFalse(UUID userId);

    /**
     * Check if preference exists.
     */
    boolean existsByUserIdAndKey(UUID userId, String key);

    /**
     * Delete preference by user and key.
     */
    @Modifying
    @Query("DELETE FROM UserPreference up WHERE up.userId = :userId AND up.key = :key")
    void deleteByUserIdAndKey(@Param("userId") UUID userId, @Param("key") String key);

    /**
     * Delete all preferences for user.
     */
    @Modifying
    @Query("DELETE FROM UserPreference up WHERE up.userId = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);

    /**
     * Delete user preferences (non-system) for user.
     */
    @Modifying
    @Query("DELETE FROM UserPreference up WHERE up.userId = :userId AND up.isSystem = false")
    void deleteUserPreferences(@Param("userId") UUID userId);

    /**
     * Update preference value.
     */
    @Modifying
    @Query("UPDATE UserPreference up SET up.value = :value, up.type = :type " +
           "WHERE up.userId = :userId AND up.key = :key")
    void updatePreferenceValue(@Param("userId") UUID userId, @Param("key") String key,
                              @Param("value") String value, @Param("type") UserPreference.PreferenceType type);

    /**
     * Find preferences by key pattern.
     */
    @Query("SELECT up FROM UserPreference up WHERE up.userId = :userId " +
           "AND up.key LIKE CONCAT(:keyPrefix, '%') ORDER BY up.key")
    List<UserPreference> findByUserIdAndKeyStartingWith(@Param("userId") UUID userId, 
                                                        @Param("keyPrefix") String keyPrefix);

    /**
     * Count preferences for user.
     */
    long countByUserId(UUID userId);

    /**
     * Count system preferences for user.
     */
    long countByUserIdAndIsSystemTrue(UUID userId);
}