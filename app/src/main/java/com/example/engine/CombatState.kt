package com.example.engine

import com.example.model.AdventureConfig
import com.example.model.CardEffectType
import com.example.model.CardTemplate
import com.example.model.CardType
import com.example.model.GlobalMutator
import com.example.model.HandCardInstance
import com.example.model.Keyword
import com.example.model.MapNode
import com.example.model.PowerTemplate
import com.example.model.RelicTemplate
import com.example.model.SpellSpeed
import com.example.model.UnitOnBoard

enum class PriorityHolder {
    PLAYER,
    ENEMY;

    fun opponent(): PriorityHolder = if (this == PLAYER) ENEMY else PLAYER
}

enum class CombatPhase {
    MAIN_ACTION_PHASE,
    SPELL_STACK_PHASE,
    COMBAT_DECLARE_ATTACK_PHASE,
    COMBAT_DECLARE_BLOCK_PHASE,
    COMBAT_RESOLUTION_PHASE,
    GAME_OVER
}

data class SpellStackEntry(
    val id: String,
    val sourceIsPlayer: Boolean,
    val card: CardTemplate,
    val targetUnitId: String? = null,
    val damageBonus: Int = 0
)

data class CombatState(
    val encounterNode: MapNode,
    val adventure: AdventureConfig,
    val championId: String,
    val starLevel: Int,
    val equippedRelics: List<RelicTemplate>,
    val activePowers: List<PowerTemplate>,
    val mutators: List<GlobalMutator>,

    val round: Int = 1,
    val playerNexusHp: Int,
    val playerMaxHp: Int,
    val enemyNexusHp: Int,
    val enemyMaxHp: Int,

    val playerMana: Int = 1,
    val playerMaxMana: Int = 1,
    val playerSpellMana: Int = 0,
    val enemyMana: Int = 1,
    val enemyMaxMana: Int = 1,
    val enemySpellMana: Int = 0,

    val playerHasAttackToken: Boolean = true,
    val enemyHasAttackToken: Boolean = false,
    val scoutUsedThisRound: Boolean = false,
    val pendingScoutRally: Boolean = false,
    val playerInitiatedCombat: Boolean = true,
    val blockCommitted: Boolean = false,
    val playerCastSpellInBlock: Boolean = false,
    val enemyCastBurstInBlock: Boolean = false,

    val phase: CombatPhase = CombatPhase.MAIN_ACTION_PHASE,
    val priorityHolder: PriorityHolder = PriorityHolder.PLAYER,
    val consecutivePasses: Int = 0,
    val consecutiveAiActionsInRound: Int = 0,
    val lastStackCaster: PriorityHolder? = null,

    val playerHand: List<HandCardInstance> = emptyList(),
    val playerDrawPile: List<HandCardInstance> = emptyList(),
    val playerGraveyard: List<CardTemplate> = emptyList(),
    val enemyHand: List<CardTemplate> = emptyList(),
    val enemyDrawPool: List<CardTemplate> = emptyList(),
    val enemyGraveyard: List<CardTemplate> = emptyList(),

    val playerBoard: List<UnitOnBoard> = emptyList(),
    val enemyBoard: List<UnitOnBoard> = emptyList(),
    val attackingUnitIds: List<String> = emptyList(),
    val blockPairings: Map<String, String> = emptyMap(), // attackerId -> blockerId
    val confirmedBlockedAttackerIds: Set<String> = emptySet(),
    val challengedPairings: Map<String, String> = emptyMap(), // attackerId -> forcedEnemyBlockerId
    val lockedBlockAttackerIds: Set<String> = emptySet(),
    val playerFastSpellUsedInCombat: Boolean = false,
    val enemyFastSpellUsedInCombat: Boolean = false,
    val spellStack: List<SpellStackEntry> = emptyList(),

    val selectedAttackerForChallenger: String? = null,
    val selectedEnemyAttackerForBlock: String? = null,

    val playerLurkTriggeredThisRound: Boolean = false,
    val enemyLurkTriggeredThisRound: Boolean = false,
    val playerLurkBonus: Int = 0,
    val enemyLurkBonus: Int = 0,
    val roundSupportPowerBuffs: Map<String, Int> = emptyMap(),
    val roundSupportHealthBuffs: Map<String, Int> = emptyMap(),
    val roundSupportKeywords: Map<String, Set<Keyword>> = emptyMap(),
    val playerUniqueKeywordsSeen: Set<Keyword> = emptySet(),
    val playerSpellsManaSpent: Int = 0,
    val playerCardsPlayedThisRound: Int = 0,
    val playerSpellsPlayedThisRound: Int = 0,
    val playerPlunderActive: Boolean = false,
    val enemiesStunnedCount: Int = 0,
    val enemiesFrostbittenCount: Int = 0,
    val enemySuppressionCount: Int = 0,
    val enemyTerrorCount: Int = 0,
    val playerElementalAndSkillsPlayed: Int = 0,
    val isEnlightened: Boolean = false,

    val isFreeAttack: Boolean = false,
    val playerTotalStrikes: Int = 0,
    val enemyTotalStrikes: Int = 0,
    val playerNexusStrikesCount: Int = 0,
    val enemyNexusStrikesCount: Int = 0,
    val playerReputationStrikes: Int = 0,

    val enemyMistStatBonus: Int = 0,
    val enemyViegoStatBonus: Int = 0,
    val playerMistStatBonus: Int = 0,
    val playerViegoStatBonus: Int = 0,
    val enemyAllyDiedThisRound: Boolean = false,
    val playerAllyDiedThisRound: Boolean = false,
    val enemyOtherAllyDiedForViegoThisRound: Boolean = false,
    val playerOtherAllyDiedForViegoThisRound: Boolean = false,
    val playerFollowersDiedThisRound: List<UnitOnBoard> = emptyList(),
    val playerStrongestFollowerDiedLastRound: UnitOnBoard? = null,
    val roundTempPowerBuffs: Map<String, Int> = emptyMap(),
    val roundTempHealthBuffs: Map<String, Int> = emptyMap(),
    val roundTempKeywords: Map<String, Set<Keyword>> = emptyMap(),
    val tempPossessedReturns: Map<String, Boolean> = emptyMap(),

    val playerWon: Boolean? = null,
    val combatLog: List<String> = emptyList()
) {
    val enemyHandCount: Int
        get() = enemyHand.size

    val elementalCardsPlayedCount: Int
        get() = playerElementalAndSkillsPlayed

    val totalPlayerManaAvailable: Int
        get() = playerMana.coerceIn(0, 10) + playerSpellMana.coerceIn(0, 3)

    val totalEnemyManaAvailable: Int
        get() = enemyMana.coerceIn(0, 10) + enemySpellMana.coerceIn(0, 3)

    /**
     * Champion Power milestone regular mana bonus applied every turn:
     * +1 at 2★ (Power 2) and +1 at 5★ (Power 5), stacking up to +2.
     */
    val championMilestoneManaBonus: Int
        get() = (if (starLevel >= 2) 1 else 0) + (if (starLevel >= 5) 1 else 0)

    /**
     * Regular mana gem capacity shown in UI: strictly capped at 10
     * (including both Base Mana and Champion Power milestone bonuses).
     */
    val displayRegularManaSlots: Int
        get() = 10

    val displayEnemyManaSlots: Int
        get() = 10

    fun canPlayCard(card: HandCardInstance): Boolean = CombatEngine.canPlayCard(this, card)

    fun effectivePlayCost(card: HandCardInstance): Int {
        var cost = card.effectiveCost
        val isSpell = card.template.type == CardType.SPELL
        if (isSpell && activePowers.any { it.id == "pow_spellslinger" }) {
            cost -= 1
        }
        if (card.isCreated && activePowers.any { it.id == "pow_wild_inspiration" }) {
            cost -= 1
        }
        if (isSpell && playerHand.any { it.template.id == "card_curse_suppression" }) {
            cost += 1
        }
        if (card.template.id == "card_clash_of_giants") {
            cost -= minOf(10, playerElementalAndSkillsPlayed)
        }
        return maxOf(0, cost)
    }

    fun previewManaPayment(card: HandCardInstance): ManaPaymentPreview {
        val cost = effectivePlayCost(card)
        val isSpellLike = card.template.type == CardType.SPELL || card.template.type == CardType.CURSE
        val spellAvail = playerSpellMana.coerceIn(0, 3)
        val regularAvail = playerMana.coerceIn(0, 10)

        val spellUsed = if (isSpellLike) minOf(spellAvail, cost) else 0
        val remainingCost = (cost - spellUsed).coerceAtLeast(0)
        val regularUsed = minOf(regularAvail, remainingCost)
        val shortfall = (remainingCost - regularUsed).coerceAtLeast(0)
        val hasEnoughMana = shortfall == 0
        val canPlay = CombatEngine.canPlayCard(this, card)

        val isUnitLike = card.template.type == CardType.UNIT ||
            card.template.type == CardType.CHAMPION ||
            card.template.type == CardType.LANDMARK
        val isInCombat = phase == CombatPhase.COMBAT_DECLARE_ATTACK_PHASE ||
            phase == CombatPhase.COMBAT_DECLARE_BLOCK_PHASE

        val blockReason = when {
            !hasEnoughMana -> if (isSpellLike) {
                "Không đủ Năng Lượng (thiếu $shortfall NL)"
            } else {
                "Không đủ NL Thường (thiếu $shortfall NL)"
            }
            canPlay -> null
            priorityHolder != PriorityHolder.PLAYER -> "Chờ lượt của đối thủ"
            isUnitLike && playerBoard.size >= 6 -> "Hàng chờ đã đầy (6/6)"
            card.template.requiresAllySacrificeOnPlay && playerBoard.none { it.template.type != CardType.LANDMARK } ->
                "Cần hy sinh 1 đồng minh trên bàn"
            card.template.effectType == CardEffectType.SACRIFICE_DAMAGE &&
                (playerBoard.none { it.template.type != CardType.LANDMARK } || enemyBoard.none { it.template.type != CardType.LANDMARK }) ->
                "Cần ít nhất 1 đồng minh và 1 kẻ địch"
            card.template.effectType == CardEffectType.POSSESS_UNIT &&
                (enemyBoard.none { it.template.type != CardType.LANDMARK } || playerBoard.size >= 6) ->
                if (playerBoard.size >= 6) "Hàng chờ đã đầy (6/6)" else "Không có kẻ địch để Chiếm Hữu"
            card.template.type == CardType.EQUIPMENT && playerBoard.isEmpty() -> "Cần có đồng minh trên bàn"
            card.template.spellSpeed == SpellSpeed.FAST && isInCombat && playerFastSpellUsedInCombat ->
                "Mỗi đợt giao tranh chỉ dùng 1 Phép Nhanh"
            card.template.spellSpeed == SpellSpeed.FOCUS && (isInCombat || spellStack.isNotEmpty()) ->
                "Phép Tập Trung chỉ dùng ngoài giao tranh"
            card.template.spellSpeed == SpellSpeed.SLOW && (isInCombat || spellStack.isNotEmpty()) ->
                "Phép Chậm chỉ dùng ngoài giao tranh"
            else -> "Không thể ra bài lúc này"
        }

        return ManaPaymentPreview(
            cardCost = cost,
            isSpellLike = isSpellLike,
            spellManaUsed = spellUsed,
            regularManaUsed = regularUsed,
            spellManaRemaining = (spellAvail - spellUsed).coerceIn(0, 3),
            regularManaRemaining = (regularAvail - regularUsed).coerceIn(0, 10),
            regularShortfall = shortfall,
            hasEnoughMana = hasEnoughMana,
            canPlay = canPlay,
            blockReason = blockReason
        )
    }
}

data class ManaPaymentPreview(
    val cardCost: Int,
    val isSpellLike: Boolean,
    val spellManaUsed: Int,
    val regularManaUsed: Int,
    val spellManaRemaining: Int,
    val regularManaRemaining: Int,
    val regularShortfall: Int,
    val hasEnoughMana: Boolean,
    val canPlay: Boolean,
    val blockReason: String?
)

