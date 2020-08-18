package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class ReceiveAttackFlow(val gameId: UUID, val xCoord: Int, val yCoord: Int, val round: Int) : FlowLogic<HitPositionDTO>() {

    @Suspendable
    override fun call(): HitPositionDTO {

        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)
        var player = gameService.getPlayerByID(gameId, ourIdentity.name.toString())
        val ship = gameService.getPlayerShip(gameId, ourIdentity.name.toString())
        val hitStatus: HitStatus
        hitStatus = if ((ship.fromX?.rangeTo(ship.toX!!)!!.contains(xCoord)) &&
                (ship.fromY?.rangeTo(ship.toY!!)!!.contains(yCoord))) {
            HitStatus.HIT
        } else
            HitStatus.MISS

        if (hitStatus == HitStatus.HIT) {
            var counter = 1
            val hits = gameService.getAllHitsForPlayerInGame(gameId, ourIdentity.name.toString())
            for (hit in hits) {
                if (hit.hitStatus == HitStatus.HIT)
                    counter++
            }
            if (counter == 3) {
                gameService.sinkPlayerByID(gameId, ourIdentity.name.toString())
                player = gameService.getPlayerByID(gameId, ourIdentity.name.toString())
            }
        }

        val hitPosition = GameSchemaV1.HitPosition(player, game, xCoord, yCoord, hitStatus, round)
        val hitDTO = serviceHub.withEntityManager {
            persist(hitPosition)
            HitPositionDTO.fromEntity(hitPosition)
        }

        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { "Captain" in it.name.organisation }
                .map { initiateFlow(it.party) }
        sessions.forEach { it.send(hitDTO) }
        return hitDTO
    }
}

@InitiatedBy(ReceiveAttackFlow::class)
class ReceiveAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val hitPositionDTO = counterpartySession.receive<HitPositionDTO>().unwrap { it -> it }
        serviceHub.withEntityManager {
            persist(hitPositionDTO.toEntity())
        }
    }
}