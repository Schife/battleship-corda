package com.r3.battleship.flows

import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.GameDTO
import com.r3.battleship.schemas.PlayerStatus
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PlaceShipFlowIT {

    lateinit var network: MockNetwork
    lateinit var player1: StartedMockNode
    lateinit var player2: StartedMockNode
    lateinit var player3: StartedMockNode
    lateinit var player4: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4),
                cordappsForAllNodes = listOf(
                        TestCordapp.findCordapp("com.r3.battleship.schemas"),
                        TestCordapp.findCordapp("com.r3.battleship.flows")
                )))
        val player1Name = CordaX500Name("Player1", "Captain", "Dublin", "IE")
        val player2Name = CordaX500Name("Player2", "Captain", "Dublin", "IE")
        val player3Name = CordaX500Name("Player3", "Captain", "Dublin", "IE")
        val player4Name = CordaX500Name("Player4", "Captain", "Dublin", "IE")
        player1 = network.createPartyNode(player1Name)
        player2 = network.createPartyNode(player2Name)
        player3 = network.createPartyNode(player3Name)
        player4 = network.createPartyNode(player4Name)
    }


    @Test
    fun `test PlaceShipFlow happy path`() {
        val flowFuture = player1.startFlow(CreateGameFlow())
        network.runNetwork()
        var game = flowFuture.get()

        val joinGamePlayer2Future = player2.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer2Future.get()

        val joinGamePlayer3Future = player3.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer3Future.get()

        val joinGamePlayer4Future = player4.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer4Future.get()

        val startGameFuture = player1.startFlow(StartGameFlow(game.gameId))
        network.runNetwork()
        game = startGameFuture.get()

        val placeShipPlayer1Future = player1.startFlow(PlaceShipFlow(game.gameId, 1, 1, 3, 1))
        network.runNetwork()
        placeShipPlayer1Future.get()

        val placeShipPlayer2Future = player2.startFlow(PlaceShipFlow(game.gameId, 2, 2, 2, 4))
        network.runNetwork()
        placeShipPlayer2Future.get()

        val placeShipPlayer3Future = player3.startFlow(PlaceShipFlow(game.gameId, 3, 1, 3, 3))
        network.runNetwork()
        placeShipPlayer3Future.get()

        val placeShipPlayer4Future = player4.startFlow(PlaceShipFlow(game.gameId, 2, 3, 4, 3))
        network.runNetwork()
        placeShipPlayer4Future.get()

        validateAllPlayers(game)
    }

    @Test
    fun `test SendAttackFlow happy path`() {
        val flowFuture = player1.startFlow(CreateGameFlow())
        network.runNetwork()
        var game = flowFuture.get()

        val joinGamePlayer2Future = player2.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer2Future.get()

        val joinGamePlayer3Future = player3.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer3Future.get()

        val joinGamePlayer4Future = player4.startFlow(JoinGameFlow(game.gameId))
        network.runNetwork()
        game = joinGamePlayer4Future.get()

        val startGameFuture = player1.startFlow(StartGameFlow(game.gameId))
        network.runNetwork()
        game = startGameFuture.get()

        val placeShipPlayer1Future = player1.startFlow(PlaceShipFlow(game.gameId, 1, 1, 3, 1))
        network.runNetwork()
        placeShipPlayer1Future.get()

        val placeShipPlayer2Future = player2.startFlow(PlaceShipFlow(game.gameId, 2, 2, 2, 4))
        network.runNetwork()
        placeShipPlayer2Future.get()

        val placeShipPlayer3Future = player3.startFlow(PlaceShipFlow(game.gameId, 3, 1, 3, 3))
        network.runNetwork()
        placeShipPlayer3Future.get()

        val placeShipPlayer4Future = player4.startFlow(PlaceShipFlow(game.gameId, 2, 3, 4, 3))
        network.runNetwork()
        placeShipPlayer4Future.get()

        validateAllPlayers(game)

        player1.startFlow(SendAttackFlow(game.gameId,"Player4", 2, 3, 1))
        network.runNetwork()
        player2.startFlow(SendAttackFlow(game.gameId,"Player4", 3, 3, 1))
        network.runNetwork()
        player3.startFlow(SendAttackFlow(game.gameId,"Player4", 4, 3, 1))
        network.runNetwork()
        player4.startFlow(SendAttackFlow(game.gameId,"Player1", 1, 1, 1))
        network.runNetwork()

        val player4GameService = player4.services.cordaService(GameService::class.java)
        val player4DTO = player4GameService.getPlayerByID(game.gameId, player4.info.legalIdentities[0].toString())
        assertEquals(PlayerStatus.SUNKEN, player4DTO.playerStatus)

    }

    private fun validateAllPlayers(game: GameDTO) {
        val player1GameService = player1.services.cordaService(GameService::class.java)
        val player2GameService = player2.services.cordaService(GameService::class.java)
        val player3GameService = player3.services.cordaService(GameService::class.java)
        val player4GameService = player4.services.cordaService(GameService::class.java)
        val fetchedGamePlayer1 = player1GameService.findGameById(game.gameId)
        val fetchedGamePlayer2 = player2GameService.findGameById(game.gameId)
        val fetchedGamePlayer3 = player3GameService.findGameById(game.gameId)
        val fetchedGamePlayer4 = player4GameService.findGameById(game.gameId)

        val fetchedShipPositionsPlayer1 = player1GameService.getShipPositionsCountForGame(game.gameId)
        val fetchedShipPositionsPlayer2 = player2GameService.getShipPositionsCountForGame(game.gameId)
        val fetchedShipPositionsPlayer3 = player3GameService.getShipPositionsCountForGame(game.gameId)
        val fetchedShipPositionsPlayer4 = player4GameService.getShipPositionsCountForGame(game.gameId)

        assertEquals(fetchedGamePlayer1, fetchedGamePlayer2)
        assertEquals(fetchedGamePlayer2, fetchedGamePlayer3)
        assertEquals(fetchedGamePlayer3, fetchedGamePlayer4)

        assertEquals(fetchedShipPositionsPlayer1, fetchedShipPositionsPlayer2)
        assertEquals(fetchedShipPositionsPlayer2, fetchedShipPositionsPlayer3)
        assertEquals(fetchedShipPositionsPlayer3, fetchedShipPositionsPlayer4)
    }
}