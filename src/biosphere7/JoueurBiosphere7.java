package biosphere7;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Joueur implémentant les actions possibles à partir d'un plateau, pour un
 * niveau donné.
 */
public class JoueurBiosphere7 implements IJoueurBiosphere7 {

    /**
     * Cette méthode renvoie, pour un plateau donné et un joueur donné, toutes
     * les actions possibles pour ce joueur.
     *
     * @param plateau le plateau considéré
     * @param couleurJoueur couleur du joueur
     * @param niveau le niveau de la partie à jouer
     * @return l'ensemble des actions possibles
     */
    @Override
    public String[] actionsPossibles(Case[][] plateau, char couleurJoueur, int niveau) {
        // afficher l'heure de lancement
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        System.out.println("actionsPossibles : lancement le " + format.format(new Date()));
        // se préparer à stocker les actions possibles
        ActionsPossibles actions = new ActionsPossibles();
        // calculer les vitalités sur le plateau initial
        Vitalites vitalites = vitalitesPlateau(plateau);
        // ajout des actions "planter pommier"
        for (int lig = 0; lig < Coordonnees.NB_LIGNES; lig++) {
            for (int col = 0; col < Coordonnees.NB_COLONNES; col++) {
                Coordonnees coord = new Coordonnees(lig, col);
                if (plateau[lig][col].espece == Case.CAR_VIDE) {
                    ajoutActionPlanter(coord, actions, vitalites, couleurJoueur, plateau);
                }
                if (plateau[lig][col].plantePresente() == true) {
                    ajoutActionCouper(coord, actions, vitalites, plateau[lig][col].couleur, plateau);
                }
            }
        }
        System.out.println("actionsPossibles : fin");
        return actions.nettoyer();
    }

    /**
     * Somme des vitalités des plantes de chaque joueur sur le plateau.
     *
     * @param plateau le plateau
     * @return la somme des vitalités des plantes de chaque joueur
     */
    static Vitalites vitalitesPlateau(Case[][] plateau) {
        // TODO il y en aura besoin à un moment !
        Vitalites vita = new Vitalites(0, 0);
        for (int lig = 0; lig < Coordonnees.NB_LIGNES; lig++) {
            for (int col = 0; col < Coordonnees.NB_COLONNES; col++) {
                if (plateau[lig][col].plantePresente() == true) {
                    if (plateau[lig][col].couleur == Case.CAR_BLEU) {
                        //compter la vitalité du pommier
                        vita.vitalitesBleu += plateau[lig][col].vitalite;
                    } else if (plateau[lig][col].couleur == Case.CAR_ROUGE) {
                        vita.vitalitesRouge += plateau[lig][col].vitalite;
                    }
                }
            }
        }

        //return new Vitalites(vitaliteRouge, vitaliteBleu);
        return vita;
    }

