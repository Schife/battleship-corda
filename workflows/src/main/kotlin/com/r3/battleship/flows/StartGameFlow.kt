package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class StartGameFlow(val gameId: UUID) : FlowLogic<GameDTO>() {

    @Suspendable
    override fun call(): GameDTO {

        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)
        if( game.numberOfPlayers != game.gamePlayers.count()) {
            throw FlowException("All the seats for this game must be filled!")
        }
        val gameDTO = GameDTO.fromEntity(game)
        serviceHub.cordaService(GameService::class.java).startGameByID(gameDTO.gameId)
        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .map { initiateFlow(it.party) }
        sessions.forEach { it.send(gameDTO) }
        return gameDTO
    }
}

@InitiatedBy(StartGameFlow::class)
class StartGameFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val gameDTO = counterpartySession.receive<GameDTO>().unwrap{ it -> it }
        serviceHub.cordaService(GameService::class.java).startGameByID(gameDTO.gameId)
    }
}