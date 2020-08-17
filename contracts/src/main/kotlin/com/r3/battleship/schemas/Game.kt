package com.r3.battleship.schemas

import net.corda.core.schemas.MappedSchema
import net.corda.core.serialization.CordaSerializable
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.*

object GameSchema

@CordaSerializable
enum class GameStatus {
    CREATED,
    ACTIVE,
    SHIPS_PLACED,
    DONE
}

@CordaSerializable
enum class PlayerStatus {
    ACTIVE,
    SUNKEN
}

@CordaSerializable
data class GameDTO(val gameId: UUID, val createdOn: Instant, val createdBy: String,
                   val gameStatus: GameStatus, val numberOfPlayers: Int, val gamePlayers: List<GamePlayersDTO>) {

    companion object {
        fun fromEntity(entity: GameSchemaV1.Game) =
                GameDTO(entity.gameId, entity.createdOn, entity.createdBy, entity.gameStatus, entity.numberOfPlayers,
                        entity.gamePlayers.map { GamePlayersDTO.fromEntity(it)} )

    }

    fun toEntity() =
            GameSchemaV1.Game(this.gameId, this.createdOn, this.createdBy, this.gameStatus, this.numberOfPlayers,
                    this.gamePlayers.map { it.toEntity()} )

}

@CordaSerializable
data class GamePlayersDTO(val gamePlayerName: String, val playerStatus: PlayerStatus, val game: UUID) {
    companion object {
        fun fromEntity(entity: GameSchemaV1.GamePlayers) =
                GamePlayersDTO(entity.gamePlayerName, entity.playerStatus, entity.game)
    }

    fun toEntity() =
            GameSchemaV1.GamePlayers(this.gamePlayerName, this.playerStatus, this.game)
}


class GameSchemaV1 : MappedSchema(
        schemaFamily = GameSchema::class.java,
        version = 1,
        mappedTypes = listOf(Game::class.java, GamePlayers::class.java)) {

    @Entity
    @Table(name = "game")
    class Game(

            @Id
            @Column(name = "game_id", nullable = false)
            @Type(type = "uuid-char")
            var gameId: UUID = UUID.randomUUID(),

            @Column(name = "created_on", nullable = false)
            var createdOn: Instant = Instant.now(),

            @Column(name = "created_by", nullable = false)
            var createdBy: String = "",

            @Enumerated(EnumType.STRING)
            @Column(name = "game_status", nullable = false)
            var gameStatus: GameStatus = GameStatus.CREATED,

            @Column(name = "number_of_players", nullable = false)
            var numberOfPlayers: Int = 4,

            @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.EAGER, mappedBy = "game")
            var gamePlayers: List<GamePlayers> = emptyList()

    ) : Serializable {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Game) return false

            if (createdOn != other.createdOn) return false
            if (createdBy != other.createdBy) return false
            if (gameStatus != other.gameStatus) return false
            if (numberOfPlayers != other.numberOfPlayers) return false
            if (gameId != other.gameId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = createdOn.hashCode()
            result = 31 * result + createdBy.hashCode()
            result = 31 * result + gameStatus.hashCode()
            result = 31 * result + numberOfPlayers
            result = 31 * result + gameId.hashCode()
            return result
        }

    }

    @Entity
    @Table(name = "game_players")
    class GamePlayers(

            @Column(name = "player")
            var gamePlayerName: String = "",

            @Column(name = "player_status")
            var playerStatus: PlayerStatus = PlayerStatus.ACTIVE,

            @Column(name = "game_fk")
            @Type(type = "uuid-char")
            var game: UUID = UUID.randomUUID()

    ) : Serializable {

        @Id
        @GeneratedValue
        @Column(name = "game_player_id")
        var id: Long = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GamePlayers) return false

            if (gamePlayerName != other.gamePlayerName) return false
            if (playerStatus != other.playerStatus) return false
            if (game != other.game) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gamePlayerName.hashCode()
            result = 31 * result + playerStatus.hashCode()
            result = 31 * result + game.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }
    }
}