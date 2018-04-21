import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Constants } from "../../constants";
@Injectable()
export class MapService {
  mapType:string = "state"
  constructor(private http: HttpClient) {}
  getState(state:string){
    return this.http.get(Constants.GET_STATE_URL.concat(state).concat("/".concat(this.mapType)));
  }
  setType(type:string){
    this.mapType = type;
  }
  saveMap(){
    return this.http.get(Constants.SAVE_URL);
  }
}
