import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ToastrService } from 'ngx-toastr';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-income-deleted',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './income-deleted.component.html',
  styleUrl: './income-deleted.component.css'
})
export class IncomeDeletedComponent {

  baseUrl = environment.BASE_URL

  constructor(private httpClient : HttpClient, private toastr : ToastrService,
     public dialogRef: MatDialogRef<IncomeDeletedComponent>, 
    @Inject(MAT_DIALOG_DATA) public data: { deletedIncomes: any[] }) {}

  closeDialog(): void {
    // You can add cleanup logic here if needed
    this.dialogRef.close();
  }

  revertIncomeFunction(incomeId : number){
    this.httpClient.get<Boolean>(`${this.baseUrl}/api/v1/income-service/user/incomeRevert/${incomeId}`).subscribe({
      next : (data) => {
        if(data){
          this.dialogRef.close(true);  
        }
        else{
          this.toastr.error('Failed to revert back!');
        }
      }
    })
  }
}
