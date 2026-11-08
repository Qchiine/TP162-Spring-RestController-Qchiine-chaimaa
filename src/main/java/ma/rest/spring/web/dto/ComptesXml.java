package ma.rest.spring.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.rest.spring.entities.Compte;

import java.util.List;

/**
 * Enveloppe d'une collection de comptes pour le format XML.
 * <p>
 * Une {@code List} nue n'a pas d'element racine exploitable. Ce wrapper produit
 * {@code <comptes><compte>...</compte></comptes>} : racine explicite et nom
 * d'entree explicite, au lieu du {@code <List><item>} genere par defaut.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "comptes")
public class ComptesXml {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "compte")
    private List<Compte> comptes;
}
