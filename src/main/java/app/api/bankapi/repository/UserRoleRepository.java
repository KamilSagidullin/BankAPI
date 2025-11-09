package app.api.bankapi.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
@Repository
public class UserRoleRepository  {
    @PersistenceContext
    private EntityManager entityManager;
    @Transactional
    public void addRoleToUser(Long userId, String role) {
        entityManager.createNativeQuery(
                        "INSERT INTO user_roles(user_id, role) VALUES (:userId, :role) " +
                                "ON CONFLICT (user_id, role) DO NOTHING"
                )
                .setParameter("userId", userId)
                .setParameter("role", role)
                .executeUpdate();
    }

}
