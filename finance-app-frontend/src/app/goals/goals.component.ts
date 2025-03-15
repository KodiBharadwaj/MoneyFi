import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AddGoalDialogComponent } from '../add-goal-dialog/add-goal-dialog.component';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { CountUpDirective } from '../shared/directives/count-up.directive';
import { AddAmountGoalComponent } from '../add-amount-goal/add-amount-goal.component';
import { ConfirmDeleteDialogComponent } from '../confirm-delete-dialog/confirm-delete-dialog.component';

interface Goal {
  id: number;
  goalName: string;
  currentAmount: number;
  targetAmount: number;
  deadLine: Date;
  category: string;
  icon: string;
  color: string
}

interface inputGoal {
  id: number;
  goalName: string;
  currentAmount: number;
  targetAmount: number;
  deadLine: Date;
  category: string;
}

@Component({
  selector: 'app-goals',
  templateUrl: './goals.component.html',
  styleUrls: ['./goals.component.scss'],
  standalone: true,
  imports: [CommonModule, CountUpDirective]
})
export class GoalsComponent {

  constructor(private httpClient:HttpClient, private dialog: MatDialog, private router:Router, private toastr:ToastrService){};
  baseUrl = "http://localhost:8765";

  goals: Goal[] = [];
  loading: boolean = false;

  // totalRemainingBalance : number | undefined;\
  totalRemainingBalance: number = 0;
  totalGoalSavings : number = 0;
  totalNetworth : number = 0;

  month : number = 0;
  year : number = 0;

  ngOnInit() {
    this.loadIncomeFunction();
    this.loadGoals();
  }

