package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class ReceiveAttackFlow(val gameId: UUID, val xCoord: Int, val yCoord: Int) : FlowLogic<GameDTO>() {

    @Suspendable
    override fun call(): GameDTO {

        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)
        val player = gameService.getPlayerByID(gameId, ourIdentity.name.toString())
        val playerDTO = GamePlayersDTO.fromEntity(player)
        val gameDTO = GameDTO.fromEntity(game)
        val sessions = this.serviceHub.identityService.getAllIdentities()
            .filter { it.owningKey != ourIdentity.owningKey }
            .map { initiateFlow(it.party) }
        sessions.forEach { it.send(playerDTO) }
        return gameDTO
    }
}

@InitiatedBy(JoinGameFlow::class)
class ReceiveAttackFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val playerDTO = counterpartySession.receive<GamePlayersDTO>().unwrap{ it -> it }
        serviceHub.withEntityManager {
            persist(playerDTO.toEntity())
        }
    }
}