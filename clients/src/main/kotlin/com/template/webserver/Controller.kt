package com.template.webserver


import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import webserver.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    // TODO: store games in the node DB and retrieve them from there.
    private val games = mutableMapOf<String, Game>()

    @GetMapping(value = ["/games"], produces = ["application/json"])
    private fun games(): ResponseEntity<List<Game>> {
        return ResponseEntity<List<Game>>(games.values.toList(), HttpStatus.OK);
    }

    @PostMapping(value = ["/createGame"], produces = ["application/json"])
    private fun createGame(): ResponseEntity<Game> {
        val gameID = UUID.randomUUID().toString()
        val players = listOf("player-1", "player-2", "player-3")
        val sampleGame = Game(gameID,  players, true, false, GameStatus.UNSTARTED)
        games[gameID] = sampleGame
        return ResponseEntity<Game>(sampleGame, HttpStatus.OK)
    }

    @PostMapping(value = ["/joinGame"], produces = ["application/json"])
    private fun joinGame(@RequestBody request: JoinGameRequest): ResponseEntity<Game> {
        val ourIdentity = proxy.nodeInfo().legalIdentities.first().name.toString()
        val game = games[request.id]!!
        game.players = game.players + ourIdentity
        game.isJoinable = false
        if (game.players.size == 4) {
            game.isStartable = true
        }
        return ResponseEntity<Game>(game, HttpStatus.OK)
    }

    @PostMapping(value = ["/startGame"], produces = ["application/json"])
    private fun startGame(@RequestBody request: StartGameRequest): ResponseEntity<Game> {
        val game = games[request.id]!!
        game.status = GameStatus.STARTED
        return ResponseEntity<Game>(game, HttpStatus.OK)
    }

    @PostMapping(value = ["/placeShip"], produces = ["application/json"])
    private fun placeShip(@RequestBody placement: Placement): ResponseEntity<String> {
        return ResponseEntity("placed", HttpStatus.OK);
    }

    @GetMapping(value = ["/gameState/{gameId}"], produces = ["application/json"])
    private fun getGameState(@PathVariable gameId:String): ResponseEntity<GameState> {
        var placement = Placement(Coordinate("3","2"), Coordinate("3","5"))
        var gameState = GameState(placement, true, createPlayerStateList(), createShotList())

        return ResponseEntity(gameState, HttpStatus.OK);
    }

    private fun createPlayerStateList(): HashMap<String, Boolean> {
        val map = HashMap<String, Boolean>()
        map.put("player1", true)
        map.put("player2", true)
        map.put("player3", true)
        map.put("player4", true)

        return map;
    }

    private fun createShotList(): HashMap<Coordinate, String> {
        val c = Coordinate("3","4")
        val c2 = Coordinate("3","5")
        val c3 = Coordinate("2","2")
        val c4 = Coordinate("3","4")
        val c5 = Coordinate("1","7")
        val c6 = Coordinate("2","7")
        val c7 = Coordinate("7","3")
        val c8 = Coordinate("8","8")
        val map = HashMap<Coordinate, String>()
        map.put(c, "HIT")
        map.put(c2, "HIT")
        map.put(c3, "MISS")
        map.put(c4, "MISS")
        map.put(c5, "HIT")
        map.put(c6, "HIT")
        map.put(c7, "MISS")
        map.put(c8, "MISS")

        return map;
    }
}


