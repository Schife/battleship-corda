package com.r3.battleship.flows

import com.r3.battleship.repository.GameService
import com.r3.battleship.schemas.GameDTO
import net.corda.core.flows.*

@StartableByRPC
@InitiatingFlow
class GetGamesFlow() : FlowLogic<List<GameDTO>>() {

    override fun call(): List<GameDTO> {
        val service = GameService(serviceHub)
        val allGames = service.getAllGames()
        val listOfGames = mutableListOf<GameDTO>()
        for (game in allGames) {
            listOfGames.add(GameDTO.fromEntity(game))
        }
        return listOfGames
    }
}