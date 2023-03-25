package forge;

import forge.game.card.Card;

public class CardValues {

    /**
     * The greater the value the better the card.
     * @param card card that is being valued
     * @return value of the card
     */
    public static int getValue(Card card){
        switch (card.getName()){
            case "Mountain":
                return 11;
            case "Lighting Bolt":
                return 10;
            case "Lava Spike":
                return 9;
            case "Skullcrack":
                return 8;
            case "Incendiary Flow":
                return 7;
            case "Incinerate":
                return 6;
            case "Lighting Strike":
            case "Searing Spear":
                return 5;
            case "Fire Ambush":
            case "Volcanic Hammer":
                return 4;
            case "Annihilating Fire":
                return 1;

            default: return -1;
        }
    }
}
