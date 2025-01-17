import { Component, OnInit, Input, Output, EventEmitter} from '@angular/core';

@Component({
  selector: 'cdg-slider',
  templateUrl: './cdg-slider.component.html',
  styleUrls: ['./cdg-slider.component.scss']
})
export class CdgSliderComponent implements OnInit {
  @Input() slider:any;
  @Input() name: string;
  @Input() disabled:boolean = false;
  @Input() value:number = 0;
  @Input() img:string;
  @Output() change:EventEmitter<number>;
  constructor() {
    this.slider = 0;
    this.change = new EventEmitter<number>();
  }
  ngOnInit() {}
  onChange(event){
    this.change.emit(event.value);
    console.log(this.slider);
  }

  round(value){
    return Math.round(value);
  }
}
