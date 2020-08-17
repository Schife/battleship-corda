package com.template.webserver


import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import webserver.Coordinate
import webserver.Game
import webserver.Placement
import java.util.*
import kotlin.collections.ArrayList

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
    private val games = mutableListOf<Game>()

    @GetMapping(value = ["/games"], produces = ["application/json"])
    private fun games(): ResponseEntity<List<Game>> {
        return ResponseEntity<List<Game>>(games, HttpStatus.OK);
    }

    @PostMapping(value = ["/createGame"], produces = ["application/json"])
    private fun createGame(): ResponseEntity<Game> {
        val gameID = UUID.randomUUID().toString()
        val players = listOf("player-1", "player-2")
        val sampleGame = Game(gameID,  players, true, false)
        return ResponseEntity<Game>(sampleGame, HttpStatus.OK)
    }

    @PostMapping(value = ["/startGame"], produces = ["application/json"])
    private fun startGame(): ResponseEntity<String> {
        return ResponseEntity("started", HttpStatus.OK);
    }

    @PostMapping(value = ["/placeShip"], produces = ["application/json"])
    private fun placeShip(@RequestBody placement: Placement): ResponseEntity<String> {
        return ResponseEntity("placed", HttpStatus.OK);
    }
}