    /**
     * Ajout d'une action de plantation de pommier dans l'ensemble des actions
     * possibles.
     *
     * @param coord coordonnées de la case où planter le pommier
     * @param actions l'ensemble des actions possibles (en construction)
     * @param vitalites la somme des vitalités sur le plateau avant de jouer
     * l'action
     * @param couleur la couleur du pommier à ajouter
     */
    void ajoutActionPlanter(Coordonnees coord, ActionsPossibles actions, Vitalites vitalites, char couleur, Case[][] plateau) {
        String action = "P" + coord.carLigne() + coord.carColonne() + "," + (vitalites.vitalitesRouge) + ","
                + (vitalites.vitalitesBleu);
        Case[][] plateauCopie = copieDePlateau(plateau);
        plateauCopie[coord.ligne][coord.colonne].vitalite = 1;
        plateauCopie[coord.ligne][coord.colonne].espece = Case.CAR_POMMIER;
        int[] VoisinePresume = new int[2];
        if (couleur == 'B') {
            plateauCopie[coord.ligne][coord.colonne].couleur = Case.CAR_BLEU;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    VoisinePresume[0] = i;
                    VoisinePresume[1] = j;
                    if (checkVoisineCase(coord, plateauCopie, VoisinePresume) == true) {
                        if (checkMemeCouleur(coord, plateauCopie, VoisinePresume) == true) {
                            plateauCopie[coord.ligne][coord.colonne].vitalite += 1;
                        }
                    }
                }
            }
            Vitalites nouvellesVitalites = vitalitesPlateau(plateauCopie);
            action = "P" + coord.carLigne() + coord.carColonne() + "," + (nouvellesVitalites.vitalitesRouge) + ","
                    + (nouvellesVitalites.vitalitesBleu);
        } else if (couleur == 'R') {
            plateauCopie[coord.ligne][coord.colonne].couleur = Case.CAR_ROUGE;
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    VoisinePresume[0] = i;
                    VoisinePresume[1] = j;
                    if (checkVoisineCase(coord, plateauCopie, VoisinePresume) == true) {
                        if (checkMemeCouleur(coord, plateauCopie, VoisinePresume) == true) {
                            plateauCopie[coord.ligne][coord.colonne].vitalite += 1;
                        }
                    }
                }
            }
            Vitalites nouvellesVitalites = vitalitesPlateau(plateauCopie);
            action = "P" + coord.carLigne() + coord.carColonne() + "," + (nouvellesVitalites.vitalitesRouge) + ","
                    + (nouvellesVitalites.vitalitesBleu);
        }
        actions.ajouterAction(action);
    }

    /**
     * fonction permettant de savoir si une case est voisine à une autre
     *
     * @param coord les coordonnees de la case d origine
     * @param plateau le plateau sur le quel on cherche les cases
     * @param voisinePresume tableau avec des valeurs alant de -1 à 1 pour le x
     * et y
     * @return retourne true si 2 cases sont voisines, sinon, retourne false
     */
    boolean checkVoisineCase(Coordonnees coord, Case[][] plateau, int[] voisinePresume) {
        int voisineColonne = voisinePresume[1];
        int voisineLigne = voisinePresume[0];
        Coordonnees coordTemp = new Coordonnees(coord.ligne + voisineLigne, coord.colonne + voisineColonne);

        if (coordTemp.estDansPlateau() == true) {
            if (plateau[coordTemp.ligne][coordTemp.colonne].plantePresente() == true && (coordTemp.ligne != coord.ligne || coordTemp.colonne != coord.colonne) && (coordTemp.memeColonne(coord) == true || coord.memeLigne(coordTemp))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * meme principe que checkVoisineCase mais pour la couleur (vérifie
     * seulement si 2 cases ont la meme couleur, pas si elles sont voisines)
     *
     * @param coord les coordonnees de la case d origine
     * @param plateau le plateau sur le quel on cherche les cases
     * @param voisinePresume tableau avec des valeurs alant de -1 à 1 pour le x
     * et y
     * @return retourne true si les 2 cases sont de la meme couleur, sinon,
     * retourne false
     */
    boolean checkMemeCouleur(Coordonnees coord, Case[][] plateau, int[] voisinePresume) {
        int voisineColonne = voisinePresume[1];
        int voisineLigne = voisinePresume[0];
        Coordonnees coordTemp = new Coordonnees(coord.ligne + voisineLigne, coord.colonne + voisineColonne);
        return plateau[coordTemp.ligne][coordTemp.colonne].couleur == plateau[coord.ligne][coord.colonne].couleur;
    }

    /**
     * fonction permettant de créer un copie d'un plateau passé en paramètre
     * (utile pour ne pas modifier sur le plateau directement)
     *
     * @param plateauACopier le plateau a copier
     * @return une copie du plateau
     */
    Case[][] copieDePlateau(Case[][] plateauACopier) {
        Case[][] plateauCopie = new Case[Coordonnees.NB_LIGNES][Coordonnees.NB_COLONNES];
        for (int lig = 0; lig < Coordonnees.NB_LIGNES; lig++) {
            for (int col = 0; col < Coordonnees.NB_COLONNES; col++) {
                plateauCopie[lig][col] = new Case(plateauACopier[lig][col].espece, plateauACopier[lig][col].couleur, plateauACopier[lig][col].vitalite, plateauACopier[lig][col].nature);
            }
        }
        return plateauCopie;
    }

    /**
     * Ajout d'une action de couper une plante dans l'ensemble des actions
     * possibles.
     *
     * @param coord coordonnées de la case où couper la plante
     * @param actions l'ensemble des actions possibles (en construction)
     * @param vitalites la somme des vitalités sur le plateau avant de jouer
     * l'action
     * @param couleurPlante la couleur de la plante à couper
     * @param plateau le plateau sur le quel on ajoute l'action
     */
    void ajoutActionCouper(Coordonnees coord, ActionsPossibles actions, Vitalites vitalites, char couleurPlante, Case[][] plateau) {
        String action2 = "C" + coord.carLigne() + coord.carColonne() + "," + (vitalites.vitalitesRouge) + ","
                + (vitalites.vitalitesBleu);
        Case[][] plateauCopie = copieDePlateau(plateau);
        int[] VoisinePresume = new int[2];
        if (couleurPlante == 'B') {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    VoisinePresume[0] = i; //Ligne
                    VoisinePresume[1] = j; //Colonne
                    if (checkVoisineCase(coord, plateauCopie, VoisinePresume) == true) {
                        if (plateauCopie[coord.ligne + i][coord.colonne + j].vitalite < 9) {
                            plateauCopie[coord.ligne + i][coord.colonne + j].vitalite += 1;
                        }
                    }
                }
            }
            plateauCopie[coord.ligne][coord.colonne].vitalite = 0;
            plateauCopie[coord.ligne][coord.colonne].espece = Case.CAR_VIDE;
            Vitalites nouvellesVitalites = vitalitesPlateau(plateauCopie);
            action2 = "C" + coord.carLigne() + coord.carColonne() + "," + (nouvellesVitalites.vitalitesRouge) + ","
                    + (nouvellesVitalites.vitalitesBleu);
        } else if (couleurPlante == 'R') {
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    VoisinePresume[0] = i; //Ligne
                    VoisinePresume[1] = j; //Colonne
                    if (checkVoisineCase(coord, plateauCopie, VoisinePresume) == true) {
                        if (plateauCopie[coord.ligne + i][coord.colonne + j].vitalite < 9) {
                            plateauCopie[coord.ligne + i][coord.colonne + j].vitalite += 1;
                        }
                    }
                }
            }
            plateauCopie[coord.ligne][coord.colonne].vitalite = 0;
            plateauCopie[coord.ligne][coord.colonne].espece = Case.CAR_VIDE;
            Vitalites nouvellesVitalites = vitalitesPlateau(plateauCopie);
            action2 = "C" + coord.carLigne() + coord.carColonne() + "," + (nouvellesVitalites.vitalitesRouge) + ","
                    + (nouvellesVitalites.vitalitesBleu);
        }
        actions.ajouterAction(action2);
    }
}
