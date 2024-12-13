import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formatAdvice',
  standalone: true
})
export class FormatAdvicePipe implements PipeTransform {
  transform(value: string): string {
    if (!value) return '';

    // Add styling to sections
    value = value.replace(/^([\w\s]+:)/gm, '<h3>$1</h3>');
    
    // Style numbers
    value = value.replace(/₹[\d,]+/g, '<span class="number">$&</span>');
    
    // Style percentages
    value = value.replace(/\d+(\.\d+)?%/g, '<span class="number">$&</span>');
    
    // Style recommendations
    value = value.replace(
      /(Recommendation:|Suggested action:|Tip:)(.*?)(\n|$)/g,
      '<div class="recommendation">$1$2</div>'
    );

    return value;
  }
} 