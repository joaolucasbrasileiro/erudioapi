package br.com.erudio.repository;

import br.com.erudio.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    @Query("SELECT t FROM PasswordResetToken t WHERE t.email = :email AND t.token = :token AND t.used = false ORDER BY t.expiryDate DESC")
    Optional<PasswordResetToken> findValidToken(@Param("email") String email, @Param("token") String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken t WHERE t.email = :email")
    void deleteAllByEmail(@Param("email") String email);
}