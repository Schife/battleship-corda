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
        val hitDTO = subFlow(ReceiveAttackFlow(gameId, player, xCoord, yCoord, round))
        serviceHub.withEntityManager {
            persist(hitDTO)
        }

        val sessions = this.serviceHub.identityService.getAllIdentities()
            .filter { it.owningKey != ourIdentity.owningKey }
            .map { initiateFlow(it.party) }
        sessions.forEach { it.send(hitDTO) }
        return hitDTO
    }
}

@InitiatedBy(SendAttackFlow::class)
class SendAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val hitPositionDTO = counterpartySession.receive<HitPositionDTO>().unwrap { it -> it }
        serviceHub.withEntityManager {
            persist(hitPositionDTO.toEntity())
        }
    }
}

@CordaSerializable
data class PositionHolder(val gameId: UUID, val xCoord: Int, val yCoord: Int, val round: Int)

@StartableByRPC
@InitiatingFlow
class ReceiveAttackFlow(val gameId: UUID, val player: String, val xCoord: Int, val yCoord: Int, val round: Int) : FlowLogic<HitPositionDTO>() {

    @Suspendable
    override fun call(): HitPositionDTO {
        val party = this.serviceHub.identityService.partiesFromName(player, true).single()
        val session = initiateFlow(party)
        val positionHolder = PositionHolder(gameId, xCoord, yCoord, round)
        return session.sendAndReceive(HitPositionDTO::class.java, positionHolder).unwrap { it -> it }
    }
}

@InitiatedBy(ReceiveAttackFlow::class)
class ReceiveAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<HitPositionDTO>() {
    @Suspendable
    override fun call(): HitPositionDTO {
        val positionHolder = counterpartySession.receive<PositionHolder>().unwrap { it -> it }
        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(positionHolder.gameId)
        var player = gameService.getPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
        val ship = gameService.getPlayerShip(positionHolder.gameId, ourIdentity.name.toString())
        val hitStatus: HitStatus
        hitStatus =  if ((ship.fromX?.rangeTo(ship.toX!!)!!.contains(positionHolder.xCoord)) &&
            (ship.fromY?.rangeTo(ship.toY!!)!!.contains(positionHolder.yCoord))) {
            HitStatus.HIT
        } else
            HitStatus.MISS

        if (hitStatus == HitStatus.HIT) {
            var counter = 1
            val hits = gameService.getAllHitsForPlayerInGame(positionHolder.gameId, ourIdentity.name.toString())
            for(hit in hits) {
                if(hit.hitStatus == HitStatus.HIT)
                    counter ++
            }
            if(counter == 3){
                gameService.sinkPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
                player = gameService.getPlayerByID(positionHolder.gameId, ourIdentity.name.toString())
            }
        }

        val hitPosition = GameSchemaV1.HitPosition(player, game, positionHolder.xCoord, positionHolder.yCoord, hitStatus, positionHolder.round)

        return HitPositionDTO.fromEntity(hitPosition)
    }
}