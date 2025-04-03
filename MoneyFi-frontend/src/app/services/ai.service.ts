import { Injectable } from '@angular/core';
import { GoogleGenerativeAI, HarmBlockThreshold, HarmCategory } from '@google/generative-ai';
import { environment } from '../../environments/environment.development';
import { Observable, from } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AiService {
  private readonly genAI: GoogleGenerativeAI;
  private readonly generationConfig = {
    safetySettings: [
      {
        category: HarmCategory.HARM_CATEGORY_HARASSMENT,
        threshold: HarmBlockThreshold.BLOCK_LOW_AND_ABOVE,
      },
    ],
    temperature: 0.8,
    top_p: 0.9,
    maxOutputTokens: 200,
  };

  constructor() {
    this.genAI = new GoogleGenerativeAI(environment.API_KEY);
  }

  /**
   * Gets AI-generated response for the given prompt
   * @param prompt - The input prompt for the AI model
   * @returns Observable<string> - The generated response
   */
  getAiResponse(prompt: string): Observable<string> {
    return from(this.generateResponse(prompt)).pipe(
      catchError((error) => {
        console.error('AI Response Generation Error:', error);
        throw new Error('Failed to generate AI response');
      })
    );
  }

  /**
   * Private method to handle the actual API call to the AI model
   * @param prompt - The input prompt for the AI model
   * @returns Promise<string> - The generated response
   */
  private async generateResponse(prompt: string): Promise<string> {
    const model = this.genAI.getGenerativeModel({
      model: 'gemini-pro',
      ...this.generationConfig,
    });

    const result = await model.generateContent(prompt);
    return result.response.text();
  }
}

// // Example usage of ai service in another component
// export class SomeOtherComponent {
//   constructor(private aiService: AiService) {}

//   someMethod() {
//     this.aiService.getAiResponse('your prompt here')
//       .subscribe({
//         next: (response) => {
//           // Handle the AI response
//         },
//         error: (error) => {
//           // Handle any errors
//         }
//       });
//   }
// }