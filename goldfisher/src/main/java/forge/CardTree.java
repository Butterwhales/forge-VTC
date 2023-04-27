package forge;

import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.spellability.SpellAbility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Generates a tree of possible orders to play cards in
 */
public class CardTree {
    List<CardNode> roots = new ArrayList<>();

    /**
     * Generates the Tree
     *
     * @param cards     - The cards that go in to the tree (The Players hand)
     * @param manaAvail - The Players available mana
     */
    public void generateTree(CardCollectionView cards, int manaAvail, int landsPlayed, boolean canPlaySorcery, int enemyHealth) {
        ExecuteTimer t = new ExecuteTimer();
        for (Card card : cards) {
            addRoot(card);
        }

        for (CardNode root : roots) {
            addBranches(root, removeCard(root.card, cards));
        }

        boolean playedLand = landsPlayed != 0 || !canPlaySorcery;
//        System.out.println("Lands played: " + playedLand);

        pruneLeaves(manaAvail, playedLand, canPlaySorcery);

        grade(enemyHealth);
        long totalLeaves = countLeaves();

        t.end();

        if (roots.isEmpty())
            return;

        //Debug Prints
        System.out.println("Mana Avail: " + manaAvail + " Enemy Health: " + enemyHealth);
        System.out.println("Tree Generation Took: " + t + " Total Roots: " + roots.size() + " Total Leaves: " + totalLeaves);
        System.out.println("Roots " + roots.size() + ": ");
        for (CardNode root : roots) {
            System.out.println(root);
        }
        System.out.println();
        System.out.println("Cards " + cards.size() + ": " + cards);
    }

    /**
     * Grades the tree
     */
    public void grade(int enemyHealth) {
        for (CardNode root : roots) {
            root.grade(enemyHealth);
        }
    }

    /**
     * Counts the number of leaves on the tree
     *
     * @return - number of leaves on the tree
     */
    public long countLeaves() {
        long totalLeaves = 0;
        for (CardNode root : roots) {
            totalLeaves += root.countLeaves(0);
        }
        return totalLeaves;
    }

    /**
     * Removes unnecessary leaves from the tree
     *
     * @param mana - The players available mana
     */
    public void pruneLeaves(int mana, boolean landPlayed, boolean canPlaySorcery) {
        for (int i = roots.size() - 1; i >= 0; i--) {
            CardNode root = roots.get(i);
            if (root.card.isSorcery() && !canPlaySorcery) {
                roots.remove(i);
            } else if (mana < root.cmc) {
                roots.remove(i);
            } else if (root.card.isLand() && landPlayed) {
                roots.remove(i);
            } else {
                root.pruneLeaves(landPlayed, mana);
            }
        }
    }

    /**
     * Adds a new root node to the tree
     *
     * @param card - The card being added
     */
    void addRoot(Card card) {
        roots.add(new CardNode(card));
    }

    /**
     * Adds the branches to the tree
     *
     * @param root  - The root node
     * @param cards - The cards that will be used to create the branches
     */
    void addBranches(CardNode root, ArrayList<Card> cards) {
        root.addBranches(root, cards);
    }

    /**
     * Removes a card from the list
     *
     * @param card  - The card being removed.
     * @param cards - The list the card is being removed from.
     * @return - The list of cards without the removed card.
     */
    ArrayList<Card> removeCard(Card card, CardCollectionView cards) {
        ArrayList<Card> newList = new ArrayList<>((Collection) cards);
        newList.remove(card);
        return newList;
    }

    /**
     * Removes all roots from the tree
     * Necessary to assist in garbage collection
     */
    void empty() {
        roots.clear();
    }

    public Card getBestCard() {
        CardNode bestRoot = getBestRoot();
        if (bestRoot == null)
            return null;
        return bestRoot.card;
    }

    private CardNode getBestRoot() {
        int highestGrade = 0;
        if (roots.isEmpty()) {
            return null;
        }

        CardNode bestRoot = roots.get(0);


        for (CardNode root : roots) {
            if (root.getGrade() > highestGrade) {
                bestRoot = root;
                highestGrade = root.getGrade();
            }
        }
        return bestRoot;
    }

    @Override
    public String toString() {
        String string = "RootNode { ";
        for (CardNode root : roots) {
            string = string.concat(root.toString());
//            string = string.concat(root.card.getName() + " ");
//            string = string.concat(String.valueOf(root.getScore()));
            if (roots.indexOf(root) != roots.size() - 1) {
                string = string.concat("| ");
            }
        }
        string = string.concat("}");

        return string;
    }

    public CardCollection getPredictedSpells() {
        CardNode bestRoot = getBestRoot();
        if (bestRoot != null){
            return bestRoot.getPredictedSpells();
        }
        return null;
    }
}
