package com.template.webserver


import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import webserver.Game

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
}