package app.api.bankapi.repository;

import app.api.bankapi.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByCardNumberCrypt(String cardNumberCrypt);
    @Query( value = "SELECT * from cards where owner_id = :owner_id", nativeQuery = true)
    List<Card> findByOwnerId(@Param("owner_id") Long ownerId);

    boolean existsByIdAndOwner_Id(Long id, Long ownerId);

}
