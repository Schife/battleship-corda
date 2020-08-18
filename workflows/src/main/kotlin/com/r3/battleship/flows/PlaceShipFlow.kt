package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.*
import net.corda.core.crypto.DigitalSignature
import net.corda.core.flows.*
import net.corda.core.internal.signWithCert
import net.corda.core.utilities.parsePublicKeyBase58
import net.corda.core.utilities.unwrap
import java.util.*
import javax.persistence.Column
import javax.persistence.ManyToOne

@StartableByRPC
@InitiatingFlow
class PlaceShipFlow(val gameId: UUID, val fromX: Int, val fromY: Int, val toX: Int, val toY: Int) : FlowLogic<ShipPositionDTO>() {

    @Suspendable
    override fun call(): ShipPositionDTO {

        val gameService = serviceHub.cordaService(GameService::class.java)
        val game = gameService.findGameById(gameId)

        if (game.gameStatus != GameStatus.ACTIVE)
            throw FlowException("Game must be in active status in order to place ships")

        val gamePlayer = game.gamePlayers.single { it.gamePlayerName == ourIdentity.name.toString() }
        verifyPosition(gamePlayer.playerRegion, fromX, fromY, toX, toY)

        val shipPosition = GameSchemaV1.ShipPosition(gamePlayer, game, fromX, fromY, toX, toY)
        val signedPosition = serviceHub.keyManagementService.sign(shipPosition.toString().toByteArray(), ourIdentity.owningKey)
        shipPosition.signedPosition = String(signedPosition.bytes)

        val shipPositionDTO = serviceHub.withEntityManager {
            persist(shipPosition)
            ShipPositionDTO.fromEntity(shipPosition)
        }

        updateGameStatus(gameService, game)

        val sessions = serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { "GamePlayer" in it.name.organisation }
                .filter { !serviceHub.networkMapCache.isNotary(it.party) }
                .map { initiateFlow(it.party) }

        val transferPositionDTO = shipPositionDTO.copy(fromX = null, fromY = null, toX = null, toY = null)
        sessions.forEach { it.send(transferPositionDTO) }

        return shipPositionDTO
    }

    @Suspendable
    private fun verifyPosition(playerRegion: Int, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val exception = FlowException("Ship not placed in players region")
        verifyOrientation(exception, fromX, fromY, toX, toY)
        when (playerRegion) {
            1 -> if (fromX !in 1..5 || fromY !in 1..5 || toX !in 1..5 || toY !in 1..5) throw exception
            2 -> if (fromX !in 6..10 || fromY !in 1..5 || toX !in 6..10 || toY !in 1..5) throw exception
            3 -> if (fromX !in 1..5 || fromY !in 6..10 || toX !in 1..5 || toY !in 6..10) throw exception
            4 -> if (fromX !in 6..10 || fromY !in 6..10 || toX !in 6..10 || toY !in 6..10) throw exception
        }
    }

    @Suspendable
    private fun verifyOrientation(exception: FlowException, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        if (toX - fromX == 3 && fromY == toY)
            return
        if (toY - fromY == 3 && fromX == toX)
            return
        throw exception
    }

    @Suspendable
    private fun updateGameStatus(gameService: GameService, game: GameSchemaV1.Game) {
        val positionsPlaced = gameService.getShipPositionsCountForGame(gameId)
        if (positionsPlaced == game.gamePlayers.count()) {
            serviceHub.withEntityManager {
                gameService.setGameStatus(game.gameId, GameStatus.SHIPS_PLACED)
            }
        }
    }
}

@InitiatedBy(PlaceShipFlow::class)
class PlaceShipFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {

    @Suspendable
    override fun call() {
        val shipPositionDTO = counterpartySession.receive<ShipPositionDTO>().unwrap { it -> it }
        val gameService = serviceHub.cordaService(GameService::class.java)
        serviceHub.withEntityManager {
            val shipPosition = shipPositionDTO.toEntity()
            val game = gameService.findGameById(shipPosition.game!!.gameId)
            shipPosition.game = game
            shipPosition.gamePlayer = game.gamePlayers.single { it.gamePlayerName == shipPosition.gamePlayer!!.gamePlayerName }
            persist(shipPosition)
        }
        updateGameStatus(gameService, shipPositionDTO.game)
    }

    @Suspendable
    private fun updateGameStatus(gameService: GameService, gameDTO: GameDTO) {
        val positionsPlaced = gameService.getShipPositionsCountForGame(gameDTO.gameId)
        if (positionsPlaced == gameDTO.gamePlayers.count()) {
            serviceHub.withEntityManager {
                gameService.setGameStatus(gameDTO.gameId, GameStatus.SHIPS_PLACED)
            }
        }
    }
}