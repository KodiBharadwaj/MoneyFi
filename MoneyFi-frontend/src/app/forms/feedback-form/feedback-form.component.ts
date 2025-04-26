// import { Component } from '@angular/core';

// @Component({
//   selector: 'app-feedback-form',
//   standalone: true,
//   imports: [],
//   templateUrl: './feedback-form.component.html',
//   styleUrl: './feedback-form.component.css'
// })
// export class FeedbackFormComponent {

// }

import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { UserProfile } from '../../model/UserProfile';
import { ProfileDetails } from '../../model/ProfileDetails';

@Component({
  selector: 'app-feedback-form',
  standalone : true,
  imports : [FormsModule, CommonModule],
  templateUrl: './feedback-form.component.html',
  styleUrls: ['./feedback-form.component.css']
})
export class FeedbackFormComponent {

  constructor(private httpClient:HttpClient, private router:Router, private toastr:ToastrService){};
  baseUrl = "http://localhost:8765";
  
  feedback = {
    name: '',
    email: '',
    rating: 0,
    comments: ''
  };

  stars: number[] = [1, 2, 3, 4, 5];

  ngOnInit(){
    
    this.getNameAndEmailOfUser();
  }

  getNameAndEmailOfUser(){
    this.httpClient.get<ProfileDetails>(`${this.baseUrl}/api/v1/userProfile/getProfile`).subscribe({
      next: (userProfile) => {
        this.feedback.name = userProfile.name;
        this.feedback.email = userProfile.email;
      },
      error: (error) => {
        console.log('Failed to get the user details', error);
      }
    });
  }

  selectRating(value: number) {
    this.feedback.rating = value;
  }

  submitFeedback() {
    if (this.feedback.name && this.feedback.email && this.feedback.rating) {
      // console.log('Feedback Submitted:', this.feedback);
      // alert('Thank you for your feedback!');
      const contactDto = {
        name : this.feedback.name,
        email : this.feedback.email,
        rating : this.feedback.rating,
        comments : this.feedback.comments
      }

      this.httpClient.post(`${this.baseUrl}/api/userProfile/feedback`, contactDto).subscribe(
        (response) => {
          // alert('Form submitted successfully!');
          this.toastr.success('Feedback submitted successfully!', '', {
            timeOut: 1500  // toast visible for 3 seconds
          });
          
          setTimeout(() => {
            window.location.reload();
          }, 1500);
        },
        error => {
          console.error('Error submitting feedback form:', error);
          alert('Failed to submit feedback form. Please try again.');
        }
      );
      
      // Reset form after submission
      this.feedback = { name: this.feedback.name, email: this.feedback.email, rating: 0, comments: '' };
    }
  }
}
