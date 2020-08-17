"use strict";

function showLoader() {
    $("#loader").show()
}

function hideLoader() {
    $("#loader").hide()
}


function populateGamesTable(gamesTableId) {
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

function createNewGame(gamesTableId) {
    $.ajax({
        url: "/createGame",
        method: "POST",
        contentType: "application/json",
        dataType: 'json',
        beforeSend: function() {
            showLoader();
        },
        success: function( result ) {
            hideLoader();
            var newGames = []
            newGames.push(result)
            renderGames(gamesTableId, newGames)
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
            var request = {
                id: game.id
            }
            var joinButton = $("<button>", {
                text: "Join Game",
                class: "btn btn-success",
                click: function() {
                    var url = "/joinGame";
                    $.ajax({
                            url: url,
                            method: "POST",
                            contentType: "application/json",
                            data: JSON.stringify(request),
                            dataType: 'json',
                            beforeSend: function() {
                                showLoader();
                            },
                            success: function( result ) {
                                hideLoader();

                                //remove game from table and re-insert the updated one.
                                $("#" + game.id).remove()
                                var newGames = []
                                newGames.push(result)
                                renderGames(gamesTableId, newGames)
                            }
                        });
                }
            });
            var joinColumn = $("<td>").append(joinButton);
        } else {
            var joinColumn = $("<td>").append("-");
        }

        //TODO:
        // - Replace join/start columns with proper action buttons wired to the web APIs.
        if (isStartable == true) {
            var startColumn = $("<td>").append("start");
        } else {
            var startColumn = $("<td>").append("-");
        }
        var playersJoinedColumn = $("<td>").append(playersList);

        var gameRow = $("<tr>", { id: game.id });
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

