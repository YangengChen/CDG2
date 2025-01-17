import { Component, OnInit } from '@angular/core';
import { RouterLink } from "@angular/router"
@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
      host: {
        '(window:scroll)': 'onScroll($event)'
    }
})
export class HomeComponent implements OnInit {

    isScrolled = false;
    currPos: Number = 0;
    startPos: Number = 0;
    changePos: Number = 100;

    constructor() {}

    onScroll(evt) {//window object can be wrapper in a service but for now we directly use it
        this.currPos = (window.pageYOffset || evt.target.scrollTop) - (evt.target.clientTop || 0);
        if(this.currPos >= this.changePos ) {
            this.isScrolled = true;
        } else {
            this.isScrolled = false;
        }
    }

  ngOnInit() {
  }

}
