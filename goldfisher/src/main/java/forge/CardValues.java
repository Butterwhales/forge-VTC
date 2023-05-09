package forge;

import forge.game.card.Card;

public class CardValues {

    /**
     * The greater the value the better the card.
     *
     * @param card card that is being valued
     * @return value of the card
     */
    public static int getValue(Card card) {
        switch (card.getName()) {
            case "Mountain":
                return 0;
            case "Lightning Bolt":
            case "Chain Lightning": // add the ability to copy when mana is available
            case "Lava Spike":
            case "Rift Bolt":
            case "Boil":
                return 1;
            case "Skewer the Critics":
            case "Skullcrack":
            case "Incendiary Flow":
            case "Price of Progress":
            case "Incinerate":
            case "Lightning Strike":
            case "Searing Spear":
            case "Fire Ambush":
            case "Volcanic Hammer":
                return 2;
            case "Annihilating Fire":
                return 3;
            case "Eidolon of the Great Revel":
                return 5;
            case "Goblin Guide":
            case "Monastery Swiftspear":
                return 6;
            case "Ensnaring Bridge":
                    return 20;

            default:
                return -1;
        }

//        switch (card.getName()){
//            case "Mountain":
//                return 11;
//            case "Lightning Bolt":
//                return 10;
//            case "Lava Spike":
//                return 9;
//            case "Skullcrack":
//                return 8;
//            case "Incendiary Flow":
//                return 7;
//            case "Incinerate":
//                return 6;
//            case "Lightning Strike":
//            case "Searing Spear":
//                return 5;
//            case "Fire Ambush":
//            case "Volcanic Hammer":
//                return 4;
//            case "Annihilating Fire":
//                return 1;
//
//            default: return -1;
//        }
    }
}
