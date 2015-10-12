package uk.ac.qmul.flagpredators.modules;

/**
 * Created by Ming-Jiun Huang on 15/6/9
 * Contact me at m.huang@hss13.qmul.ac.uk
 * Collect JSON Data and Parse it into a real command.
 * Hold all the protocol information to execute commands.
 * Data Format:
 *		<register_player>{username="Ming",latitude="2.2",longitude="1.1"}
 * 		<create_game>{playerid="pn00000001",latitude="2.2",longitude="1.1,players="10",flags="3",boundary="50",jail="false"}
 * 		<join_game>{playerid="pn00000002",gameid="gn00000001",latitude="2.2",longitude="1.1"}
 *		...
 */
public class ConnectionManager {
    private RespondingProtocol command;
    private Protocol protocol;
    private String commandValue;
    private String json;
    private String jsonValues;
    private boolean firstKey = true;
    private int keyStartIndex;

    //The constructor for Input Data
    ConnectionManager(String json){
        this.json = json;
        int startIndex = 0;
        keyStartIndex = json.indexOf(">",startIndex);
        commandValue =  json.substring(startIndex + 1, keyStartIndex);
        jsonValues = json.substring(keyStartIndex + 1, json.length());
        System.out.println("commandValue: " + commandValue); //Get the command of this connection.
    }

    //The constructor for Output Data
    ConnectionManager(Protocol protocol){
        this.protocol = protocol;
    }

    //Getters for testing
    String getCommandString(){ return commandValue; }
    String getKeyValues(){ return jsonValues; }

    //Search the value from the given key, returning the value in String type.
    String parseJson(String key){
        if(jsonValues.contains(key)){
            int keyIndex = jsonValues.indexOf(key,0);
            int startIndex = jsonValues.indexOf("\"",keyIndex);
            int endIndex = jsonValues.indexOf("\"",startIndex+1);
            String value = jsonValues.substring(startIndex+1, endIndex);
            System.out.println("Key: " + key + " Value: " + value);
            return value;
        }else{
            return null;
        }
    }

    //Check if there is a string key in this JSON.
    boolean isNull(String key){
        if(jsonValues.contains(key)){
            return false;
        }else{
            return true;
        }
    }

    //Get the responding protocol command from JSON String and Return a RespondingProtocol value
    RespondingProtocol getCommand(){
        switch(commandValue){
            case "respond_id":
                command = RespondingProtocol.RESPOND_ID;
                return command;
            case "game_initiated":
                command = RespondingProtocol.GAME_INITIATED;
                return command;
            case "show_games":
                command = RespondingProtocol.SHOW_GAMES;
                return command;
            case "show_gameroom":
                command = RespondingProtocol.SHOW_GAMEROOM_INFO;
                return command;
            case "joining_denied":
                command = RespondingProtocol.JOINING_DENIED;
                return command;
            case "still_in_game":
                command = RespondingProtocol.STILL_IN_GAME;
                return command;
            case "update_game_room":
                command = RespondingProtocol.UPDATE_GAME_ROOM;
                return command;
            case "update_game_info":
                command = RespondingProtocol.UPDATE_GAME_INFO;
                return command;
            case "get_flag":
                command = RespondingProtocol.GET_FLAG;
                return command;
            case "get_base":
                command = RespondingProtocol.GET_BASE;
                return command;
            case "out_of_bounds":
                command = RespondingProtocol.OUT_OF_BOUNDS;
                return command;
            case "game_over":
                command = RespondingProtocol.GAME_OVER;
                return command;
            case "error":
                command = RespondingProtocol.ERROR;
                return command;
            default:
                command = null;
                return command;
        }
    }

    //Return a request format of the protocol
    String getProtocolInString(){
        switch(protocol){
            case REGISTER_BROADCAST:
                return "<register_broadcast>";
            case BROADCAST:
                return "<broadcast>";
            case REGISTER_PLAYER:
                return "<register_player>";
            case CREATE_GAME:
                return "<create_game>";
            case REQUEST_GAMES:
                return "<request_games>";
            case JOIN_GAME:
                return "<join_game>";
            case JOIN_TEAM:
                return "<join_team>";
            case CHANGE_TEAM:
                return "<change_team>";
            case LEAVE_ROOM:
                return "<leave_room>";
            case CANCEL_GAME:
                return "<cancel_game>";
            case READY_TO_GO:
                return "<ready_to_go>";
            case START_GAME:
                return "<start_game>";
            case LEAVE_GAME:
                return "<leave_game>";
            case CHECK_LOCATION_WITH_FLAG:
                return "<check_location_with_flag>";
            case CHECK_LOCATION_WITH_BASE:
                return "<check_location_with_base>";
            default: return "null";
        }
    }

    //Add Key and Value for String data
    String addKeyValue(String key, String value){
        String info = "";
        if(firstKey){
            firstKey = false;
        }else{
            info += ",";
        }
        info += (key + "=\"" + value + "\"");
        return info;
    }

    //Return a String of RespondingProtocol
    String getRespondingProtocolInString(){
        this.getCommand();
        switch(command){
            case RESPOND_ID:
                return "<respond_id>";
            case GAME_INITIATED:
                return "<game_initiated>";
            case SHOW_GAMES:
                return "<show_games>";
            case SHOW_GAMEROOM_INFO:
                return "<show_gameroom>";
            case JOINING_DENIED:
                return "<joining_denied>";
            case STILL_IN_GAME:
                return "<still_in_game>";
            case UPDATE_GAME_ROOM:
                return "<update_game_room>";
            case UPDATE_GAME_INFO:
                return "<update_game_info>";
            case GET_FLAG:
                return "<get_flag>";
            case GET_BASE:
                return "<get_base>";
            case OUT_OF_BOUNDS:
                return "<out_of_bounds>";
            case GAME_OVER:
                return "<game_over>";
            case ERROR:
                return "<error>";
            default:
                return null;
        }
    }
}
