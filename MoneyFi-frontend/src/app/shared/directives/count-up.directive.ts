import { Directive, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CountUp } from 'countup.js';

@Directive({
  selector: '[appCountUp]',
  standalone: true
})
export class CountUpDirective implements OnChanges {
  @Input('appCountUp') endVal: number = 0;
  private countUp: CountUp;
  @Input() options: any

  constructor(private el: ElementRef) {
    this.countUp = new CountUp(this.el.nativeElement, 0, {
      duration: 1,
      prefix: '₹',
      separator: ',',
      decimal: '.',
      useGrouping: true
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['endVal'] && !changes['endVal'].firstChange) {
      this.countUp.update(this.endVal);
    } else {
      this.countUp.start();
    }
  }
} 
