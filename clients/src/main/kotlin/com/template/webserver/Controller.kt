package com.template.webserver


import com.r3.battleship.flows.*
import com.r3.battleship.schemas.GameDTO
import com.r3.battleship.schemas.HitPositionDTO
import com.r3.battleship.schemas.ShipPositionDTO
import net.corda.core.flows.FlowException
import net.corda.core.messaging.startFlow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import webserver.*
import java.lang.Exception
import java.util.*
import javax.persistence.NoResultException
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/battleship") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)

        private const val GAME_SIZE_PLAYERS = 2
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/games"], produces = ["application/json"])
    private fun games(): ResponseEntity<List<Game>> {
        val ourIdentity = proxy.nodeInfo().legalIdentities.first().name.toString()
        var gameDTOList: List<GameDTO> = proxy.startFlow(::GetGamesFlow).returnValue.get()
        var games = ArrayList<Game>()
        gameDTOList.forEach { gameDTO -> games.add(DTOModelHelper.toGame(gameDTO, ourIdentity)) }
        return ResponseEntity<List<Game>>(games, HttpStatus.OK);
    }

    @PostMapping(value = ["/createGame"], produces = ["application/json"])
    private fun createGame(): ResponseEntity<Game> {
        val ourIdentity = proxy.nodeInfo().legalIdentities.first().name.toString()
        var gameGto: GameDTO = proxy.startFlow(::CreateGameFlow, GAME_SIZE_PLAYERS).returnValue.get()
        var newGame = DTOModelHelper.toGame(gameGto, ourIdentity)
        return ResponseEntity<Game>(newGame, HttpStatus.OK)
    }

    @PostMapping(value = ["{gameId}/joinGame"], produces = ["application/json"])
    private fun joinGame(@PathVariable gameId:String): ResponseEntity<Game> {
        val ourIdentity = proxy.nodeInfo().legalIdentities.first().name.toString()
        val gameDTO = proxy.startFlow(::JoinGameFlow, UUID.fromString(gameId)).returnValue.get()
        val game = DTOModelHelper.toGame(gameDTO, ourIdentity)
        return ResponseEntity<Game>(game, HttpStatus.OK)
    }

    @PostMapping(value = ["/{gameId}/startGame"], produces = ["application/json"])
    private fun startGame(@PathVariable gameId:String): ResponseEntity<Game> {
        val ourIdentity = proxy.nodeInfo().legalIdentities.first().name.toString()
        val gameDTO = proxy.startFlow(::StartGameFlow, UUID.fromString(gameId)).returnValue.get()
        val game = DTOModelHelper.toGame(gameDTO, ourIdentity)
        return ResponseEntity<Game>(game, HttpStatus.OK)
    }

    @PostMapping(value = ["/{gameId}/placeShip"], produces = ["application/json"])
    private fun placeShip(@PathVariable gameId:String, @RequestBody placement: Placement): ResponseEntity<String> {
        val shipPositionDTO: ShipPositionDTO = proxy.startFlow(::PlaceShipFlow, UUID.fromString(gameId), placement.start.x, placement.start.y, placement.end.x, placement.end.y).returnValue.get()
        return ResponseEntity("placed", HttpStatus.OK);
    }

    @PostMapping(value = ["/{gameId}/attack"], produces = ["application/json"])
    private fun placeShip(@PathVariable gameId:String, @RequestBody attackRequest: AttackRequest): ResponseEntity<String> {
        // TODO: wire it up to SendAttackFlow
        return ResponseEntity("placed", HttpStatus.OK);
    }

    @GetMapping(value = ["/{gameId}/gameState"], produces = ["application/json"])
    private fun getGameState(@PathVariable gameId:String): ResponseEntity<GameState> {
        var placement = Placement(Coordinate(3,2), Coordinate(3,5))
        var identity = proxy.nodeInfo().legalIdentities.first().name.toString()

        var gameState: GameState

        if (gameId == "1") {
            // initial game state before placing ships
            gameState = GameState(null, identity, true, GameStatus.ACTIVE, createPlayerStateList(identity), HashMap<String, HashMap<Coordinate, String>>(), null, emptyMap(),1 )
        } else if (gameId == "2") {
            // game state after placing ships
            gameState = GameState(placement, identity, true, GameStatus.SHIPS_PLACED, createPlayerStateList(identity), createShotList(), null, emptyMap(), 1)
        } else if (gameId == "3") {
            // game state after game finished and we won
            gameState = GameState(placement, identity, true, GameStatus.DONE, createPlayerStateList(identity), createShotList(), identity, createShipLocations(), 1)
        } else if (gameId == "4") {
            // game state after game finished and someone else won
            gameState = GameState(placement, identity, true, GameStatus.DONE, createPlayerStateList(identity), createShotList(), "player2", createShipLocations(), 1)
        } else {
            val hitPositionDTOList : List<HitPositionDTO> = proxy.startFlow(::GetGameHitsFlow, UUID.fromString(gameId)).returnValue.get()
            gameState = DTOModelHelper.toGameState(hitPositionDTOList, identity);

            if (hitPositionDTOList.isEmpty()) {
                var gameDTO : GameDTO = proxy.startFlow(::GetGameByIDFlow, UUID.fromString(gameId)).returnValue.get()
                var gamePlayerStates = DTOModelHelper.getPlayerStates(gameDTO?.gamePlayers);
                gameState.playerState = gamePlayerStates
                gameState.status = GameStatus.valueOf(gameDTO?.gameStatus.toString())
            }

            val shipPositionDTO: ShipPositionDTO? = proxy.startFlow(::GetMyShipsPositionFlow, UUID.fromString(gameId)).returnValue.get();
            if (shipPositionDTO == null) {
                gameState.placement = null
            } else {
                gameState.placement = DTOModelHelper.toPlacement(shipPositionDTO)
            }

            if (gameState.placement == null && hitPositionDTOList.isEmpty()) {
                gameState.isMyTurn = true
                gameState.status = GameStatus.ACTIVE
            }

            if (gameState.status == GameStatus.DONE) {
                //val playersShipLocationsDTO: Map<String,ShipPositionDTO>? = proxy.startFlow(::GetMyShipsPositionFlow, UUID.fromString(gameId)).returnValue.get();
                //gameState.playersShipLocations = DTOModelHelper.toPlayersShipLocations()
            }
            gameState.isMyTurn = true
        }

        return ResponseEntity(gameState, HttpStatus.OK);
    }

    private fun createPlayerStateList(ourPlayer: String): HashMap<String, Boolean> {
        val map = HashMap<String, Boolean>()
        map.put(ourPlayer, true)
        map.put("player2", true)
        map.put("player3", true)
        map.put("player4", true)

        return map;
    }

    private fun createShotList(): HashMap<String, HashMap<Coordinate, String>> {
        val c = Coordinate(3,4)
        val c2 = Coordinate(3,5)
        val c3 = Coordinate(2,2)
        val c4 = Coordinate(3,4)

        val map = HashMap<Coordinate, String>()
        map.put(c, "HIT")
        map.put(c2, "HIT")
        map.put(c3, "MISS")
        map.put(c4, "MISS")

        val playerMap = HashMap<String, HashMap<Coordinate, String>>()
        playerMap.put("O=PartyA, L=London, C=GB", map)
        playerMap.put("player2", map)

        return playerMap;
    }

    private fun createShipLocations(): Map<String, Placement> {
        return mapOf(
                "player2" to Placement(Coordinate(3,3), Coordinate(3,5)),
                "player3" to Placement(Coordinate(1,1), Coordinate(1,3)),
                "player4" to Placement(Coordinate(2,2), Coordinate(4,2))
        )
    }
}


