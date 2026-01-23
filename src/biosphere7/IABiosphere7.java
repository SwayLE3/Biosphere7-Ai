package biosphere7;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

/**
 * Votre IA pour le jeu Biosphere7.
 */
public class IABiosphere7 {

    /**
     * Hôte du grand ordonnateur.
     */
    String hote = null;

    /**
     * Port du grand ordonnateur.
     */
    int port = -1;

    /**
     * Couleur de votre joueur (IA) : 'R'ouge ou 'B'leu.
     */
    final char couleur;

    /**
     * Interface pour le protocole du grand ordonnateur.
     */
    TcpGrandOrdonnateur grandOrdo = null;

    /**
     * Nombre maximal de tours de jeu.
     */
    static final int NB_TOURS_JEU_MAX = 40;

    /**
     * Constructeur.
     *
     * @param hote Hôte.
     * @param port Port.
     * @param uneCouleur Couleur de ce joueur
     */
    public IABiosphere7(String hote, int port, char uneCouleur) {
        this.hote = hote;
        this.port = port;
        this.grandOrdo = new TcpGrandOrdonnateur();
        this.couleur = uneCouleur;
    }

    /**
     * Connexion au Grand Ordonnateur.
     *
     * @throws IOException exception sur les entrées/sorties
     */
    void connexion() throws IOException {
        System.out.print(
                "Connexion au Grand Ordonnateur : " + hote + " " + port + "...");
        System.out.flush();
        grandOrdo.connexion(hote, port);
        System.out.println(" ok.");
        System.out.flush();
    }

    /**
     * Boucle de jeu : envoi des actions que vous souhaitez jouer, et réception
     * des actions de l'adversaire.
     *
     * @throws IOException exception sur les entrées/sorties
     */
    void toursDeJeu() throws IOException {
        // paramètres
        System.out.println("Je suis le joueur " + couleur + ".");
        // le plateau initial
        System.out.println("Réception du plateau initial...");
        Case[][] plateau = grandOrdo.recevoirPlateauInitial();
        System.out.println("Plateau reçu.");
        // compteur de tours de jeu (entre 1 et 40)
        int nbToursJeu = 1;
        // la couleur du joueur courant (change à chaque tour de jeu)
        char couleurTourDeJeu = Case.CAR_ROUGE;
        // booléen pour détecter la fin du jeu
        boolean fin = false;
        while (!fin) {
            boolean disqualification = false;
            if (couleurTourDeJeu == couleur) {
                // à nous de jouer !
                jouer(plateau, nbToursJeu);
            } else {
                // à l'adversaire de jouer
                disqualification = adversaireJoue(plateau, couleurTourDeJeu);
            }
            if (nbToursJeu == NB_TOURS_JEU_MAX || disqualification) {
                // fini
                fin = true;
            } else {
                // au suivant
                nbToursJeu++;
                couleurTourDeJeu = suivant(couleurTourDeJeu);
            }
        }
    }

    /**
     * Fonction exécutée lorsque c'est à notre tour de jouer. Cette fonction
     * envoie donc l'action choisie au serveur.
     *
     * @param plateau le plateau de jeu
     * @param nbToursJeu numéro du tour de jeu
     * @throws IOException exception sur les entrées / sorties
     */
    void jouer(Case[][] plateau, int nbToursJeu) throws IOException {
        String actionJouee = actionChoisie(plateau, nbToursJeu);
        if (actionJouee != null) {
            // jouer l'action
            System.out.println("On joue : " + actionJouee);
            grandOrdo.envoyerAction(actionJouee);
            mettreAJour(plateau, actionJouee, couleur);
        } else {
            // Problème : le serveur vous demande une action alors que vous n'en
            // trouvez plus...
            System.out.println("Aucun action trouvée : abandon...");
            grandOrdo.envoyerAction("ABANDON");
        }
    }

