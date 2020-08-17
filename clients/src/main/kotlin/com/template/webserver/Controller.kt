package com.template.webserver


import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import webserver.Coordinate
import webserver.Game
import webserver.Placement

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

    @GetMapping(value = ["/games"], produces = ["application/json"])
    private fun games(): ResponseEntity<List<Game>> {
        val sampleGameList = ArrayList<Game>();
        var sampleGame = Game("game-1",  null, true, false)
        val listPlayers = ArrayList<String>()
        listPlayers.add("player1")
        listPlayers.add("player2")
        sampleGame.players =listPlayers
        sampleGameList.add(sampleGame);
        return ResponseEntity<List<Game>>(sampleGameList, HttpStatus.OK);
    }

    @PostMapping(value = ["/createGame"], produces = ["application/json"])
    private fun createGame(): ResponseEntity<String> {
        return ResponseEntity("game-1", HttpStatus.CREATED);
    }

    @PostMapping(value = ["/startGame"], produces = ["application/json"])
    private fun startGeme(): ResponseEntity<String> {
        return ResponseEntity("started", HttpStatus.OK);
    }

    @PostMapping(value = ["/placeShip"], produces = ["application/json"])
    private fun placeShip(@RequestBody placement: Placement): ResponseEntity<String> {
        return ResponseEntity("placed", HttpStatus.OK);
    }
}