  loadIncomeFunction(){
    const currentDate = new Date();
    this.month = currentDate.getMonth()+1;
    this.year = currentDate.getFullYear();
  }

  
  loadGoals() {
    this.loading = true;
    const token = sessionStorage.getItem('finance.auth');
    // console.log(token);
  
    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next: (userId) => {
        // console.log(userId);
  
        this.httpClient.get<inputGoal[]>(`${this.baseUrl}/api/goal/${userId}`).subscribe({
          next: (data) => {
            // console.log(data);
            let amount = 0;
            if (data && data.length > 0) {

              this.goals = data.map(goal => {
                const convertedGoal = this.modelConverterFunction(goal);
                amount = amount + goal.currentAmount;
                // console.log(amount);
                return convertedGoal;
              });
            } else {
              this.goals = [];
              this.toastr.warning('No goal data is available.', 'No Data');
              this.loading = false;
            }
            this.totalGoalSavings = amount;
            this.httpClient.get<number>(`${this.baseUrl}/api/income/${userId}/totalRemainingIncomeUpToPreviousMonth/${this.month}/${this.year}`).subscribe({
              next: (totalIncome) => {
                // console.log(totalIncome);
                // console.log(amount);
                this.totalNetworth = totalIncome;
                this.totalRemainingBalance = totalIncome - amount;
              },
            });
          },
          error: (error) => {
            console.error('Failed to load Goal data:', error);
          },
          complete: () => {
            this.loading = false;
          }
        });
      },
      error: (error) => {
        console.error('Failed to fetch userId:', error);
        this.loading = false;
      }
    });
  }

  addGoal() {
    const dialogRef = this.dialog.open(AddGoalDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
    });

    if(this.totalRemainingBalance === 0){
      alert("You don't have savings till previous month. Adding goal money will be deducted next month!")
    }
  
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        const token = sessionStorage.getItem('finance.auth');
        // console.log(token);
  
        this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
          next: (userId) => {
            // console.log(userId);
            
            // Send POST request with the income data
            const formattedDate = this.formatDate(result.deadLine);
            const goalData = {
              ...result, // This should contain fields like source, amount, date, category, recurring, etc.
              // userId: userId, // Add userId if your backend requires it
              deadLine:formattedDate,
            };
            if(goalData.currentAmount < this.totalRemainingBalance){
              this.httpClient.post<inputGoal>(`${this.baseUrl}/api/goal/${userId}`, goalData, {
                headers: {
                  Authorization: `Bearer ${token}`,
                },
              }).subscribe({
                next: (newGoal) => {
                  // console.log(newGoal);
                  // this.goals = newGoal.map(goal => this.modelConverterFunction(goal));
                  const newGoalConverted = this.modelConverterFunction(newGoal); // Convert single goal
                  this.goals.push(newGoalConverted); // Add to existing goals array
                  // console.log(this.goals);
                  this.loadGoals();
                },
                error: (error) => {
                  console.error('Failed to add goal data:', error);
                },
                complete: () => {
                  this.loading = false;
                },
              });
            }else{
              alert("Cannot add the goal! Entered amount is greater than the remaining amount")
            }
          },
          error: (error) => {
            console.error('Failed to fetch userId:', error);
            alert("Session timed out! Please login again");
            sessionStorage.removeItem('finance.auth');
            this.router.navigate(['login']);
            this.loading = false;
          },
        });
      }
    });
  }

  addAmount(id: number) {

    const token = sessionStorage.getItem('finance.auth');
    // console.log(token);
    // console.log(id);
  
    this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
      next: (userId) => {
        // console.log(userId);
  
        // Open the dialog to get the amount
        const dialogRef = this.dialog.open(AddAmountGoalComponent, {
          width: '300px',
          data: { id }
        });
  
        // Handle the dialog's close event
        dialogRef.afterClosed().subscribe((amount) => {
          if (amount !== undefined && amount > 0 && amount < this.totalRemainingBalance) {
            this.httpClient
              .post<inputGoal>(`${this.baseUrl}/api/goal/${id}/addAmount/${amount}`, null)
              .subscribe({
                next: (response) => {
                  // console.log(response);
                  this.loadGoals();
                },
                error: (error) => {
                  console.error('Error adding amount:', error);
                }
              });
          }else {
            alert("Entered money is greater than the remaining amount")
          }
        });
      }
    });
  }
  

  updateGoal(goal: inputGoal) {
    const dialogRef = this.dialog.open(AddGoalDialogComponent, {
      width: '500px',
      panelClass: 'income-dialog',
      data: { ...goal, isUpdate: true }, // Pass the income data to the dialog
    });
    // console.log(goal);
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.currentAmount < this.totalRemainingBalance) {
        const token = sessionStorage.getItem('finance.auth');
        this.httpClient.get<number>(`${this.baseUrl}/auth/token/${token}`).subscribe({
          next: (userId) => {
            // console.log(userId);
            
            // Send POST request with the income data
            // console.log(result);
            const formattedDate = this.formatDate(result.deadLine);
            const goalData = {
              ...result, // This should contain fields like source, amount, date, category, recurring, etc.
              // userId: userId, // Add userId if your backend requires it
              userId:userId,
              deadLine:formattedDate,
            };
            // console.log(goalData);
            this.httpClient.put<inputGoal>(`${this.baseUrl}/api/goal/${goal.id}`, goalData, {
              headers: {
                Authorization: `Bearer ${token}`,
              },
            }).subscribe({
              next: (updatedGoal) => {
                const newGoalConverted = this.modelConverterFunction(updatedGoal); // Convert single goal
                this.goals.push(newGoalConverted); // Add to existing goals array
                // console.log(this.goals);
                this.toastr.success("Goal has been updated");
                this.loadGoals();
              },
              error: (error) => {
                console.error('Failed to update goal data:', error);
              },
              complete: () => {
                this.loading = false;
              },
            });
          },
          error: (error) => {
            console.error('Failed to fetch userId:', error);
            alert("Session timed out! Please login again");
            sessionStorage.removeItem('finance.auth');
            this.router.navigate(['login']);
            this.loading = false;
          },
        });
        
      }
    });
  }


  formatDate(date: string): string {
    const d = new Date(date);
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0'); // Months are zero-based
    const dd = String(d.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

 
  modelConverterFunction(data: inputGoal): Goal {
    let icon = '';
    let color = '';
    
    if (data.category === 'Vacation') {
      icon = 'fa-plane';
      color = '#2196F3';
    } else if (data.category === 'Savings') {
      icon = 'fa-shield-alt';
      color = '#4CAF50';
    } else if (data.category === 'Vehicle') {
      icon = 'fa-car';
      color = '#FF9800';
    } else if (data.category === 'Health') {
      icon = 'fa-heartbeat';
      color = '#E91E63';
    } else if (data.category === 'Education') {
      icon = 'fa-graduation-cap';
      color = '#673AB7';
    } else if (data.category === 'Home') {
      icon = 'fa-home';
      color = '#009688';
    } else if (data.category === 'Investments') { 
      icon = 'fa-chart-line'; 
      color = '#3F51B5';      
    } else if (data.category === 'Electronics') {
      icon = 'fa-laptop'; 
      color = '#00BCD4'; 
    } else {
      icon = 'fa-globe'; 
      color = '#607D8B'; 
    }
  
    return {
      id: data.id,
      goalName: data.goalName,
      currentAmount: data.currentAmount,
      targetAmount: data.targetAmount,
      deadLine: new Date(data.deadLine),
      category: data.category,
      icon: icon,
      color: color
    };
  }
  
  

  // getProgressPercentage(currentAmount: number, targetAmount: number): number {
  //   return (currentAmount / targetAmount) * 100;
  // }
  getProgressPercentage(currentAmount: number, targetAmount: number): number {
    return Math.min((currentAmount / targetAmount) * 100, 100); // Ensure it doesn't exceed 100%
  }

  getDaysRemaining(deadline: Date): number {
    if (!deadline) {
      console.error('Deadline is undefined or null');
      return NaN;
    }
  
    const deadlineDate = new Date(deadline);
    if (isNaN(deadlineDate.getTime())) {
      console.error('Invalid deadline date:', deadline);
      return NaN;
    }
  
    const today = new Date();
    const diffTime = deadlineDate.getTime() - today.getTime();
    return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }
  

  getProgressColor(currentAmount: number, targetAmount: number): string {
    const percentage = this.getProgressPercentage(currentAmount, targetAmount);
    if (percentage >= 90) return '#4caf50';
    if (percentage >= 50) return '#2196f3';
    return '#ff9800';
  }

  
  viewGoalDetails(goal: Goal) {
    
  }


  deleteGoal(goalId : number){
    // console.log(goalId);
    const dialogRef = this.dialog.open(ConfirmDeleteDialogComponent, {
      width: '400px',
      panelClass: 'custom-dialog-container',
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.httpClient.delete<void>(`${this.baseUrl}/api/goal/${goalId}`)
        .subscribe({
          next: () => {
            // console.log(`Expense with ID ${goalId} deleted successfully.`);
            this.toastr.warning("Goal has been deleted");
            this.loadGoals(); // Reload the data after successful deletion
            this.loadIncomeFunction();
          },
          error: (err) => {
            console.error('Error deleting goal:', err);
          }
        });
      }
    });

  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }

  getGoalStatus(goal: Goal): string {
    const progressPercentage = this.getProgressPercentage(goal.currentAmount, goal.targetAmount);
    const daysRemaining = this.getDaysRemaining(goal.deadLine);
  
    if (progressPercentage >= 100) {
      return daysRemaining > 0 ? 'completed-early' : 'completed-on-time';
    }
  
    if (daysRemaining <= 0) {
      return 'overdue';
    }
  
    return 'in-progress';
  }

}