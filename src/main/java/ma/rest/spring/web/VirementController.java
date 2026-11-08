package ma.rest.spring.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import ma.rest.spring.web.dto.ResultatVirement;
import ma.rest.spring.web.dto.Virement;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Apport personnel : operations transverses sur le portefeuille.
 * <p>
 * L'enonce s'arrete au CRUD d'un compte. Une agence a pourtant besoin de filtrer
 * son portefeuille et de deplacer de l'argent entre deux comptes : ces endpoints
 * repondent a ce besoin sans modifier le controleur demande par le TP.
 */
@RestController
@RequestMapping({"/banque", "/api"})
@Tag(name = "Portefeuille", description = "Filtres et virements entre comptes")
public class VirementController {

    private final CompteRepository compteRepository;

    /**
     * @param compteRepository depot des comptes
     */
    public VirementController(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    /**
     * Filtre les comptes par nature.
     *
     * @param type COURANT ou EPARGNE
     * @return les comptes de cette nature, ou 400 si la valeur est inconnue
     */
    @GetMapping(value = "/comptes/type/{type}", produces = {"application/json", "application/xml"})
    @Operation(summary = "Lister les comptes d'une nature donnee")
    public ResponseEntity<List<Compte>> parType(@PathVariable String type) {
        try {
            return ResponseEntity.ok(compteRepository.findByType(TypeCompte.valueOf(type.toUpperCase())));
        } catch (IllegalArgumentException inconnu) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Filtre les comptes dont le solde depasse un seuil.
     *
     * @param seuil montant minimal exclu
     * @return les comptes concernes, du plus riche au moins riche
     */
    @GetMapping(value = "/comptes/riches", produces = {"application/json", "application/xml"})
    @Operation(summary = "Lister les comptes dont le solde depasse un seuil")
    public List<Compte> plusRichesQue(@RequestParam double seuil) {
        return compteRepository.findPlusRichesQue(seuil);
    }

    /**
     * Deplace un montant d'un compte vers un autre.
     *
     * @param virement comptes source et destination, montant
     * @return les soldes apres operation, ou 404 si un compte manque
     */
    @PostMapping(value = "/virements",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @Operation(summary = "Virer un montant entre deux comptes")
    @Transactional
    public ResponseEntity<ResultatVirement> virer(@RequestBody Virement virement) {
        Compte source = compteRepository.findById(virement.getCompteSource()).orElse(null);
        Compte destination = compteRepository.findById(virement.getCompteDestination()).orElse(null);
        if (source == null || destination == null) {
            return ResponseEntity.notFound().build();
        }
        if (source.getId().equals(destination.getId()) || virement.getMontant() <= 0
                || source.getSolde() < virement.getMontant()) {
            return ResponseEntity.badRequest().build();
        }
        source.setSolde(arrondir(source.getSolde() - virement.getMontant()));
        destination.setSolde(arrondir(destination.getSolde() + virement.getMontant()));
        compteRepository.save(source);
        compteRepository.save(destination);

        return ResponseEntity.ok(new ResultatVirement(source.getId(), source.getSolde(),
                destination.getId(), destination.getSolde(), virement.getMontant()));
    }

    private static double arrondir(double valeur) {
        return Math.round(valeur * 100.0) / 100.0;
    }
}
