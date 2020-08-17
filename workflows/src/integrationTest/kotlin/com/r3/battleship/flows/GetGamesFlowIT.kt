package com.r3.battleship.flows

import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetworkParameters
import net.corda.testing.node.StartedMockNode
import net.corda.testing.node.TestCordapp
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetGamesFlowIT {

    lateinit var network: MockNetwork
    lateinit var player1: StartedMockNode
    lateinit var player2: StartedMockNode
    lateinit var player3: StartedMockNode
    lateinit var player4: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(
            MockNetworkParameters(
                networkParameters = testNetworkParameters(minimumPlatformVersion = 4),
                cordappsForAllNodes = listOf(
                    TestCordapp.findCordapp("com.r3.battleship.schemas"),
                    TestCordapp.findCordapp("com.r3.battleship.flows")
                )
            )
        )
        player1 = network.createPartyNode()
        player2 = network.createPartyNode()
        player3 = network.createPartyNode()
        player4 = network.createPartyNode()
    }


    @Test
    fun `test GetGamesFlow`() {
        player1.startFlow(CreateGameFlow())
        network.runNetwork()
        val flowFuture = player1.startFlow(GetGamesFlow())
        network.runNetwork()
        val gamesList = flowFuture.get()

        assertEquals(1, gamesList.size)
    }
}