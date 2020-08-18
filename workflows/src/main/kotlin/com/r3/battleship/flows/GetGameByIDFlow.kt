package com.r3.battleship.flows

import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.GameDTO
import net.corda.core.flows.*
import java.util.*

@StartableByRPC
@InitiatingFlow
class GetGameByIDFlow(val gameId: UUID) : FlowLogic<GameDTO>() {

    override fun call(): GameDTO {
        val service = GameService(serviceHub)
        val game = service.findGameById(gameId)
        return GameDTO.fromEntity(game)
    }
}