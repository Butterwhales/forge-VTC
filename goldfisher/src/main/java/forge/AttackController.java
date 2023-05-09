package forge;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import forge.ai.ComputerUtilCost;
import forge.ai.ability.AnimateAi;
import forge.game.GameEntity;
import forge.game.ability.ApiType;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardLists;
import forge.game.combat.Combat;
import forge.game.combat.CombatUtil;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityPredicates;
import forge.game.zone.ZoneType;
import forge.util.Aggregates;
import forge.util.collect.FCollectionView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AttackController {
    // possible attackers and blockers
    private List<Card> attackers;
    private List<Card> blockers;

    private List<Card> oppList; // holds human player creatures
    private List<Card> myList; // holds computer creatures

    private final Player goldfisher;
    private Player defendingOpponent;

    private int aiAggression = 0; // how aggressive the ai is attack will be depending on circumstances
    private final boolean nextTurn; // include creature that can only attack/block next turn

    public AttackController(final Player goldfisher) {
        this(goldfisher, false);
    } // constructor

    public AttackController(final Player goldfisher, boolean nextTurn) {
        this.goldfisher = goldfisher;
        defendingOpponent = choosePreferredDefenderPlayer(goldfisher, true);
        myList = goldfisher.getCreaturesInPlay();
        this.nextTurn = nextTurn;
        refreshCombatants(defendingOpponent);
    } // overloaded constructor to evaluate attackers that should attack next turn

    public AttackController(final Player goldfisher, Card attacker) {
        this.goldfisher = goldfisher;
        defendingOpponent = choosePreferredDefenderPlayer(goldfisher, true);
        this.oppList = getOpponentCreatures(defendingOpponent);
        myList = goldfisher.getCreaturesInPlay();
        this.nextTurn = false;
        this.attackers = new ArrayList<>();
        if (CombatUtil.canAttack(attacker, defendingOpponent)) {
            attackers.add(attacker);
        }
        this.blockers = getPossibleBlockers(oppList, this.attackers, this.nextTurn);
    } // overloaded constructor to evaluate single specified attacker

    private void refreshCombatants(GameEntity defender) {
        this.oppList = getOpponentCreatures(defendingOpponent);
        this.attackers = new ArrayList<>();
        for (Card c : myList) {
            if (canAttackWrapper(c, defender)) {
                attackers.add(c);
            }
        }
        this.blockers = getPossibleBlockers(oppList, this.attackers, this.nextTurn);
    }

    public static Player choosePreferredDefenderPlayer(Player goldfish, boolean forCombatDmg) {
        Player defender = goldfish.getWeakestOpponent(); //Concentrate on opponent within easy kill range
        return defender;
    }

    public static List<Card> getOpponentCreatures(final Player defender) {
        List<Card> defenders = defender.getCreaturesInPlay();
        Predicate<Card> canAnimate = new Predicate<Card>() {
            @Override
            public boolean apply(Card c) {
                return !c.isTapped() && !c.isCreature() && !c.isPlaneswalker();
            }
        };
        for (Card c : CardLists.filter(defender.getCardsIn(ZoneType.Battlefield), canAnimate)) {
            if (c.isToken() && c.getCopiedPermanent() == null) {
                continue;
            }
            for (SpellAbility sa : Iterables.filter(c.getSpellAbilities(), SpellAbilityPredicates.isApi(ApiType.Animate))) {
                if (ComputerUtilCost.canPayCost(sa, defender, false)
                        && sa.getRestrictions().checkOtherRestrictions(c, sa, defender)) {
                    Card animatedCopy = AnimateAi.becomeAnimated(c, sa);
                    defenders.add(animatedCopy);
                }
            }
        }
        return defenders;
    }

    private boolean canAttackWrapper(final Card attacker, final GameEntity defender) {
        if (nextTurn) {
            return CombatUtil.canAttackNextTurn(attacker, defender);
        } else {
            return CombatUtil.canAttack(attacker, defender);
        }
    }

    public final static List<Card> getPossibleBlockers(final List<Card> blockers, final List<Card> attackers, final boolean nextTurn) {
        return CardLists.filter(blockers, new Predicate<Card>() {
            @Override
            public boolean apply(final Card c) {
                return canBlockAnAttacker(c, attackers, nextTurn);
            }
        });
    }

    public final static boolean canBlockAnAttacker(final Card c, final List<Card> attackers, final boolean nextTurn) {
        return getCardCanBlockAnAttacker(c, attackers, nextTurn) != null;
    }

    public final static Card getCardCanBlockAnAttacker(final Card c, final List<Card> attackers, final boolean nextTurn) {
        final List<Card> attackerList = new ArrayList<>(attackers);
        if (!c.isCreature()) {
            return null;
        }
        for (final Card attacker : attackerList) {
            if (CombatUtil.canBlock(attacker, c, nextTurn)) {
                return attacker;
            }
        }
        return null;
    }

    public final int declareAttackers(final Combat combat) {
        CardCollection oppCreatures = defendingOpponent.getCreaturesInPlay();
        int maxOppDef = 0;
        int maxOppPow = 0;
        for(Card creature : oppCreatures){
            if (maxOppPow < creature.getCurrentPower()){
                maxOppPow = creature.getCurrentPower();
            }
            if (maxOppDef < creature.getCurrentToughness()){
                maxOppDef = creature.getCurrentToughness();
            }
        }

        CardCollection creatures = goldfisher.getCreaturesInPlay();
        for (Card creature : creatures) {
            final FCollectionView<GameEntity> defs = combat.getDefenders();
            GameEntity defender = defs.getFirst();
            if (creature.getCurrentToughness() > maxOppPow && creature.getCurrentPower() >= maxOppDef){
                combat.addAttacker(creature, defender);
            }
        }
        return 0;
    }
}
