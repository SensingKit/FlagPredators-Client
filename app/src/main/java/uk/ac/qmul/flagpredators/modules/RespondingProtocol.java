package uk.ac.qmul.flagpredators.modules;

/**
 * Created by Ming-Jiun Huang on 15/7/22.
 * Contact me at m.huang@hss13.qmul.ac.uk
 */

public enum RespondingProtocol {
    RESPOND_ID,
    GAME_INITIATED,
    SHOW_GAMES,
    SHOW_GAMEROOM_INFO,
    JOINING_DENIED,
    STILL_IN_GAME,
    UPDATE_GAME_ROOM,
    UPDATE_GAME_INFO,
    GET_FLAG,
    GET_BASE,
    OUT_OF_BOUNDS,
    GAME_OVER,
    ERROR
}
