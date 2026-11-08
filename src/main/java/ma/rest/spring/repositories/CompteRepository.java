package ma.rest.spring.repositories;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Depot JPA des comptes.
 * <p>
 * {@code JpaRepository} fournit findAll, save, findById et deleteById : seules
 * les recherches metier sont declarees ici.
 */
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    /**
     * @param type nature recherchee
     * @return les comptes de cette nature
     */
    List<Compte> findByType(TypeCompte type);

    /**
     * @param seuil montant minimal exclu
     * @return les comptes dont le solde depasse le seuil, du plus riche au moins riche
     */
    @Query("from Compte c where c.solde > :seuil order by c.solde desc")
    List<Compte> findPlusRichesQue(double seuil);
}
