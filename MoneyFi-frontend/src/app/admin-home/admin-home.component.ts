import { Component, OnInit } from '@angular/core';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-admin-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-home.component.html',
  styleUrl: './admin-home.component.css'
})
export class AdminHomeComponent implements OnInit {
totalUsers = 0;
  activeUsers = 0;
  blockedUsers = 0;
  deletedUsers = 0;

  showGrid = false;
  selectedTile = '';
  gridData: any[] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.fetchCounts();
  }

  fetchCounts() {
    this.adminService.getUserCounts().subscribe(data => {
      this.totalUsers = data.totalUsers;
      this.activeUsers = data.activeUsers;
      this.blockedUsers = data.blockedUsers;
      this.deletedUsers = data.deletedUsers;
    });
  }

  fetchGrid(type: string) {
    this.selectedTile = type;
    this.showGrid = true;
    this.adminService.getUsersByStatus(type).subscribe(users => {
      this.gridData = users;
    });
  }

  navigateTo(route: string) {
    console.log('Navigate to:', route);
  }
}