    /**
     * L'action choisie par notre IA.
     *
     * @param plateau le plateau de jeu
     * @param nbToursJeu numéro du tour de jeu
     * @return l'action choisie sous forme de chaîne
     */
    public String actionChoisie(Case[][] plateau, int nbToursJeu) {
        //
        // TODO : ici, on choisit aléatoirement n'importe quelle action possible
        // retournée par votre programme. À vous de faire un meilleur choix...
        //

        JoueurBiosphere7 joueur = new JoueurBiosphere7();
        String[] actionsPossiblesDepuisPlateau = ActionsPossibles.nettoyerTableau(joueur.actionsPossibles(plateau, couleur, 6));

        String highestStr = null;
        double highest = -9999.0;

        char couleurAdverse = ' ';
        if (couleur == 'R') {
            couleurAdverse = 'B';
        } else {
            couleurAdverse = 'R';
        }


        for (int i = 0; i < actionsPossiblesDepuisPlateau.length; i++) {
            String wholeString = actionsPossiblesDepuisPlateau[i];
            String lettresAction = ActionsPossibles.enleverVitalites(wholeString);

            char lettreAction = lettresAction.charAt(0);
            char lettreLigne = lettresAction.charAt(1);
            char lettreColonne = lettresAction.charAt(2);

            Coordonnees coord = Coordonnees.depuisCars(lettreLigne, lettreColonne);
            Case caseConcernee = plateau[coord.ligne][coord.colonne];
            
            Coordonnees caseConcerneeLignePlusUn = new Coordonnees(coord.ligne + 1, coord.colonne);
            Coordonnees caseConcerneeLigneMoinsUn = new Coordonnees(coord.ligne - 1, coord.colonne);
            Coordonnees caseConcerneeColonnePlusUn = new Coordonnees(coord.ligne, coord.colonne + 1);
            Coordonnees caseConcerneeColonneMoinsUn = new Coordonnees(coord.ligne, coord.colonne - 1);

            int voisinsMoiLignePlusUn = IABiosphere7.nbVoisinesJoueur(caseConcerneeLignePlusUn, plateau, couleur);
            int voisinsMoiLigneMoinsUn = IABiosphere7.nbVoisinesJoueur(caseConcerneeLigneMoinsUn, plateau, couleur);
            int voisinsMoiColonnePlusUn = IABiosphere7.nbVoisinesJoueur(caseConcerneeColonnePlusUn, plateau, couleur);
            int voisinsMoiColonneMoinsUn = IABiosphere7.nbVoisinesJoueur(caseConcerneeColonneMoinsUn, plateau, couleur);
            
            int voisinsMoi = IABiosphere7.nbVoisinesJoueur(coord, plateau, couleur);
            int voisinsAdverse = IABiosphere7.nbVoisinesJoueur(coord, plateau, couleurAdverse);

            double noteAction = 0;

            if (lettreAction == 'P') {
                noteAction = 10.0;

                noteAction += (voisinsMoi * 50.0);
                noteAction += (voisinsMoiLignePlusUn * 10.0);
                noteAction += (voisinsMoiLigneMoinsUn * 10.0);
                noteAction += (voisinsMoiColonneMoinsUn * 10.0);
                noteAction += (voisinsMoiColonnePlusUn * 10.0);

                if (voisinsAdverse > 0) {
                    noteAction -= (120.0 + (voisinsAdverse * 30.0));
                }

                double ecartLigne = 6.5 - coord.ligne;
                if (ecartLigne < 0) {
                    ecartLigne = ecartLigne * -1;
                }

                double ecartColonne = 6.5 - coord.colonne;
                if (ecartColonne < 0) {
                    ecartColonne = ecartColonne * -1;
                }

                double distanceDuCentre = ecartLigne + ecartColonne;
                noteAction += (15.0 - distanceDuCentre);
            }
            else if (lettreAction == 'C') {
                //check pas ma case
                if (caseConcernee.couleur != couleur) {
                    noteAction = caseConcernee.vitalite * 40.0;

                    noteAction += (voisinsMoi * 20.0);

                    noteAction -= (voisinsAdverse * 45.0);

                    if (nbToursJeu > 30) {
                        noteAction += 1.0;
                    }
                } else {
                    if(caseConcernee.vitalite < voisinsMoi){
                        noteAction += (voisinsMoi * 20.0);
                    }else{
                        noteAction = -9999.0;
                    }
                }
            }
            if (noteAction > highest) {
                highest = noteAction;
                highestStr = lettresAction;
            }
        }

        String actionFinal = " ";
        if (highestStr != null) {
            actionFinal = highestStr;
        } else {
            actionFinal = ActionsPossibles.enleverVitalites(actionsPossiblesDepuisPlateau[0]);
        }

        return actionFinal;
    }
    

