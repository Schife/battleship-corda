package com.r3.battleship.repository

import com.r3.battleship.schemas.GameSchemaV1
import com.r3.battleship.schemas.GameStatus
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SerializeAsToken
import net.corda.core.serialization.SingletonSerializeAsToken
import java.util.*

@CordaService
class GameService(val serviceHub: ServiceHub) : SingletonSerializeAsToken() {

    fun findGameById(id: UUID): GameSchemaV1.Game {
        return serviceHub.withEntityManager {
            find(GameSchemaV1.Game::class.java, id)
        }
    }

    fun getAllGames(): List<GameSchemaV1.Game> {
        return serviceHub.withEntityManager {
            createQuery("select g from GameSchemaV1\$Game g where g.gameStatus = 'CREATED' OR " +
                    "g.gameStatus = 'ACTIVE'", GameSchemaV1.Game::class.java).resultList
        }
    }

    fun setGameStatus(id: UUID, gameStatus: GameStatus) {
        return serviceHub.withEntityManager {
            val query = createQuery("update GameSchemaV1\$Game g set g.gameStatus = :gameStatus where " +
                    "g.gameId = :gameId")
            query.setParameter("gameStatus", gameStatus)
            query.setParameter("gameId", id)
            query.executeUpdate()
        }
    }

    fun getShipPositionsCountForGame(id: UUID): Int {
        return serviceHub.withEntityManager {
            val query = createQuery("select count(*) from GameSchemaV1\$ShipPosition sp where " +
                    "sp.game.gameId = :gameId", java.lang.Long::class.java)
            query.setParameter("gameId", id)
            query.singleResult.toInt()
        }
    }

    fun getAllHitsForGame(id: UUID): List<GameSchemaV1.HitPosition> {
        return serviceHub.withEntityManager {
            val query = createQuery("select hp from GameSchemaV1\$HitPosition hp where hp.game.gameId = :gameId ORDER BY hp.createdOn DESC", GameSchemaV1.HitPosition::class.java)
            query.setParameter("gameId", id)
            query.resultList
        }
    }

    fun getAllHitsForGameAndRound(id: UUID, roundNum: Int): List<GameSchemaV1.HitPosition> {
        return serviceHub.withEntityManager {
            val query = createQuery("select hp from GameSchemaV1\$HitPosition hp where hp.game.gameId = :gameId " +
                    "and hp.roundNum = :roundNum",
                    GameSchemaV1.HitPosition::class.java)
            query.setParameter("gameId", id)
            query.setParameter("roundNum", roundNum)
            query.resultList
        }
    }

    fun getAllHitsForPlayerInGame(id: UUID, player: String): List<GameSchemaV1.HitPosition> {
        return serviceHub.withEntityManager {
            val query = createQuery("select hp from GameSchemaV1\$HitPosition hp where hp.game.gameId = :gameId " +
                    "and hp.gamePlayer.gamePlayerName = :player",
                GameSchemaV1.HitPosition::class.java)
            query.setParameter("gameId", id)
            query.setParameter("player", player)
            query.resultList
        }
    }

    fun getPlayerByID(id: UUID, player: String) : GameSchemaV1.GamePlayers {
        return serviceHub.withEntityManager {
            createQuery("select p from GameSchemaV1\$GamePlayers p where p.game = '$id' " +
                    "and p.gamePlayerName = '$player'", GameSchemaV1.GamePlayers::class.java).singleResult
        }
    }

    fun getPlayerShip(gameId: UUID, player: String): GameSchemaV1.ShipPosition {
        return serviceHub.withEntityManager {
            val query = createQuery("select sp from GameSchemaV1\$ShipPosition sp where sp.game.gameId = :gameId " +
                    "and sp.gamePlayer.gamePlayerName = :player", GameSchemaV1.ShipPosition::class.java)
            query.setParameter("gameId", gameId)
            query.setParameter("player", player)
            query.singleResult
        }
    }

    fun sinkPlayerByID(id: UUID, player: String) {
        return serviceHub.withEntityManager {
            createQuery("update GameSchemaV1\$GamePlayers p set p.playerStatus = 'SUNKEN' where p.game = '$id' " +
                    "and p.gamePlayerName = '$player'").executeUpdate()
        }
    }

}