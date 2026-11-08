package ma.rest.spring;

import ma.rest.spring.entities.Compte;
import ma.rest.spring.entities.TypeCompte;
import ma.rest.spring.repositories.CompteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifie l'API REST de bout en bout : serveur reel, base H2 en memoire, et
 * appels HTTP sur les deux formats.
 * <p>
 * Le coeur du TP est la negociation de contenu : le meme endpoint doit repondre
 * en JSON ou en XML selon l'en-tete {@code Accept}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("API REST des comptes")
class CompteControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CompteRepository compteRepository;

    private static final String BASE = "/banque";

    @BeforeEach
    void preparer() {
        compteRepository.deleteAll();
        compteRepository.save(new Compte(null, 1500.0, aujourdHui(), TypeCompte.COURANT));
        compteRepository.save(new Compte(null, 3500.0, aujourdHui(), TypeCompte.EPARGNE));
        compteRepository.save(new Compte(null, 250.0, aujourdHui(), TypeCompte.EPARGNE));
    }

    private static Date aujourdHui() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private ResponseEntity<String> envoyer(HttpMethod methode, String chemin, String corps,
                                           MediaType accept, MediaType typeEnvoye) {
        HttpHeaders entetes = new HttpHeaders();
        entetes.setAccept(List.of(accept));
        if (typeEnvoye != null) {
            entetes.setContentType(typeEnvoye);
        }
        return restTemplate.exchange(BASE + chemin, methode, new HttpEntity<>(corps, entetes), String.class);
    }

    @Test
    @DisplayName("GET /comptes repond en JSON")
    void listeEnJson() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes", null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertNotNull(reponse.getHeaders().getContentType());
        assertTrue(reponse.getHeaders().getContentType().includes(MediaType.APPLICATION_JSON));
        assertTrue(reponse.getBody().startsWith("["), reponse.getBody());
        assertTrue(reponse.getBody().contains("solde"));
    }

    @Test
    @DisplayName("GET /comptes repond en XML")
    void listeEnXml() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes", null,
                MediaType.APPLICATION_XML, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertNotNull(reponse.getHeaders().getContentType());
        assertTrue(reponse.getHeaders().getContentType().includes(MediaType.APPLICATION_XML));
        assertTrue(reponse.getBody().contains("<compte>"), reponse.getBody());
        assertTrue(reponse.getBody().contains("<solde>"));
    }

    @Test
    @DisplayName("GET /comptes/{id} renvoie le compte en JSON")
    void compteParIdJson() {
        Long premier = compteRepository.findAll().get(0).getId();
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes/" + premier, null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertTrue(reponse.getBody().contains("\"id\":" + premier));
    }

    @Test
    @DisplayName("GET /comptes/{id} renvoie 404 pour un identifiant inconnu")
    void compteInconnu() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes/999999", null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.NOT_FOUND, reponse.getStatusCode());
    }

    @Test
    @DisplayName("GET /comptes/{id} renvoie le compte en XML")
    void compteParIdXml() {
        Long premier = compteRepository.findAll().get(0).getId();
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes/" + premier, null,
                MediaType.APPLICATION_XML, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertTrue(reponse.getBody().contains("<compte>"), reponse.getBody());
        assertTrue(reponse.getBody().contains("<id>" + premier + "</id>"));
    }

    @Test
    @DisplayName("POST /comptes cree un compte depuis un corps JSON")
    void creationJson() {
        String corps = "{\"solde\":1200.0,\"dateCreation\":\"2024-11-01\",\"type\":\"COURANT\"}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.POST, "/comptes", corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.CREATED, reponse.getStatusCode());
        assertNotNull(reponse.getHeaders().getLocation());
        assertTrue(reponse.getBody().contains("\"solde\":1200.0"));
        assertEquals(4, compteRepository.count());
    }

    @Test
    @DisplayName("POST /comptes cree un compte depuis un corps XML")
    void creationXml() {
        String corps = "<compte><solde>1750.0</solde><dateCreation>2024-11-01</dateCreation>"
                + "<type>EPARGNE</type></compte>";
        ResponseEntity<String> reponse = envoyer(HttpMethod.POST, "/comptes", corps,
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML);

        assertEquals(HttpStatus.CREATED, reponse.getStatusCode());
        assertTrue(reponse.getBody().contains("<solde>1750.0</solde>"), reponse.getBody());
        assertEquals(4, compteRepository.count());
    }

    @Test
    @DisplayName("POST /comptes ignore un identifiant fourni par le client")
    void creationIgnoreIdFourni() {
        String corps = "{\"id\":42,\"solde\":100.0,\"dateCreation\":\"2024-11-01\",\"type\":\"COURANT\"}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.POST, "/comptes", corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.CREATED, reponse.getStatusCode());
        Compte cree = compteRepository.findAll().stream()
                .filter(compte -> compte.getSolde() == 100.0)
                .findFirst()
                .orElseThrow();
        assertTrue(cree.getId() != 42L);
    }

    @Test
    @DisplayName("PUT /comptes/{id} met a jour un compte en JSON")
    void miseAJourJson() {
        Long premier = compteRepository.findAll().get(0).getId();
        String corps = "{\"solde\":999.99,\"dateCreation\":\"2024-11-01\",\"type\":\"EPARGNE\"}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.PUT, "/comptes/" + premier, corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertEquals(999.99, compteRepository.findById(premier).orElseThrow().getSolde());
    }

    @Test
    @DisplayName("PUT /comptes/{id} met a jour un compte en XML")
    void miseAJourXml() {
        Long premier = compteRepository.findAll().get(0).getId();
        String corps = "<compte><solde>2000.0</solde><dateCreation>2024-11-01</dateCreation>"
                + "<type>EPARGNE</type></compte>";
        ResponseEntity<String> reponse = envoyer(HttpMethod.PUT, "/comptes/" + premier, corps,
                MediaType.APPLICATION_XML, MediaType.APPLICATION_XML);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertEquals(2000.0, compteRepository.findById(premier).orElseThrow().getSolde());
    }

    @Test
    @DisplayName("PUT /comptes/{id} renvoie 404 pour un identifiant inconnu")
    void miseAJourInconnue() {
        String corps = "{\"solde\":10.0,\"dateCreation\":\"2024-11-01\",\"type\":\"COURANT\"}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.PUT, "/comptes/999999", corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.NOT_FOUND, reponse.getStatusCode());
    }

    @Test
    @DisplayName("DELETE /comptes/{id} supprime un compte")
    void suppression() {
        Long premier = compteRepository.findAll().get(0).getId();
        ResponseEntity<String> reponse = envoyer(HttpMethod.DELETE, "/comptes/" + premier, null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.NO_CONTENT, reponse.getStatusCode());
        assertTrue(compteRepository.findById(premier).isEmpty());
        assertEquals(2, compteRepository.count());
    }

    @Test
    @DisplayName("DELETE /comptes/{id} renvoie 404 pour un identifiant inconnu")
    void suppressionInconnue() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.DELETE, "/comptes/999999", null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.NOT_FOUND, reponse.getStatusCode());
    }

    @Test
    @DisplayName("GET /comptes/type/EPARGNE filtre par nature")
    void filtreParType() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes/type/EPARGNE", null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertTrue(reponse.getBody().contains("EPARGNE"));
        assertTrue(!reponse.getBody().contains("COURANT"));
    }

    @Test
    @DisplayName("GET /comptes/riches renvoie les comptes au-dessus du seuil")
    void filtreParSeuil() {
        ResponseEntity<String> reponse = envoyer(HttpMethod.GET, "/comptes/riches?seuil=1000", null,
                MediaType.APPLICATION_JSON, null);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertTrue(reponse.getBody().contains("3500.0"));
        assertTrue(!reponse.getBody().contains("250.0"));
    }

    @Test
    @DisplayName("POST /virements deplace le montant entre deux comptes")
    void virementAccepte() {
        List<Compte> comptes = compteRepository.findAll();
        Long source = comptes.get(0).getId();
        Long destination = comptes.get(1).getId();
        String corps = "{\"compteSource\":" + source + ",\"compteDestination\":" + destination
                + ",\"montant\":500.0}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.POST, "/virements", corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.OK, reponse.getStatusCode());
        assertEquals(1000.0, compteRepository.findById(source).orElseThrow().getSolde());
        assertEquals(4000.0, compteRepository.findById(destination).orElseThrow().getSolde());
    }

    @Test
    @DisplayName("POST /virements refuse un montant superieur au solde")
    void virementRefuse() {
        List<Compte> comptes = compteRepository.findAll();
        Long source = comptes.stream().filter(c -> c.getSolde() == 250.0).findFirst().orElseThrow().getId();
        Long destination = comptes.get(1).getId();
        String corps = "{\"compteSource\":" + source + ",\"compteDestination\":" + destination
                + ",\"montant\":100000.0}";
        ResponseEntity<String> reponse = envoyer(HttpMethod.POST, "/virements", corps,
                MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);

        assertEquals(HttpStatus.BAD_REQUEST, reponse.getStatusCode());
        assertEquals(250.0, compteRepository.findById(source).orElseThrow().getSolde());
    }
}
