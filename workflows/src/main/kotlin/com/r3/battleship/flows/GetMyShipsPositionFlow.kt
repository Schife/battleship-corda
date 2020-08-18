package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.ShipPositionDTO
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import java.util.*
import javax.persistence.NoResultException

@StartableByRPC
@InitiatingFlow
class GetMyShipsPositionFlow(val gameId: UUID) : FlowLogic<ShipPositionDTO?>() {

    @Suspendable
    override fun call(): ShipPositionDTO? {
        val gameService = serviceHub.cordaService(GameService::class.java)
        return try {
            val shipPosition = gameService.getPlayerShip(gameId, ourIdentity.name.toString())
            ShipPositionDTO.fromEntity(shipPosition)
        } catch (e: NoResultException) {
            null
        }
    }


}