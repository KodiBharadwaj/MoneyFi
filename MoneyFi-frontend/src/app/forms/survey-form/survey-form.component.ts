import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-survey-form',
  standalone : true,
  imports : [FormsModule, CommonModule],
  templateUrl: './survey-form.component.html',
  styleUrls: ['./survey-form.component.css']
})
export class SurveyFormComponent {
  
  survey = {
    name: '',
    email: '',
    satisfaction: '',
    featuresUsed: [] as string[],
    comments: ''
  };

  satisfactionOptions: string[] = ['Very Satisfied', 'Satisfied', 'Neutral', 'Dissatisfied', 'Very Dissatisfied'];
  featureOptions: string[] = ['Feature A', 'Feature B', 'Feature C', 'Feature D'];

  toggleFeature(feature: string) {
    if (this.survey.featuresUsed.includes(feature)) {
      this.survey.featuresUsed = this.survey.featuresUsed.filter(f => f !== feature);
    } else {
      this.survey.featuresUsed.push(feature);
    }
  }

  submitSurvey() {
    if (this.survey.name && this.survey.email && this.survey.satisfaction) {
      console.log('Survey Submitted:', this.survey);
      alert('Thank you for your feedback!');
      
      // Reset form after submission
      this.survey = { name: '', email: '', satisfaction: '', featuresUsed: [], comments: '' };
    }
  }

  
}

