package com.r3.battleship.flows

import com.r3.battleship.schemas.GameSchemaV1
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.*

@StartableByRPC
@InitiatingFlow
class PlaceShipFlow(gameId: UUID, fromX: Int, fromY: Int, toX: Int, toY: Int) : FlowLogic<GameSchemaV1.Game>() {
    override fun call(): GameSchemaV1.Game {
        TODO("Not yet implemented")
    }
}