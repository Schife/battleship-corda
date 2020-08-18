package com.r3.battleship.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.battleship.schemas.GameDTO
import com.r3.battleship.schemas.GameSchemaV1
import com.r3.battleship.schemas.GameStatus
import com.r3.battleship.schemas.PlayerStatus
import com.template.flows.Initiator
import net.corda.core.flows.*
import net.corda.core.utilities.unwrap
import java.time.Instant
import java.util.*

@StartableByRPC
@InitiatingFlow
class CreateGameFlow(val numberOfPlayers: Int = 4) : FlowLogic<GameDTO>() {

    @Suspendable
    override fun call(): GameDTO {

        val gameId = UUID.randomUUID()
        val player = GameSchemaV1.GamePlayers(ourIdentity.name.toString(), PlayerStatus.ACTIVE, 1, gameId)
        val game = GameSchemaV1.Game(gameId, Instant.now(), ourIdentity.name.toString(), GameStatus.CREATED, numberOfPlayers, listOf(player))
        val gameDTO =  serviceHub.withEntityManager {
            persist(game)
            GameDTO.fromEntity(game)
        }
        val sessions = this.serviceHub.identityService.getAllIdentities()
                .filter { it.owningKey != ourIdentity.owningKey }
                .filter { !serviceHub.networkMapCache.isNotary(it.party) }
                .map { initiateFlow(it.party) }
        sessions.forEach { it.send(gameDTO) }
        return gameDTO
    }
}

@InitiatedBy(CreateGameFlow::class)
class CreateGameFlowResponder(val counterpartySession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val gameDTO = counterpartySession.receive<GameDTO>().unwrap{ it -> it }
        serviceHub.withEntityManager {
            persist(gameDTO.toEntity())
        }
    }
}