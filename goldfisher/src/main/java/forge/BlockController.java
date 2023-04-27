package forge;

import forge.ai.*;
import forge.ai.AiProps;
import forge.game.GameEntity;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.combat.Combat;
import forge.game.combat.CombatUtil;
import forge.game.player.Player;
import forge.game.staticability.StaticAbilityMustBlock;
import forge.util.collect.FCollectionView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BlockController {
    private final Player goldfisher;
    /**
     * Constant <code>attackers</code>.
     */
    private List<Card> attackers = new ArrayList<>(); // all attackers
    /**
     * Constant <code>attackersLeft</code>.
     */
    private List<Card> attackersLeft = new ArrayList<>(); // keeps track of all currently unblocked attackers
    /**
     * Constant <code>blockedButUnkilled</code>.
     */
    private List<Card> blockedButUnkilled = new ArrayList<>(); // blocked attackers that currently wouldn't be destroyed
    /**
     * Constant <code>blockersLeft</code>.
     */
    private List<Card> blockersLeft = new ArrayList<>(); // keeps track of all unassigned blockers
    private int diff = 0;

    private boolean lifeInDanger = false;

    // set to true when AI is predicting a blocking for another player so it doesn't use hidden information
    private boolean checkingOther = false;

    public BlockController(Player aiPlayer, boolean checkingOther) {
        this.checkingOther = checkingOther;
        this.goldfisher = aiPlayer;
    }

    // finds the creatures able to block the attacker
    private static List<Card> getPossibleBlockers(final Combat combat, final Card attacker, final List<Card> blockersLeft, final boolean solo) {
        final List<Card> blockers = new ArrayList<>();

        for (final Card blocker : blockersLeft) {
            // if the blocker can block a creature with lure it can't block a creature without
            if (CombatUtil.canBlock(attacker, blocker, combat)) {
                boolean cantBlockAlone = blocker.hasKeyword("CARDNAME can't attack or block alone.") || blocker.hasKeyword("CARDNAME can't block alone.");
                if (solo && cantBlockAlone) {
                    continue;
                }
                blockers.add(blocker);
            }
        }

        return blockers;
    }

    private List<Card> sortPotentialAttackers(final Combat combat) {
        final CardCollection sortedAttackers = new CardCollection();
        CardCollection firstAttacker = new CardCollection();
        final FCollectionView<GameEntity> defenders = combat.getDefenders();

        // If I don't have any planeswalkers then sorting doesn't really matter
        if (defenders.size() == 1) {
            final CardCollection attackers = combat.getAttackersOf(defenders.get(0));
            // Begin with the attackers that pose the biggest threat
            ComputerUtilCard.sortByEvaluateCreature(attackers);
            CardLists.sortByPowerDesc(attackers);
            Collections.sort(attackers, (o1, o2) -> {
                if (o1.hasSVar("MustBeBlocked") && !o2.hasSVar("MustBeBlocked")) {
                    return -1;
                }
                if (!o1.hasSVar("MustBeBlocked") && o2.hasSVar("MustBeBlocked")) {
                    return 1;
                }
                return 0;
            });
            return attackers;
        }
        return sortedAttackers;
    }

    public void assignBlockersForCombat(final Combat combat) {
        assignBlockersForCombat(combat, null);
    }

    public void assignBlockersForCombat(final Combat combat, final CardCollection exludedBlockers) {
        List<Card> possibleBlockers = goldfisher.getCreaturesInPlay();
        if (exludedBlockers != null && !exludedBlockers.isEmpty()) {
            possibleBlockers.removeAll(exludedBlockers);
        }
        attackers = sortPotentialAttackers(combat);
        assignBlockers(combat, possibleBlockers);
    }

    private void clearBlockers(final Combat combat, final List<Card> possibleBlockers) {
        for (final Card blocker : CardLists.filterControlledBy(combat.getAllBlockers(), goldfisher)) {
            // don't touch other player's blockers
            combat.removeFromCombat(blocker);
        }

        attackersLeft = new ArrayList<>(attackers); // keeps track of all currently unblocked attackers
        blockersLeft = new ArrayList<>(possibleBlockers); // keeps track of all unassigned blockers
        blockedButUnkilled = new ArrayList<>(); // keeps track of all blocked attackers that currently wouldn't be destroyed
    }

    /**
     * Core blocker assignment algorithm.
     *
     * @param combat           combat instance
     * @param possibleBlockers list of blockers to be considered
     */
    private void assignBlockers(final Combat combat, List<Card> possibleBlockers) {
        System.out.println("Attacking Creatures: " + attackers);
        if (attackers.isEmpty()) {
            return;
        }

        clearBlockers(combat, possibleBlockers);

        // remove all attackers that can't be blocked anyway
        for (final Card a : attackers) {
            if (!CombatUtil.canBeBlocked(a, null, goldfisher)) { // pass null to skip redundant checks for performance
                attackersLeft.remove(a);
            }
        }

        if (attackersLeft.isEmpty()) {
            return;
        }

        // remove all blockers that can't block anyway
        for (final Card b : possibleBlockers) {
            if (!CombatUtil.canBlock(b, combat)) {
                blockersLeft.remove(b);
            }
        }

        // Begin with the weakest blockers
        CardLists.sortByPowerAsc(blockersLeft);

        makeBlocks(combat);
        // == 1. choose best blocks first ==
//        makeGoodBlocks(combat);
//        makeGangBlocks(combat);

        // When the AI holds some Fog effect, don't bother about lifeInDanger
        if (!ComputerUtil.hasAFogEffect(goldfisher, checkingOther)) {
//            lifeInDanger = ComputerUtilCombat.lifeInDanger(goldfisher, combat);
//            if (attackers)
//            makeTradeBlocks(combat); // choose necessary trade blocks

            // if life is still in danger
            if (lifeInDanger) {
//                makeChumpBlocks(combat); // choose necessary chump blocks
            }

            // Reinforce blockers blocking attackers with trample if life is still in danger
            if (lifeInDanger && ComputerUtilCombat.lifeInDanger(goldfisher, combat)) {
//                reinforceBlockersAgainstTrample(combat);
            } else {
                lifeInDanger = false;
            }
            // Support blockers not destroying the attacker with more blockers
            // to try to kill the attacker
            if (!lifeInDanger) {
//                reinforceBlockersToKill(combat);
            }

            // TODO could be made more accurate if this would be inside each blocker choosing loop instead
//            lifeInDanger |= removeUnpayableBlocks(combat) && ComputerUtilCombat.lifeInDanger(ai, combat);

            // == 2. If the AI life would still be in danger make a safer approach ==
            if (lifeInDanger) {
//                clearBlockers(combat, possibleBlockers); // reset every block assignment
//                makeTradeBlocks(combat); // choose necessary trade blocks
//                makeGoodBlocks(combat);
                // choose necessary chump blocks if life is still in danger
//                makeChumpBlocks(combat);

                // Reinforce blockers blocking attackers with trample if life is still in danger
                if (lifeInDanger && ComputerUtilCombat.lifeInDanger(goldfisher, combat)) {
//                    reinforceBlockersAgainstTrample(combat);
                } else {
                    lifeInDanger = false;
                }

//                makeGangBlocks(combat);
//                reinforceBlockersToKill(combat);
            }

            // == 3. If the AI life would be in serious danger make an even safer approach ==
            if (lifeInDanger && ComputerUtilCombat.lifeInSeriousDanger(goldfisher, combat)) {
//                clearBlockers(combat, possibleBlockers);
//                makeChumpBlocks(combat);

                if (lifeInDanger && ComputerUtilCombat.lifeInDanger(goldfisher, combat)) {
//                    makeTradeBlocks(combat);
                } else {
                    lifeInDanger = false;
                }

                if (lifeInDanger && ComputerUtilCombat.lifeInDanger(goldfisher, combat)) {
//                    reinforceBlockersAgainstTrample(combat);
                } else {
                    lifeInDanger = false;
                }

                if (!lifeInDanger) {
//                    makeGoodBlocks(combat);
                }

//                makeGangBlocks(combat);
//                reinforceBlockersToKill(combat);
            }
        }

        // block requirements
        // TODO because this isn't done earlier, sometimes a good block will enforce a restriction that prevents another for the requirement
//        makeRequiredBlocks(combat);

        // check to see if it's possible to defend a Planeswalker under attack with a chump block,
        // unless life is low enough to be more worried about saving preserving the life total
        if (goldfisher.getController().isAI()) {
//            makeChumpBlocksToSavePW(combat);
        }

        // if there are still blockers left, see if it's possible to block Menace creatures with
        // non-lethal blockers that won't kill the attacker but won't die to it as well
//        makeGangNonLethalBlocks(combat);

        //Check for validity of blocks in case something slipped through
        for (Card attacker : attackers) {
            if (!CombatUtil.canAttackerBeBlockedWithAmount(attacker, combat.getBlockers(attacker).size(), combat)) {
                for (final Card blocker : CardLists.filterControlledBy(combat.getBlockers(attacker), goldfisher)) {
                    // don't touch other player's blockers
                    combat.removeFromCombat(blocker);
                }
            }
        }
    }

    private void makeBlocks(Combat combat) {
        final CardCollection blockersList = new CardCollection();

        for (final Card blocker : blockersLeft) {
//            if (CombatUtil.mustBlockAnAttacker(blocker, combat, null) ||
//                    StaticAbilityMustBlock.blocksEachCombatIfAble(blocker)) {
                blockersList.add(blocker);
//            }
        }

        System.out.println("Blockers List: " + blockersList);

        if (!blockersList.isEmpty()) {
            for (final Card attacker : attackers) {
                List<Card> blockers = getPossibleBlockers(combat, attacker, blockersList, false);
                for (final Card blocker : blockers) {
                    if (CombatUtil.canBlock(attacker, blocker, combat) ){
//                            && blockersLeft.contains(blocker)
//                            && (CombatUtil.mustBlockAnAttacker(blocker, combat, null)
//                            || StaticAbilityMustBlock.blocksEachCombatIfAble(blocker))) {
                        System.out.println(blocker + " blocked " + attacker);
                        combat.addBlocker(attacker, blocker);
                    }
                }
            }
        }
    }

    public static CardCollection orderBlockers(Card attacker, CardCollection blockers) {
        // ordering of blockers, sort by evaluate, then try to kill the best
        int damage = attacker.getNetCombatDamage();
        ComputerUtilCard.sortByEvaluateCreature(blockers);
        final CardCollection first = new CardCollection();
        final CardCollection last = new CardCollection();
        for (Card blocker : blockers) {
            int lethal = ComputerUtilCombat.getEnoughDamageToKill(blocker, damage, attacker, true);
            if (lethal > damage) {
                last.add(blocker);
            } else {
                first.add(blocker);
                damage -= lethal;
            }
        }
        first.addAll(last);

        // TODO: Take total damage, and attempt to maximize killing the greatest evaluation of creatures
        // It's probably generally better to kill the largest creature, but sometimes its better to kill a few smaller ones

        return first;
    }

    /**
     * Orders a blocker that put onto the battlefield blocking. Depends heavily
     * on the implementation of orderBlockers().
     */
    public static CardCollection orderBlocker(final Card attacker, final Card blocker, final CardCollection oldBlockers) {
        // add blocker to existing ordering
        // sort by evaluate, then insert it appropriately
        // relies on current implementation of orderBlockers()
        final CardCollection allBlockers = new CardCollection(oldBlockers);
        allBlockers.add(blocker);
        ComputerUtilCard.sortByEvaluateCreature(allBlockers);
        final int newBlockerIndex = allBlockers.indexOf(blocker);

        int damage = attacker.getNetCombatDamage();

        final CardCollection result = new CardCollection();
        boolean newBlockerIsAdded = false;
        // The new blocker comes right after this one
        final Card newBlockerRightAfter = newBlockerIndex == 0 ? null : allBlockers.get(newBlockerIndex - 1);
        if (newBlockerRightAfter == null
                && damage >= ComputerUtilCombat.getEnoughDamageToKill(blocker, damage, attacker, true)) {
            result.add(blocker);
            newBlockerIsAdded = true;
        }
        // Don't bother to keep damage up-to-date after the new blocker is
        // added, as we can't modify the order of the other cards anyway
        for (final Card c : oldBlockers) {
            final int lethal = ComputerUtilCombat.getEnoughDamageToKill(c, damage, attacker, true);
            damage -= lethal;
            result.add(c);
            if (!newBlockerIsAdded && c == newBlockerRightAfter
                    && damage <= ComputerUtilCombat.getEnoughDamageToKill(blocker, damage, attacker, true)) {
                // If blocker is right after this card in priority and we have
                // sufficient damage to kill it, add it here
                result.add(blocker);
                newBlockerIsAdded = true;
            }
        }
        // We don't have sufficient damage, just add it at the end!
        if (!newBlockerIsAdded) {
            result.add(blocker);
        }

        return result;
    }
}
