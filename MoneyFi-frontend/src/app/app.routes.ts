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
import { ForgotUsernameComponent } from './forgot-username/forgot-username.component';
import { ReportsInsightsComponent } from './reports-insights/reports-insights.component';
import { RaiseRequestComponent } from './raise-request/raise-request.component';
import { RequestTrackerComponent } from './request-tracker/request-tracker.component';
import { AdminHomeComponent } from './admin-home/admin-home.component';
import { AdminUsersComponent } from './admin-users/admin-users.component';
import { AdminLoginComponent } from './admin-login/admin-login.component';
import { AdminRequestsComponent } from './admin-requests/admin-requests.component';
import { AdminInsightsComponent } from './admin-insights/admin-insights.component';
import { AdminUserDefectsComponent } from './admin-user-defects/admin-user-defects.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'admin/login', component: AdminLoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'forgot-password',component:ForgotPasswordComponent },
  { path: 'forgot-username', component:ForgotUsernameComponent },
  { path: 'raise-request', component:RaiseRequestComponent },
  { path: 'otp-confirm', component: SignupOtpConfirmDialogComponent },
  { path: 'track-request', component: RequestTrackerComponent },
  { path: 'admin/home', component: AdminHomeComponent },
  { path: 'admin/users/:status', component: AdminUsersComponent },
  { path: 'admin/requests/:status', component: AdminRequestsComponent },
  { path: 'admin/insights', component: AdminInsightsComponent },
  { path: 'admin/user-defects', component: AdminUserDefectsComponent },
  // { path: 'admin/feedbacks', component: AdminFeedbacksComponent },
  // { path: 'admin/configuration', component: AdminConfigurationComponent },
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
    {path: 'ai-assistant', component: AiAssistantComponent},
    {path: 'reports-insights', component:ReportsInsightsComponent},
  ]},
];