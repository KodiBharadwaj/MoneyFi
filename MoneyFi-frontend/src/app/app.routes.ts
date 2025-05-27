import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { SignupComponent } from './signup/signup.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './auth.guard';
import { IncomeComponent } from './income/income.component';
import { ExpensesComponent } from './expenses/expenses.component';
import { BudgetsComponent } from './budgets/budgets.component';
import { GoalsComponent } from './goals/goals.component';
import { ProfileComponent } from './profile/profile.component';
import { OverviewComponent } from './overview/overview.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { AnalysisComponent } from './analysis/analysis.component';
import { AiAssistantComponent } from './ai-assistant/ai-assistant.component';
import { ContactUsComponent } from './contact-us/contact-us.component';
import { SignupOtpConfirmDialogComponent } from './signup-otp-confirm-dialog/signup-otp-confirm-dialog.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  {path:'forgot-password',component:ForgotPasswordComponent},
  { path: 'otp-confirm', component: SignupOtpConfirmDialogComponent },
  { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard],children:[
    {path:'',component:OverviewComponent},
    {path:'overview',component:OverviewComponent},
    {path:'income',component:IncomeComponent},
    {path:'expenses',component:ExpensesComponent},
    {path:'budgets',component:BudgetsComponent},
    {path:'goals',component:GoalsComponent},
    {path:'analysis',component:AnalysisComponent},
    {path:'profile',component:ProfileComponent},
    {path: 'contact-us', component:ContactUsComponent},
    { path: 'ai-assistant', component: AiAssistantComponent },
  ]},
];