import { DropdownValue } from "./cdg-objects/dropdownvalue";
import { State } from "./cdg-objects/state";

export class Constants{
    public static get PAUSE_URL(): string {return "http://localhost:8080/api/generation/pause"}
    public static get STOP_URL(): string {return "http://localhost:8080/api/generation/pause"}
    public static get STATUS_URL(): string {return "http://localhost:8080/api/generation/status"}
    public static get PLAY_URL(): string {return "http://localhost:8080/api/generation/play"}
    public static get START_URL(): string { return "http://localhost:8080/api/generation/start"}
    public static get UNITED_STATES_URL(): string {return " http://localhost:8080/api/map/file/unitedstates"}
    public static get SAVE_URL(): string{return "http://localhost:8080/api/map/save"} 
    public static get SAVE_GEN_URL(): string{return "http://localhost:8080/api/generation/save/map"} 
    public static get DELETE_GEN_URL(): string{return "http://localhost:8080/api/generation/delete_user_map"} 
    public static get LOGOUT_URL(): string {return "http://localhost:8080/api/user/logout"}
    public static get LOGIN_URL(): string {return "http://localhost:8080/api/user/login"}
    public static get UNITED_STATES_DROPDOWNVALUE(): DropdownValue<State> {return  new DropdownValue<State>(new State("All", "0"), "All")}
    public static get SAVE_MAP_DROPDOWNVALUE(): DropdownValue<State> {return  new DropdownValue<State>(new State("Saved Maps", "-1"), "Saved Maps")}
    public static get NO_SAVED_MAPS(): DropdownValue<String> {return new DropdownValue<String>("", "No Saved Maps")}
    public static get FULLMAP_ID(): string {return "0"}
    public static get STATELIST_URL():string {return "http://localhost:8080/api/map/states"}
    public static get INIT_MAP_TYPE(): string {return "state"};
    public static get STATELIST_KEY(): string {return "states"};
    public static GET_FINISHED_URL(type:string): string {return "http://localhost:8080/api/generation/file/".concat(type)};
    public static GET_FINISHED_DATA_URL(type:string): string {return "http://localhost:8080/api/generation/data/".concat(type)};
    public static get COLOR_PROPERTY(): string {return "districtID"}
    public static get EXPORT_HEADERS(): string {return "application/json"}
    public static get EXPORT_FILE_NAME(): string {return "CDG_Map.json"}
    public static get INVALID_CRED():string {return""}
    public static get SUCCESS_REGISTER(): string{ return""}
    public static GET_MAP_DATA_URL(state:string, type:string): string {return "http://localhost:8080/api/map/data/".concat(state).concat("/".concat(type))}
    public static GET_STATE_URL(state:string, type:string): string {return "http://localhost:8080/api/map/file/".concat(state).concat("/".concat(type))}
    public static get CONFIRM_EDIT_USER(): string {return 'Are you sure you want to edit this user?'; }
    public static get CONFIRM_DELETE_USER(): string {return 'Are you sure you want to delete this user?'; }
    public static get CONFIRM_ADD_USER(): string {return 'Are you sure you want to add this user?'; }
    public static get USER_MAP_LIST_URL():string {return "http://localhost:8080/api/map/user_map_names"}
    public static GET_USER_MAP_URL(state:string, type:string): string {return "http://localhost:8080/api/generation/user_map/".concat(state).concat("/".concat(type))}
}