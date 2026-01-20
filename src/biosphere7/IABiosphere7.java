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
        MonTacheron monTacheron = new MonTacheron(couleur);

        JoueurBiosphere7 joueur = new JoueurBiosphere7();
        String[] actionsPossiblesDepuisPlateau
                = joueur.actionsPossibles(plateau, couleur, 6);

        ActionsPossibles actionsPossibles
                = new ActionsPossibles(actionsPossiblesDepuisPlateau);

        actionsPossibles.afficher();

        
        String highestStr = " ";

        Vitalites scoreActuel = JoueurBiosphere7.vitalitesPlateau(plateau);


        int nbPommiersBleu = nbPommier(plateau, 'B');
        int nbPommiersRouge = nbPommier(plateau, 'R');
        char couleurAdverse = ' ';
        if (couleur == 'R') {
            couleurAdverse = 'B';
        } else {
            couleurAdverse = 'R';
        }
        int nbPommiersMoi = nbPommier(plateau, couleur);

        int HighestVitaMoi = highestVita(plateau, couleur);
        int HighestVitaAdverse = highestVita(plateau, couleurAdverse);

        Coordonnees CentreCluster = BuildPatternCentre(plateau, couleur);
        
        Coordonnees bestCaseClusterAdverse =  DetectPatternCoord(plateau, couleurAdverse);
        int nbCaseClusterAdverse = DetectPatternNb(plateau, couleurAdverse);
        
        Coordonnees bestCaseClusterMoi =  DetectPatternCoord(plateau, couleur);
        int nbCaseClusterMoi = DetectPatternNb(plateau, couleur);

        if (nbPommiersMoi <= 2 || (nbPommiersMoi >= 3 && HighestVitaMoi > 2)) {//ici forcément égalité contre le cadet
            String[] listeAction = new String[0];

            int Rouge;
            int Bleu;

            int highest = -99;

            for (int TableauEcartZeroIndexDeLindex = 0; TableauEcartZeroIndexDeLindex < actionsPossiblesDepuisPlateau.length; TableauEcartZeroIndexDeLindex++) {
                String[] separatedScoreBaseDeux = actionsPossiblesDepuisPlateau[TableauEcartZeroIndexDeLindex].split(",");
                String RougeStrDeux = separatedScoreBaseDeux[1];
                String BleuStrDeux = separatedScoreBaseDeux[2];

                Rouge = Integer.parseInt(RougeStrDeux);
                Bleu = Integer.parseInt(BleuStrDeux);

                if (Bleu - Rouge >= highest) {//changer pour les simu
                    highest = Bleu - Rouge;
                    highestStr = actionsPossiblesDepuisPlateau[TableauEcartZeroIndexDeLindex];
                }
            }

        }
        else if (nbPommiersMoi >= 3 && HighestVitaMoi <= 3) {//cluster highestVitaMoi pour savoir si c'est rentable !!! si deja config exploitable alors la jouer
            int bestLigne = ligneAvecLePlusDeCases(plateau, couleur)[1]; //numéro de ligne = [1] (changer pour ne pas avoir la 1ere ligne ?)

            if (nbToursJeu <= 2 && (nbCaseClusterAdverse > nbCaseClusterMoi) ){
                    highestStr = "" + "C" + bestCaseClusterAdverse.carLigne() + bestCaseClusterAdverse.carColonne();
            } else {
                if (CentreCluster != new Coordonnees(13, 13)) {
                    highestStr = "" + "P" + CentreCluster.carLigne() + CentreCluster.carColonne();
                }else {//sinon on le construit
                    Coordonnees emplacement = BuildPatternHorizontale(plateau, couleur, bestLigne);
                    highestStr = "" + "P" + emplacement.carLigne() + emplacement.carColonne();
                }
            }
        }

        String actionAfaire = actionsPossiblesDepuisPlateau[0];
        if (highestStr.matches("[PC][a-n][A-N],[0-9]{1,2},[0-9]{1,2}")) {
            actionAfaire = highestStr;
        }

        String actionFinal = ActionsPossibles.enleverVitalites(actionAfaire);
        return actionFinal;
        //"PbA,4,6"
        //return monTacheron.actionChoisie(plateau, nbToursJeu);
    }
    
    //retourne la plus haute vitalité d'une plante de la couleur du joueur en paramètre
    int highestVita(Case[][] plateau, char couleur) {
        int nbColonnes = 14;
        int nbLignes = 14;
        int maxVita = 0;
        for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
            for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
                if (plateau[parseLignes][parseColonnes].plantePresente() && plateau[parseLignes][parseColonnes].couleur == couleur) {
                    if (plateau[parseLignes][parseColonnes].vitalite > maxVita) {
                        maxVita = plateau[parseLignes][parseColonnes].vitalite;
                    }
                }
            }
        }
        return maxVita;
    }

    int DetectPatternNb(Case[][] plateau, char couleurMonJoueur) {
        int nbColonnes = 14;
        int nbLignes = 14;
        Coordonnees Centre = new Coordonnees(13, 13);
        int possibiliteDeBoost = 0;
        int possibiliteDeBoostMax = 0;

        for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
            for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {

                //init des variables si possible
                if (parseColonnes + 2 < nbColonnes) {
                    Case droitePlusUn = plateau[parseLignes][parseColonnes + 2];
                    if (droitePlusUn.plantePresente() && droitePlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseColonnes - 2 >= 0) {
                    Case gauchePlusUn = plateau[parseLignes][parseColonnes - 2];
                    if (gauchePlusUn.plantePresente() && gauchePlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseLignes - 2 >= 0) {
                    Case HautPlusUn = plateau[parseLignes - 2][parseColonnes];
                    if (HautPlusUn.plantePresente() && HautPlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseLignes + 2 < nbLignes) {
                    Case BasPlusUn = plateau[parseLignes + 2][parseColonnes];
                    if (BasPlusUn.plantePresente() && BasPlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales HautDroit
                if (parseColonnes + 1 < nbColonnes && parseLignes - 1 >= 0) {
                    Case diagHautDroit = plateau[parseLignes - 1][parseColonnes + 1];
                    if (diagHautDroit.plantePresente() && diagHautDroit.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales BasDroit
                if (parseColonnes + 1 < nbColonnes && parseLignes + 1 < nbLignes) {
                    Case diagBasDroit = plateau[parseLignes + 1][parseColonnes + 1];
                    if (diagBasDroit.plantePresente() && diagBasDroit.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales BasGauche
                if (parseColonnes - 1 >= 0 && parseLignes + 1 < nbLignes) {
                    Case diagBasGauche = plateau[parseLignes + 1][parseColonnes - 1];
                    if (diagBasGauche.plantePresente() && diagBasGauche.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales HautGauche
                if (parseColonnes - 1 >= 0 && parseLignes - 1 >= 0) {
                    Case diagHautGauche = plateau[parseLignes - 1][parseColonnes - 1];
                    if (diagHautGauche.plantePresente() && diagHautGauche.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
            }
        }
        return possibiliteDeBoostMax;
    }
    
    //renvoie la coordonnes de la case qui donne le plus de possibilité de boost d'un cluster
    Coordonnees DetectPatternCoord(Case[][] plateau, char couleurMonJoueur) {
        int nbColonnes = 14;
        int nbLignes = 14;
        Coordonnees Centre = new Coordonnees(13, 13);
        int possibiliteDeBoost = 0;
        int possibiliteDeBoostMax = 0;

        for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
            for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {

                if (plateau[parseLignes][parseColonnes].plantePresente() && plateau[parseLignes][parseColonnes].couleur == couleurMonJoueur){
                //init des variables si possible
                if (parseColonnes + 2 < nbColonnes) {
                    Case droitePlusUn = plateau[parseLignes][parseColonnes + 2];
                    if (droitePlusUn.plantePresente() && droitePlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseColonnes - 2 >= 0) {
                    Case gauchePlusUn = plateau[parseLignes][parseColonnes - 2];
                    if (gauchePlusUn.plantePresente() && gauchePlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseLignes - 2 >= 0) {
                    Case HautPlusUn = plateau[parseLignes - 2][parseColonnes];
                    if (HautPlusUn.plantePresente() && HautPlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                if (parseLignes + 2 < nbLignes) {
                    Case BasPlusUn = plateau[parseLignes + 2][parseColonnes];
                    if (BasPlusUn.plantePresente() && BasPlusUn.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales HautDroit
                if (parseColonnes + 1 < nbColonnes && parseLignes - 1 >= 0) {
                    Case diagHautDroit = plateau[parseLignes - 1][parseColonnes + 1];
                    if (diagHautDroit.plantePresente() && diagHautDroit.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales BasDroit
                if (parseColonnes + 1 < nbColonnes && parseLignes + 1 < nbLignes) {
                    Case diagBasDroit = plateau[parseLignes + 1][parseColonnes + 1];
                    if (diagBasDroit.plantePresente() && diagBasDroit.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales BasGauche
                if (parseColonnes - 1 >= 0 && parseLignes + 1 < nbLignes) {
                    Case diagBasGauche = plateau[parseLignes + 1][parseColonnes - 1];
                    if (diagBasGauche.plantePresente() && diagBasGauche.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                //diagonales HautGauche
                if (parseColonnes - 1 >= 0 && parseLignes - 1 >= 0) {
                    Case diagHautGauche = plateau[parseLignes - 1][parseColonnes - 1];
                    if (diagHautGauche.plantePresente() && diagHautGauche.couleur == couleurMonJoueur) {
                        possibiliteDeBoost++;
                        if (possibiliteDeBoost >= possibiliteDeBoostMax){
                            possibiliteDeBoostMax = possibiliteDeBoost;
                            Centre = new Coordonnees(parseLignes, parseColonnes);
                        }
                    }
                }
                }
            }
        }
        return Centre;
    }
    
    //renvoie la coordonnes du centre du cluster si il y a 2 cases au moins pour le début
    Coordonnees BuildPatternCentre(Case[][] plateau, char couleurMonJoueur) {
        int nbColonnes = 14;
        int nbLignes = 14;
        Coordonnees Centre = new Coordonnees(13, 13);

        for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
            for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {

                //init des variables si possible
                if (parseColonnes + 2 < nbColonnes) {
                    Case droitePlusUn = plateau[parseLignes][parseColonnes + 2];
                    if (droitePlusUn.plantePresente() && droitePlusUn.couleur == couleurMonJoueur) {
                        Centre = new Coordonnees(parseLignes, parseColonnes + 1);
                    }
                }
                if (parseColonnes - 2 >= 0) {
                    Case gauchePlusUn = plateau[parseLignes][parseColonnes - 2];
                    if (gauchePlusUn.plantePresente() && gauchePlusUn.couleur == couleurMonJoueur) {
                        Centre = new Coordonnees(parseLignes, parseColonnes - 1);
                    }
                }
                if (parseLignes - 2 >= 0) {
                    Case HautPlusUn = plateau[parseLignes - 2][parseColonnes];
                    if (HautPlusUn.plantePresente() && HautPlusUn.couleur == couleurMonJoueur) {
                        Centre = new Coordonnees(parseLignes - 1, parseColonnes);
                    }
                }
                if (parseLignes + 2 < nbLignes) {
                    Case BasPlusUn = plateau[parseLignes + 2][parseColonnes];
                    if (BasPlusUn.plantePresente() && BasPlusUn.couleur == couleurMonJoueur) {
                        Centre = new Coordonnees(parseLignes + 1, parseColonnes);
                    }
                }
            }
        }
        return Centre;
    }

    //renvoie la coordonnée de la case horizontale pour faire le pattern
    Coordonnees BuildPatternHorizontale(Case[][] plateau, char couleurMonJoueur, int numLigne) {
        int nbColonnes = 14;
        Coordonnees meilleurCordActuelle = new Coordonnees(13, 13);
        for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
            if (parseColonnes + 2 < nbColonnes && !plateau[numLigne][parseColonnes + 2].plantePresente() && plateau[numLigne][parseColonnes].couleur == couleurMonJoueur && plateau[numLigne][parseColonnes].plantePresente()) {
                meilleurCordActuelle = new Coordonnees(numLigne, parseColonnes + 2);
            }
        }
        return meilleurCordActuelle;
    }

    int nbPommier(Case[][] plateau, char couleurAconsulter) {
        int nbLignes = 14;
        int nbColonnes = 14;
        int NbDePommiers = 0;
        for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
            for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
                if (plateau[parseLignes][parseColonnes].plantePresente() && plateau[parseLignes][parseColonnes].couleur == couleurAconsulter) {
                    NbDePommiers++;
                }
            }
        }
        return NbDePommiers;
    }

    //return la ligne avec le plus de cases 
    int[] ligneAvecLePlusDeCases(Case[][] plateau, char couleurMonJoueur) {
        int nbLignes = 14;
        int nbColonnes = 14;

        int[] toReturn = new int[2];
        //toReturn[0] est le nombre de case sur la même ligne
        //toReturn[1] est le numéro de la ligne

        int nbCaseMax = 0;
        int BestLigne = 0;
        for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
            int nbCase = 0;
            for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
                if (plateau[parseLignes][parseColonnes].plantePresente() && plateau[parseLignes][parseColonnes].couleur == couleurMonJoueur) {
                    nbCase++;
                    if (nbCase >= nbCaseMax) { //ici >= prends la ligne la plus basse dans le tableau même si elle a autant de pommiers
                        BestLigne = parseLignes;
                        nbCaseMax = nbCase;
                    }
                }
            }
        }
        toReturn[0] = BestLigne;
        toReturn[1] = nbCaseMax;
        return toReturn;
    }

    //pour le patern (une case entourrée d autre pommier ?) !! rajouter la mm mechanique pour couper (avantage)
    Coordonnees meilleurEmplacement(Case[][] plateau, char couleurMonJoueur) {
        int nbLignes = 14;
        int nbColonnes = 14;

        Coordonnees meilleurCordActuelle = new Coordonnees(1, 1); //to fix
        int nbVoisinsMax = 0;

        for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
            for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
                int nbVoisins = 0;
                if (!plateau[parseLignes][parseColonnes].plantePresente()) {//check que c est une case vide
                    if (parseLignes - 1 >= 0 && plateau[parseLignes - 1][parseColonnes].plantePresente() && plateau[parseLignes - 1][parseColonnes].couleur == couleurMonJoueur) {
                        nbVoisins++;
                    }
                    if (parseLignes + 1 < nbLignes && plateau[parseLignes + 1][parseColonnes].plantePresente() && plateau[parseLignes + 1][parseColonnes].couleur == couleurMonJoueur) {
                        nbVoisins++;
                    }
                    if (parseColonnes - 1 >= 0 && plateau[parseLignes][parseColonnes - 1].plantePresente() && plateau[parseLignes][parseColonnes - 1].couleur == couleurMonJoueur) {
                        nbVoisins++;
                    }
                    if (parseColonnes + 1 < nbColonnes && plateau[parseLignes][parseColonnes + 1].plantePresente() && plateau[parseLignes][parseColonnes + 1].couleur == couleurMonJoueur) {
                        nbVoisins++;
                    }

                    if (nbVoisins > nbVoisinsMax) {
                        meilleurCordActuelle = new Coordonnees(parseLignes, parseColonnes);
                        nbVoisinsMax = nbVoisins;
                    }
                }
            }
        }
        return meilleurCordActuelle;
    }

    //donne les coordonnes d'une case bonne pour faire le pattern
    Coordonnees ConstructionPattern(Case[][] plateau, char couleurMonJoueur) {
        int nbLignes = 14;
        int nbColonnes = 14;

        Coordonnees meilleurCordActuelle = new Coordonnees(2, 2); //to fix
        int nbVoisinsCroixMax = 0;

        for (int parseLignes = 0; parseLignes < nbLignes; parseLignes++) {
            for (int parseColonnes = 0; parseColonnes < nbColonnes; parseColonnes++) {
                int nbVoisinsCroix = 0;
                if (plateau[parseLignes][parseColonnes].plantePresente()) {
                    if (parseLignes - 2 >= 0 && !plateau[parseLignes - 2][parseColonnes].plantePresente()) {
                        nbVoisinsCroix++;
                    }
                    if (parseLignes + 2 < nbLignes && !plateau[parseLignes + 2][parseColonnes].plantePresente()) {
                        nbVoisinsCroix++;
                    }
                    if (parseColonnes - 2 >= 0 && !plateau[parseLignes][parseColonnes - 2].plantePresente()) {
                        nbVoisinsCroix++;
                    }
                    if (parseColonnes + 2 < nbColonnes && !plateau[parseLignes][parseColonnes + 2].plantePresente()) {
                        nbVoisinsCroix++;
                    }
                    if (nbVoisinsCroix > nbVoisinsCroixMax) {
                        meilleurCordActuelle = new Coordonnees(parseLignes, parseColonnes);
                        nbVoisinsCroixMax = nbVoisinsCroix;
                    }
                }
            }
        }

        return meilleurCordActuelle;
    }

    //permet de savoir si on peux planter à coté d'une case
    boolean checkPlanter(Case[][] plateau, Coordonnees coord, String direction) {
        Coordonnees coordHaut = new Coordonnees(coord.ligne - 1, coord.colonne);
        Coordonnees coordBas = new Coordonnees(coord.ligne + 1, coord.colonne);
        Coordonnees coordGauche = new Coordonnees(coord.ligne, coord.colonne - 1);
        Coordonnees coordDroite = new Coordonnees(coord.ligne, coord.colonne + 1);
        switch (direction) {
            case "haut":
                if (coordHaut.estDansPlateau() == true) {
                    if (plateau[coord.ligne - 1][coord.colonne].plantePresente() == true) {
                        return false;
                    } else {
                        return true;
                    }
                }
            case "bas":
                if (coordBas.estDansPlateau() == true) {
                    if (plateau[coord.ligne + 1][coord.colonne].plantePresente() == true) {
                        return false;
                    } else {
                        return true;
                    }
                }
            case "gauche":
                if (coordGauche.estDansPlateau() == true) {
                    if (plateau[coord.ligne][coord.colonne - 1].plantePresente() == true) {
                        return false;
                    } else {
                        return true;
                    }
                }
            case "droite":
                if (coordDroite.estDansPlateau() == true) {
                    if (plateau[coord.ligne][coord.colonne + 1].plantePresente() == true) {
                        return false;
                    } else {
                        return true;
                    }
                }
            default:
                return false;
        }
    }

    /**
     * converti une coordonne de string à coordonnes (l objet)
     *
     * @param coord
     * @return
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
     * @param plateau
     * @param couleurMonJoueur
     * @return
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
