import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AdminScheduleDialogComponent } from '../admin-schedule-dialog/admin-schedule-dialog.component';

@Component({
  selector: 'app-admin-configuration',
  standalone: true,
  imports: [],
  templateUrl: './admin-configuration.component.html',
  styleUrl: './admin-configuration.component.css'
})
export class AdminConfigurationComponent {

  constructor(private dialog: MatDialog) {}

  openScheduleNotificationDialog(){
    const dialogRef = this.dialog.open(AdminScheduleDialogComponent, {
      width: '600px'
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        console.log('Notification scheduled:', result);
        // Optionally refresh list
      }
    });
  }

}
