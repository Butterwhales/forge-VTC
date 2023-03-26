package forge;

import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

public class CardNode {
    Card card;
    List<CardNode> leaves;
    /**
     * The value of a card
     */
    int value;
    /**
     * The accumulated value of every card under it
     */
    int maxValue;
    /**
     * The Converted Mana Cost of the Card
     */
    int cmc;
    /**
     * Total Damage of the branch
     */
    int totalDamage;

    /**
     * Represents a card
     *
     * @param key - The card
     * @value value - The score given to a card based on how powerful it is
     * @value leaves - A list of the child nodes
     */
    public CardNode(Card key) {
        card = key;
        value = CardValues.getValue(key);
        leaves = new ArrayList<>();
        cmc = key.getCMC();
    }

    /**
     * Creates a node out of the given card.
     * Adds that node to the tree.
     *
     * @param key - The card
     * @return - A new CardNode
     */
    public CardNode addNode(Card key) {
        CardNode newNode = new CardNode(key);
        leaves.add(newNode);
        return newNode;
    }

    /**
     * Adds branches to a given CardNode
     *
     * @param node  - The node that branches are going to be added to
     * @param cards - The cards to add to the tree
     */
    public void addBranches(CardNode node, ArrayList<Card> cards) {
        for (Card card : cards) {
            CardNode newNode = node.addNode(card);
            newNode.addBranches(newNode, removeCardFromList(card, cards));
        }
    }

    /**
     * Removes a specific card from the list of cards
     *
     * @param card  - The Card being removed
     * @param cards - The list of cards that the card is being removed from
     * @return A list of cards that does not contain the removed card
     */
    ArrayList<Card> removeCardFromList(Card card, ArrayList<Card> cards) {
        ArrayList<Card> newList = new ArrayList<>(cards);
        newList.remove(card);
        return newList;
    }

    //    int enemyHealth

    /**
     * Grades the tree
     */
    public void grade(int enemyHealth) {
        int maxLeafDamage = 0;
        for (CardNode leaf : leaves) {
            leaf.grade(enemyHealth);
            maxValue += leaf.value;
            if (leaf.totalDamage > maxLeafDamage){
                maxLeafDamage = leaf.totalDamage;
            }
        }

        if (!card.isLand()) {
            if (card.getName().equals("Price of Progress")){
                totalDamage = 0;


                for(Card c: CardLists.filter(card.getController().getSingleOpponent().getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.LANDS)){
                    if (!c.isBasicLand()) {
                        totalDamage += 2;
                    }
                }
            } else {
                //card.getAllSpellAbilities();
                totalDamage += 3; //TODO set based on card
            }

        }

        totalDamage += maxLeafDamage;

        if (enemyHealth < totalDamage) {
            maxValue += 10; //Add 10 for terminal state
        }
        maxValue += value + totalDamage;
    }

    /**
     * Counts the total leaves on a branch
     *
     * @param acc - The accumulated # of all the cards in the branch
     * @return the number leaves on the given branch
     */
    public long countLeaves(long acc) {
        if (leaves.isEmpty()) {
            acc++;
        } else {
            for (CardNode leaf : leaves) {
                acc += leaf.countLeaves(0);
            }
        }
        return acc;
    }

    /**
     * Removes unnecessary leaves from a branch
     *
     * @param landPlayed - Was a land played this turn
     * @param mana       - The players available mana
     */
    public void pruneLeaves(boolean landPlayed, int mana) {
        mana -= cmc;

        if (card.isLand()) {
            landPlayed = true;
            mana += 1;
        }

        for (int i = leaves.size() - 1; i >= 0; i--) {
            CardNode leaf = leaves.get(i);
            if (mana - leaf.cmc < 0) {
                leaves.remove(leaf);
            }
            if (landPlayed && leaf.card.isLand()) {
                leaves.remove(leaf);
            }

            leaf.pruneLeaves(landPlayed, mana);
        }
    }

    /**
     * Removes all the leaves
     */
    public void removeLeaves() {
        leaves.clear();
    }

    public int getGrade() {
        return maxValue;
    }

    @Override
    public String toString() {
        String string = card.getName() + "(" + maxValue + ", " + totalDamage + ")"+ "[ ";
        for (CardNode leaf : leaves) {
            string = string.concat(leaf.toString());

            if (leaves.indexOf(leaf) != leaves.size() - 1) {
                string = string.concat(", ");
            }
        }
        string = string.concat("]");

        return string;
    }
}
