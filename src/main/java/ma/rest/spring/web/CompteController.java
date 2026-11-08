package ma.rest.spring.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import ma.rest.spring.web.dto.ComptesXml;
import ma.rest.spring.web.dto.Virement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * API REST des comptes bancaires, exposee par Spring MVC.
 * <p>
 * Chaque methode declare produire et consommer du JSON et du XML : Spring choisit
 * le format d'apres les en-tetes {@code Accept} et {@code Content-Type}. Les
 * recherches metier et le virement completent le CRUD demande par l'enonce.
 */
@RestController
@RequestMapping("/banque")
@Tag(name = "Comptes", description = "Gestion des comptes bancaires")
public class CompteController {

    private final CompteRepository compteRepository;

    /**
     * @param compteRepository depot injecte par Spring
     */
    public CompteController(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    /**
     * Liste tous les comptes au format JSON.
     *
     * @return les comptes
     */
    @GetMapping(value = "/comptes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Lister tous les comptes")
    public List<Compte> getAllComptes() {
        return compteRepository.findAll();
    }

    /**
     * Liste tous les comptes au format XML.
     * <p>
     * Une liste nue n'a pas d'element racine XML : {@link ComptesXml} fournit la
     * racine {@code <comptes>} pour un document bien forme.
     *
     * @return l'enveloppe XML des comptes
     */
    @GetMapping(value = "/comptes", produces = MediaType.APPLICATION_XML_VALUE)
    @Operation(summary = "Lister tous les comptes")
    public ComptesXml getAllComptesXml() {
        return new ComptesXml(compteRepository.findAll());
    }

    /**
     * Recherche un compte par identifiant.
     *
     * @param id identifiant recherche
     * @return 200 avec le compte, ou 404
     */
    @GetMapping(value = "/comptes/{id}", produces = {"application/json", "application/xml"})
    @Operation(summary = "Consulter un compte par son identifiant")
    public ResponseEntity<Compte> getCompteById(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> ResponseEntity.ok().body(compte))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Cree un compte.
     *
     * @param compte compte recu dans le corps de la requete
     * @return 201 avec le compte cree et l'en-tete Location
     */
    @PostMapping(value = "/comptes",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @Operation(summary = "Creer un compte")
    public ResponseEntity<Compte> createCompte(@RequestBody Compte compte) {
        compte.setId(null);
        Compte enregistre = compteRepository.save(compte);
        URI emplacement = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(enregistre.getId())
                .toUri();
        return ResponseEntity.created(emplacement).body(enregistre);
    }

    /**
     * Met a jour un compte existant.
     *
     * @param id            identifiant du compte
     * @param compteDetails nouvelles valeurs
     * @return 200 avec le compte mis a jour, ou 404
     */
    @PutMapping(value = "/comptes/{id}",
            consumes = {"application/json", "application/xml"},
            produces = {"application/json", "application/xml"})
    @Operation(summary = "Mettre a jour un compte")
    public ResponseEntity<Compte> updateCompte(@PathVariable Long id,
                                               @RequestBody Compte compteDetails) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compte.setSolde(compteDetails.getSolde());
                    compte.setDateCreation(compteDetails.getDateCreation());
                    compte.setType(compteDetails.getType());
                    return ResponseEntity.ok().body(compteRepository.save(compte));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Supprime un compte.
     *
     * @param id identifiant du compte
     * @return 204 si supprime, 404 sinon
     */
    @DeleteMapping("/comptes/{id}")
    @Operation(summary = "Supprimer un compte")
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        return compteRepository.findById(id)
                .map(compte -> {
                    compteRepository.delete(compte);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
