package ma.rest.spring.entities;

/**
 * Nature du compte bancaire.
 * <p>
 * L'entite est annotee {@code @Enumerated(EnumType.STRING)} : la valeur stockee
 * en base est le nom de la constante, ce qui rend les donnees lisibles.
 */
public enum TypeCompte {

    COURANT,

    EPARGNE
}
