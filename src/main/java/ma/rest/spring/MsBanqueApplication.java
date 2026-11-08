package ma.rest.spring;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;

/**
 * Point d'entree du service bancaire.
 * <p>
 * Le {@code CommandLineRunner} de l'etape 6 insere trois comptes au demarrage
 * et les affiche dans la console pour verification.
 */
@SpringBootApplication
public class MsBanqueApplication {

    /**
     * @param args arguments de la ligne de commande
     */
    public static void main(String[] args) {
        SpringApplication.run(MsBanqueApplication.class, args);
    }

    /**
     * Insere les comptes de demonstration.
     *
     * @param compteRepository depot des comptes
     * @return le lanceur execute au demarrage
     */
    @Bean
    CommandLineRunner start(CompteRepository compteRepository) {
        return args -> {
            List<Compte> comptes = List.of(
                    new Compte(null, arrondir(Math.random() * 9000), new Date(), TypeCompte.EPARGNE),
                    new Compte(null, arrondir(Math.random() * 9000), new Date(), TypeCompte.COURANT),
                    new Compte(null, arrondir(Math.random() * 9000), new Date(), TypeCompte.EPARGNE));
            compteRepository.saveAll(comptes);
            compteRepository.findAll().forEach(compte -> System.out.println(compte.toString()));
        };
    }

    private static double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
