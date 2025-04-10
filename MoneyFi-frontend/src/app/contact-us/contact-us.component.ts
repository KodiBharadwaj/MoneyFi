
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FeedbackFormComponent } from '../forms/feedback-form/feedback-form.component';
import { SurveyFormComponent } from '../forms/survey-form/survey-form.component';
import { ContactFormComponent } from '../forms/contact-form/contact-form.component';

@Component({
  selector: 'app-contact-us',
  standalone: true,
  imports: [ContactFormComponent, FeedbackFormComponent, SurveyFormComponent, CommonModule],
  templateUrl: './contact-us.component.html',
  styleUrls: ['./contact-us.component.css']
})
export class ContactUsComponent {
  selectedForm: string = ''; // Stores the selected form type

  // Available form options
  formOptions = [
    { value: 'contact', label: 'Contact Form' },
    { value: 'feedback', label: 'Feedback Form' }
  ];


  selectForm(event: Event) {
    const target = event.target as HTMLSelectElement; // Explicitly cast to HTMLSelectElement
    this.selectedForm = target.value; // Now TypeScript knows that 'value' exists
  }
  
}