    /**
     * converti une string de coordonnee en coordonnes (l'objet)
     *
     * @param coord les coordonnees en string
     * @return des coordonnees
     */
    Coordonnees coordsStringToCoords(String coord) {
        String[] separe = coord.split(",");
        String ligneStr = separe[0];
        String colonneStr = separe[1];
        int ligne = Integer.parseInt(ligneStr);
        int colonne = Integer.parseInt(colonneStr);
        Coordonnees coordConvert = new Coordonnees(ligne, colonne);
        return coordConvert;
    }

    /**
     * retourne une liste de string avec les emplacement de mes arbres
     *
     * @param plateau le plateau de jeu
     * @param couleurMonJoueur la couleur du joueur concerné
     * @return une liste de string avec les emplacement des arbres de la couleur concernée
     */
    String[] CaseMonJoueur(Case[][] plateau, char couleurMonJoueur) {
        int nbLignes = 14;
        int nbColonnes = 14;
        String[] listeCaseAmoi = new String[nbLignes * nbColonnes];
        int listeCaseAmoiIndex = 0;

        for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
            for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
                if (plateau[parseLignes][parseColonnes].plantePresente() && plateau[parseLignes][parseColonnes].couleur == couleurMonJoueur) {
                    String FullInfo = parseLignes + "," + parseColonnes;
                    listeCaseAmoi[listeCaseAmoiIndex] = FullInfo;
                    listeCaseAmoiIndex++;
                }
            }
        }
        int tailleTab2 = 0;
        for (int i = 0; i < listeCaseAmoi.length; i++) {
            if (listeCaseAmoi[i] != null) {
                tailleTab2++;
            }
        }
        String[] mesCasesPurged = new String[tailleTab2];
        for (int i = 0, j = 0; i < listeCaseAmoi.length; i++) {
            if (listeCaseAmoi[i] != null) {
                mesCasesPurged[j++] = listeCaseAmoi[i];
            }
        }
        return mesCasesPurged;
    }

    /**
     * L'adversaire joue : on récupère son action, met à jour le plateau, et
     * signale toute disqualification.
     *
     * @param plateau le plateau de jeu
     * @param couleurAdversaire couleur de l'adversaire
     * @return l'action choisie sous forme de chaîne
     */
    boolean adversaireJoue(Case[][] plateau, char couleurAdversaire) {
        boolean disqualification = false;
        System.out.println("Attente de réception action adversaire...");
        String actionAdversaire = grandOrdo.recevoirAction();
        System.out.println("Action adversaire reçue : " + actionAdversaire);
        if ("Z".equals(actionAdversaire)) {
            System.out.println("L'adversaire est disqualifié.");
            disqualification = true;
        } else {
            System.out.println("L'adversaire joue : "
                    + actionAdversaire + ".");
            mettreAJour(plateau, actionAdversaire, couleurAdversaire);
        }
        return disqualification;
    }

    /**
     * Calcule la couleur du prochain joueur.
     *
     * @param couleurCourante la couleur du joueur courant
     * @return la couleur du prochain joueur
     */
    static char suivant(char couleurCourante) {
        return couleurCourante == Case.CAR_ROUGE
                ? Case.CAR_BLEU : Case.CAR_ROUGE;
    }

    /**
     * Mettre à jour le plateau suite à une action, supposée valide.
     *
     * @param plateau le plateau
     * @param action l'action à appliquer
     * @param couleurCourante couleur du joueur courant
     */
    static void mettreAJour(Case[][] plateau, String action, char couleurCourante) {
        // vérification des arguments
        if (plateau == null || action == null || action.length() != 3) {
            return;
        }
        Coordonnees coord = Coordonnees.depuisCars(action.charAt(1), action.charAt(2));
        switch (action.charAt(0)) {
            case 'P' ->
                planter(coord, plateau, couleurCourante);
            case 'C' ->
                couper(coord, plateau);
            default ->
                System.out.println("Type d'action incorrect : " + action.charAt(0));
        }
    }

    /**
     * Mettre à jour le plateau suite à une action, supposée valide.
     *
     * @param plateau le plateau
     * @param action l'action à appliquer
     * @param couleurCourante couleur du joueur courant
     */
    static void mettreAJour_niveau8(Case[][] plateau, String action, char couleurCourante) {
        // A COMPLETER, POUR LES BINOMES DE LA LIGUE 8
        // vérification des arguments
        if (plateau == null || action == null || action.length() != 3) {
            return;
        }
        Coordonnees coord = Coordonnees.depuisCars(action.charAt(1), action.charAt(2));
        switch (action.charAt(0)) {
            case 'P' ->
                planter(coord, plateau, couleurCourante);
            case 'C' ->
                couper(coord, plateau);
            default ->
                System.out.println("Type d'action incorrect : " + action.charAt(0));
        }
    }

    /**
     * Planter un pommier sur une case donnée.
     *
     * @param coord coordonnées de la case
     * @param plateau le plateau de jeu
     * @param couleurCourante la couleur du joueur courant
     */
    static void planter(Coordonnees coord, Case[][] plateau, char couleurCourante) {
        Case laCase = plateau[coord.ligne][coord.colonne];
        laCase.couleur = couleurCourante;
        laCase.espece = Case.CAR_POMMIER;
        laCase.vitalite = 1 + nbVoisinesJoueur(coord, plateau, couleurCourante);
    }

    /**
     * Nombre de cases voisines d'une case et contenant une plante du joueur.
     *
     * @param coord la case dont on souhaite analyser les voisines
     * @param plateau le plateau courant
     * @param couleurCourante la couleur du joueur courant
     * @return le nombre de cases voisines contenant une plante du joueur
     */
    static int nbVoisinesJoueur(Coordonnees coord, Case[][] plateau, char couleurCourante) {
        return (int) voisines(coord)
                .map(v -> plateau[v.ligne][v.colonne])
                .filter(c -> c.plantePresente())
                .filter(c -> c.couleur == couleurCourante)
                .count();
    }

    /**
     * Les coordonnées des cases voisines dans le plateau.
     *
     * @param coord les coordonnées de la case d'origine
     * @return les coordonnées des cases voisines
     */
    static Stream<Coordonnees> voisines(final Coordonnees coord) {
        return Stream.of(new int[][]{{-1, 0}, {0, -1}, {1, 0}, {0, 1}})
                .map(d -> new Coordonnees(coord.ligne + d[0], coord.colonne + d[1]))
                .filter(v -> 0 <= v.ligne && v.ligne < Coordonnees.NB_LIGNES)
                .filter(v -> 0 <= v.colonne && v.colonne < Coordonnees.NB_COLONNES);
    }

    /**
     * Couper une plante sur une case donnée.
     *
     * @param coord coordonnées de la case
     * @param plateau le plateau de jeu
     */
    static void couper(Coordonnees coord, Case[][] plateau) {
        retirerPlante(plateau[coord.ligne][coord.colonne]);
        voisines(coord)
                .map(v -> plateau[v.ligne][v.colonne])
                .filter(c -> c.plantePresente())
                .forEach(c -> c.vitalite = Math.min(9, c.vitalite + 1));
    }

    /**
     * Retirer une plante d'une case.
     *
     * @param laCase la case dont on doit retirer la plante
     */
    static void retirerPlante(Case laCase) {
        laCase.espece = Case.CAR_VIDE;
        laCase.couleur = Case.CAR_ROUGE;
        laCase.vitalite = 0;
    }

    /**
     * Programme principal. Il sera lancé automatiquement, ce n'est pas à vous
     * de le lancer.
     *
     * @param args Arguments.
     */
    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
        System.out.println("Démarrage le " + format.format(new Date()));
        System.out.flush();
        // « create » du protocole du grand ordonnateur.
        final String USAGE
                = System.lineSeparator()
                + "\tUsage : java " + IABiosphere7.class.getName()
                + " <hôte> <port> <ordre>";
        if (args.length != 3) {
            System.out.println("Nombre de paramètres incorrect." + USAGE);
            System.out.flush();
            System.exit(1);
        }
        String hote = args[0];
        int port = -1;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Le port doit être un entier." + USAGE);
            System.out.flush();
            System.exit(1);
        }
        int ordre = -1;
        try {
            ordre = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("L'ordre doit être un entier." + USAGE);
            System.out.flush();
            System.exit(1);
        }
        try {
            char couleurJoueur = (ordre == 1 ? 'R' : 'B');
            IABiosphere7 iaLowatem = new IABiosphere7(hote, port, couleurJoueur);
            iaLowatem.connexion();
            iaLowatem.toursDeJeu();
        } catch (IOException e) {
            System.out.println("Erreur à l'exécution du programme : \n" + e);
            System.out.flush();
            System.exit(1);
        }
    }
}
