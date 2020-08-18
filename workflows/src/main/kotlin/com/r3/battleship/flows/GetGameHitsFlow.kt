package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.HitPositionDTO
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.*

@StartableByRPC
@InitiatingFlow
class GetGameHitsFlow(val gameId: UUID) : FlowLogic<List<HitPositionDTO>>() {

    @Suspendable
    override fun call(): List<HitPositionDTO> {
        val gameService = serviceHub.cordaService(GameService::class.java)
        return gameService.getAllHitsForGame(gameId).map { HitPositionDTO.fromEntity(it) }
    }

}