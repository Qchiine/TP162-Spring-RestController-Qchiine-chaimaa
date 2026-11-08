package ma.rest.spring.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Demande de virement entre deux comptes.
 * <p>
 * Serialisable en JSON comme en XML : c'est le corps attendu par l'endpoint
 * {@code POST /banque/virements}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "virement")
public class Virement {

    private Long compteSource;

    private Long compteDestination;

    private double montant;
}
