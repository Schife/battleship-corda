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
        val player = GameSchemaV1.GamePlayers(ourIdentity.name.toString(), PlayerStatus.ACTIVE,
                game.gamePlayers.count() + 1, game.gameId)
        val playerDTO = serviceHub.withEntityManager {
            persist(player)
            GamePlayersDTO.fromEntity(player)
        }
        val gamePlayersDTO = mutableListOf(playerDTO)
        gamePlayersDTO.addAll(game.gamePlayers.map { GamePlayersDTO.fromEntity(it) })
        val gameDTO = GameDTO.fromEntity(game).copy(
                gamePlayers = gamePlayersDTO)
        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { !serviceHub.networkMapCache.isNotary(it.party) }
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