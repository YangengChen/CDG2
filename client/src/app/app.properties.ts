import { Inject, Injectable } from '@angular/core';
import { Http } from '@angular/http';
import { Observable } from 'rxjs/Rx';

@Injectable()
export class AppProperties {

    private properties: any;

    constructor(private http: Http) {

    }
    public getProperties(){
        return this.properties;
    }
    public load() {
        return new Promise((resolve, reject) => {
            this.http.get("http://localhost:4200/assets/angular.properties.json")
            .subscribe(props => {
                this.properties = props.json();
                console.log(this.properties);
                resolve(true);
            });

        });
    }

}