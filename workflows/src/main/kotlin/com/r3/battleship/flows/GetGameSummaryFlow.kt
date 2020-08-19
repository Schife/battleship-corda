package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.GamePlayersDTO
import com.r3.battleship.schemas.GameStatus
import com.r3.battleship.schemas.PlayerStatus
import com.r3.battleship.schemas.ShipPositionDTO
import net.corda.core.flows.*
import net.corda.core.serialization.CordaSerializable
import net.corda.core.utilities.unwrap
import java.util.*

@CordaSerializable
data class GameSummaryDTO(val winner: GamePlayersDTO?, val positionsMap: Map<GamePlayersDTO, ShipPositionDTO>)

@StartableByRPC
@InitiatingFlow
class GetGameSummaryFlow(val gameId: UUID) : FlowLogic<GameSummaryDTO>() {

    @Suspendable
    override fun call(): GameSummaryDTO {
        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)
        if (game.gameStatus != GameStatus.DONE) {
            throw FlowException("Cannot get summary for not DONE game")
        }
        var winner : GamePlayersDTO? = null
        if(game.gamePlayers.filter { it.playerStatus == PlayerStatus.ACTIVE }.count() == 1) {
            winner = GamePlayersDTO.fromEntity(game.gamePlayers.single { it.playerStatus == PlayerStatus.ACTIVE })
        }
        val positionsList = mutableListOf<ShipPositionDTO>()

        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { "Captain" in it.name.organisation }
                .filter { !serviceHub.networkMapCache.isNotary(it.party) }
                .map { initiateFlow(it.party) }
        positionsList.addAll(sessions.map { it.sendAndReceive(ShipPositionDTO::class.java, gameId)
                .unwrap { it -> it } })

        return GameSummaryDTO(winner, positionsList.map { it.gamePlayer to it }.toMap())
    }

}

@InitiatedBy(GetGameSummaryFlow::class)
class GetGameSummaryFlowResponder(val counterpartySession: FlowSession) : FlowLogic<ShipPositionDTO>() {
    @Suspendable
    override fun call() : ShipPositionDTO {
        val gameService = serviceHub.cordaService(GameService::class.java)
        val gameId = counterpartySession.receive<UUID>().unwrap { it -> it }
        val playerShipPosition = gameService.getPlayerShip(gameId, ourIdentity.name.toString())
        counterpartySession.send(ShipPositionDTO.fromEntity(playerShipPosition))
        return  ShipPositionDTO.fromEntity(playerShipPosition)
    }
}