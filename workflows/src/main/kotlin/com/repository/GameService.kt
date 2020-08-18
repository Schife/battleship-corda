package com.r3.battleship.repository

import com.r3.battleship.schemas.GameSchemaV1
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SerializeAsToken
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.*

@CordaService
class GameService(val serviceHub: ServiceHub) : SingletonSerializeAsToken() {

    fun findGameById(id: UUID) : GameSchemaV1.Game {
        return serviceHub.withEntityManager {
            find(GameSchemaV1.Game::class.java, id)
        }
    }

    fun getAllGames() : List<GameSchemaV1.Game> {
        return serviceHub.withEntityManager {
            createQuery("select g from GameSchemaV1\$Game g where g.gameStatus = 'CREATED' OR g.gameStatus = 'ACTIVE'", GameSchemaV1.Game::class.java).resultList
        }
    }

    fun startGameByID(id: UUID) {
        return serviceHub.withEntityManager {
            createQuery("update GameSchemaV1\$Game g set g.gameStatus = 'ACTIVE' where g.gameId = '$id'").executeUpdate()
        }
    }

}