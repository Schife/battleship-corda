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
enum class HitStatus {
    HIT,
    MISS
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
data class GamePlayersDTO(val gamePlayerName: String, val playerStatus: PlayerStatus, val playerRegion: Int, val game: UUID) {
    companion object {
        fun fromEntity(entity: GameSchemaV1.GamePlayers) =
                GamePlayersDTO(entity.gamePlayerName, entity.playerStatus, entity.playerRegion, entity.game)
    }

    fun toEntity() =
            GameSchemaV1.GamePlayers(this.gamePlayerName, this.playerStatus, this.playerRegion, this.game)
}

@CordaSerializable
data class ShipPositionDTO(val id: Long, val gamePlayer: GamePlayersDTO, val game: GameDTO, val fromX: Int?, val fromY: Int?,
                           val toX: Int?, val toY: Int?, val signedPosition: String) {

    companion object {
        fun fromEntity(entity: GameSchemaV1.ShipPosition) =
                ShipPositionDTO(entity.id, GamePlayersDTO.fromEntity(entity.gamePlayer!!),
                        GameDTO.fromEntity(entity.game!!), entity.fromX, entity.fromY, entity.toX, entity.toY,
                        entity.signedPosition)
    }

    fun toEntity() : GameSchemaV1.ShipPosition {
        return GameSchemaV1.ShipPosition(this.gamePlayer.toEntity(), this.game.toEntity(),
                this.fromX, this.fromY, this.toX, this.toY, this.signedPosition)
    }

}

@CordaSerializable
data class HitPositionDTO(val gamePlayer: GamePlayersDTO, val game: GameDTO, val hitX: Int, val hitY: Int,
                          val hitStatus: HitStatus, val roundNum: Int, val id: UUID) {

    companion object {
        fun fromEntity(entity: GameSchemaV1.HitPosition) =
                HitPositionDTO(GamePlayersDTO.fromEntity(entity.gamePlayer!!),
                        GameDTO.fromEntity(entity.game!!), entity.hitX!!, entity.hitY!!, entity.hitStatus,
                        entity.roundNum!!, entity.id)
    }

    fun toEntity() : GameSchemaV1.HitPosition {
        return GameSchemaV1.HitPosition(this.gamePlayer.toEntity(), this.game.toEntity(),
                this.hitX, this.hitY, this.hitStatus, this.roundNum, this.id)
    }

}


class GameSchemaV1 : MappedSchema(
        schemaFamily = GameSchema::class.java,
        version = 1,
        mappedTypes = listOf(Game::class.java, GamePlayers::class.java,
                ShipPosition::class.java, HitPosition::class.java)) {

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
            var gamePlayers: List<GamePlayers> = mutableListOf()

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
            @Enumerated(EnumType.STRING)
            var playerStatus: PlayerStatus = PlayerStatus.ACTIVE,

            @Column(name = "player_region")
            var playerRegion: Int = 0,

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
            if (playerRegion != other.playerRegion) return false
            if (game != other.game) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gamePlayerName.hashCode()
            result = 31 * result + playerStatus.hashCode()
            result = 31 * result + playerRegion.hashCode()
            result = 31 * result + game.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }
    }

    @Entity
    @Table(name = "ship_position")
    class ShipPosition(

            @ManyToOne
            var gamePlayer: GamePlayers? = null,

            @ManyToOne
            var game: Game? = null,

            @Column(name = "from_x", nullable = true)
            var fromX: Int? = null,

            @Column(name = "from_y", nullable = true)
            var fromY: Int? = null,

            @Column(name = "to_x", nullable = true)
            var toX: Int? = null,

            @Column(name = "to_y", nullable = true)
            var toY: Int? = null,

            @Column(name = "signed_position")
            var signedPosition: String = ""

    ) : Serializable {

        @Id
        @GeneratedValue
        @Column(name = "ship_position_id")
        var id: Long = 0

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ShipPosition) return false

            if (gamePlayer != other.gamePlayer) return false
            if (game != other.game) return false
            if (fromX != other.fromX) return false
            if (fromY != other.fromY) return false
            if (toX != other.toX) return false
            if (toY != other.toY) return false
            if (signedPosition != other.signedPosition) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gamePlayer?.hashCode() ?: 0
            result = 31 * result + (game?.hashCode() ?: 0)
            result = 31 * result + (fromX ?: 0)
            result = 31 * result + (fromY ?: 0)
            result = 31 * result + (toX ?: 0)
            result = 31 * result + (toY ?: 0)
            result = 31 * result + signedPosition.hashCode()
            result = 31 * result + id.hashCode()
            return result
        }


    }

    @Entity
    @Table(name = "hit_position")
    class HitPosition(

            @ManyToOne
            var gamePlayer: GamePlayers? = null,

            @ManyToOne
            var game: Game? = null,

            @Column(name = "hit_x", nullable = true)
            var hitX: Int? = null,

            @Column(name = "hit_y", nullable = true)
            var hitY: Int? = null,

            @Column(name = "hit_status")
            var hitStatus: HitStatus = HitStatus.MISS,

            @Column(name = "roundNum")
            var roundNum: Int? = null,

            @Id
            @Column(name = "hit_id")
            var id: UUID = UUID.randomUUID()

    ) : Serializable {

        var createdOn: Instant = Instant.now()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is HitPosition) return false

            if (gamePlayer != other.gamePlayer) return false
            if (game != other.game) return false
            if (hitX != other.hitX) return false
            if (hitY != other.hitY) return false
            if (hitStatus != other.hitStatus) return false
            if (roundNum != other.roundNum) return false
            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            var result = gamePlayer?.hashCode() ?: 0
            result = 31 * result + (game?.hashCode() ?: 0)
            result = 31 * result + (hitX ?: 0)
            result = 31 * result + (hitY ?: 0)
            result = 31 * result + hitStatus.hashCode()
            result = 31 * result + (roundNum ?: 0)
            result = 31 * result + id.hashCode()
            return result
        }

    }
}