package com.r3.battleship.flows

import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.GameDTO
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CreateGameFlowIT {

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
        player1 = network.createPartyNode()
        player2 = network.createPartyNode()
        player3 = network.createPartyNode()
        player4 = network.createPartyNode()
    }


    @Test
    fun `test CreateGameFlow happy path`() {
        val flowFuture = player1.startFlow(CreateGameFlow())
        network.runNetwork()
        val game = flowFuture.get()

        validateAllPlayers(game)
    }

    @Test
    fun `test JoinGameFlow happy path`() {
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

        validateAllPlayers(game)
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

        assertEquals(fetchedGamePlayer1, fetchedGamePlayer2)
        assertEquals(fetchedGamePlayer2, fetchedGamePlayer3)
        assertEquals(fetchedGamePlayer3, fetchedGamePlayer4)
    }
}