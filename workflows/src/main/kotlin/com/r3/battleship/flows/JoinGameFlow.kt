package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.util.*

@StartableByRPC
@InitiatingFlow
class JoinGameFlow(val gameId: UUID) : FlowLogic<GameDTO>() {

    @Suspendable
    override fun call(): GameDTO {

        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)
        if( game.numberOfPlayers <= game.gamePlayers.count()) {
            throw FlowException("All the seats for this game has taken, better luck next time!")
        }
        val player = GameSchemaV1.GamePlayers(ourIdentity.name.toString(), PlayerStatus.ACTIVE, game.gameId)
        val playerDTO = serviceHub.withEntityManager {
            persist(player)
            GamePlayersDTO.fromEntity(player)
        }
        val gameDTO = GameDTO.fromEntity(gameService.findGameById(gameId))
        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .map { initiateFlow(it.party) }
        sessions.forEach { it.send(playerDTO) }
        return gameDTO
    }
}

@InitiatedBy(JoinGameFlow::class)
class JoinGameFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val playerDTO = counterpartySession.receive<GamePlayersDTO>().unwrap{ it -> it }
        serviceHub.withEntityManager {
            persist(playerDTO.toEntity())
        }
    }
}