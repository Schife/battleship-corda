package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.serialization.CordaSerializable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class SendAttackFlow(val gameId: UUID, val player: String, val xCoord: Int, val yCoord: Int, val round: Int) : FlowLogic<HitPositionDTO>() {

    @Suspendable
    override fun call(): HitPositionDTO {
        val gameService = serviceHub.cordaService(GameService::class.java)
        val attackerName = ourIdentity.name.toString()
        val hitDTO = subFlow(ReceiveAttackFlow(gameId, player, attackerName, xCoord, yCoord, round))
        val game = gameService.findGameById(hitDTO.game.gameId)
        val player = gameService.getPlayerByID(game.gameId, hitDTO.gamePlayer.gamePlayerName)
        val attacker = gameService.getPlayerByID(game.gameId, hitDTO.attacker.gamePlayerName)
        val hit = hitDTO.toEntity()
        hit.gamePlayer = player
        hit.game = game
        hit.attacker = attacker
        serviceHub.withEntityManager {
            if(hitDTO.gamePlayer.playerStatus == PlayerStatus.SUNKEN) {
                gameService.sinkPlayerByID(gameId, hitDTO.gamePlayer.gamePlayerName)
            }
            if(hitDTO.game.gameStatus == GameStatus.DONE) {
                gameService.setGameStatus(gameId, GameStatus.DONE)
            }
            persist(hit)
        }

        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { "Captain" in it.name.organisation }
                .filter { !serviceHub.networkMapCache.isNotary(it.party) }
                .map { initiateFlow(it.party) }
        sessions.forEach { it.send(hitDTO) }
        return HitPositionDTO.fromEntity(hit)
    }
}

@InitiatedBy(SendAttackFlow::class)
class SendAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val gameService = serviceHub.cordaService(GameService::class.java)
        val hitPositionDTO = counterpartySession.receive<HitPositionDTO>().unwrap { it -> it }
        val game = gameService.findGameById(hitPositionDTO.game.gameId)
        val player = gameService.getPlayerByID(game.gameId, hitPositionDTO.gamePlayer.gamePlayerName)
        val attacker = gameService.getPlayerByID(game.gameId, hitPositionDTO.attacker.gamePlayerName)
        val hit = hitPositionDTO.toEntity()
        hit.gamePlayer = player
        hit.game = game
        hit.attacker = attacker
        serviceHub.withEntityManager {
            if(hitPositionDTO.gamePlayer.playerStatus == PlayerStatus.SUNKEN) {
                gameService.sinkPlayerByID(hitPositionDTO.game.gameId, hitPositionDTO.gamePlayer.gamePlayerName)
            }
            if(hitPositionDTO.game.gameStatus == GameStatus.DONE) {
                gameService.setGameStatus(hitPositionDTO.game.gameId, GameStatus.DONE)
            }
            persist(hit)
        }
    }
}

@CordaSerializable
data class PositionHolder(val gameId: UUID, val attacker: String, val xCoord: Int, val yCoord: Int, val round: Int)

@StartableByRPC
@InitiatingFlow
class ReceiveAttackFlow(val gameId: UUID, val player: String, val attacker: String,
                        val xCoord: Int,
                        val yCoord: Int, val round: Int) : FlowLogic<HitPositionDTO>() {

    @Suspendable
    override fun call(): HitPositionDTO {
        val party = this.serviceHub.identityService.partiesFromName(player, true).single()
        val session = initiateFlow(party)
        val positionHolder = PositionHolder(gameId, attacker, xCoord, yCoord, round)
        return session.sendAndReceive(HitPositionDTO::class.java, positionHolder).unwrap { it -> it }
    }
}

@InitiatedBy(ReceiveAttackFlow::class)
class ReceiveAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<HitPositionDTO>() {
    @Suspendable
    override fun call(): HitPositionDTO {
        val positionHolder = counterpartySession.receive<PositionHolder>().unwrap { it -> it }
        val gameService = serviceHub.cordaService(GameService::class.java)
        var game = gameService.findGameById(positionHolder.gameId)
        var player = gameService.getPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
        val ship = gameService.getPlayerShip(positionHolder.gameId, ourIdentity.name.toString())
        val hitStatus: HitStatus
        hitStatus = if ((ship.fromX?.rangeTo(ship.toX!!)!!.contains(positionHolder.xCoord)) &&
                (ship.fromY?.rangeTo(ship.toY!!)!!.contains(positionHolder.yCoord))) {
            HitStatus.HIT
        } else
            HitStatus.MISS

        if (hitStatus == HitStatus.HIT) {
            var counter = 1
            val hits = gameService.getAllHitsForPlayerInGame(positionHolder.gameId, ourIdentity.name.toString())
            for (hit in hits) {
                if (hit.hitStatus == HitStatus.HIT)
                    counter++
            }
            if (counter == 3) {
                gameService.sinkPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
                player = gameService.getPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
                game = gameService.findGameById(positionHolder.gameId)
                if(game.gamePlayers.count { it.playerStatus == PlayerStatus.SUNKEN } >= game.gamePlayers.count() -1) {
                    gameService.setGameStatus(game.gameId, GameStatus.DONE)
                    game.gameStatus = GameStatus.DONE
                }
            }
        }
        val attacker = game.gamePlayers.single { it.gamePlayerName == positionHolder.attacker }
        val hitPosition = GameSchemaV1.HitPosition(player, attacker, game, positionHolder.xCoord, positionHolder.yCoord, hitStatus, positionHolder.round)
        val position = HitPositionDTO.fromEntity(hitPosition)
        counterpartySession.send(position)
        return position
    }
}