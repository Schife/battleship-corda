"use strict";

function showLoader() {
    $("#loader").show()
}

function hideLoader() {
    $("#loader").hide()
}


function populateGamesTable(gamesTableId) {
    //TODO:
    // - Replace join/start columns with proper action buttons wired to the web APIs.
    $.ajax({
        url: "/games",
        beforeSend: function() {
            showLoader();
        },
        success: function(result) {
            hideLoader();
            renderGames(gamesTableId, result)
        }
    });
}

function renderGames(gamesTableId, gamesPayload) {
    var tableItems = gamesPayload.map(game => {
        var gameIdColumn = $("<td>").text(game.id);
        var playersList = $("<ol>");
        var players = game.players.map(player => $("<li>").text(player));
        playersList.append(players);
        var isJoinable = game.joinable;
        var isStartable = game.startable;
        if (isJoinable == true) {
            var joinColumn = $("<td>").append("start");
        } else {
            var joinColumn = $("<td>").append("-");
        }

        if (isStartable == true) {
            var startColumn = $("<td>").append("start");
        } else {
            var startColumn = $("<td>").append("-");
        }
        var playersJoinedColumn = $("<td>").append(playersList);

        var gameRow = $("<tr>");
        gameRow.append(gameIdColumn);
        gameRow.append(playersJoinedColumn);
        gameRow.append(joinColumn);
        gameRow.append(startColumn);

        return gameRow;
    });
    tableItems.forEach(function(element) {
        $('#' + gamesTableId + " tbody").append(element);
    });
}