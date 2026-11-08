package ma.rest.spring.web.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Resultat d'un virement accepte : les deux soldes apres operation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JacksonXmlRootElement(localName = "resultatVirement")
public class ResultatVirement {

    private Long compteSource;

    private double nouveauSoldeSource;

    private Long compteDestination;

    private double nouveauSoldeDestination;

    private double montant;
}
