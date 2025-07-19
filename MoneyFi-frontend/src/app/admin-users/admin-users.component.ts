import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  imports : [CommonModule, FormsModule],
  standalone : true
})
export class AdminUsersComponent implements OnInit {
  status: string = '';
  users: any[] = [];

  nameFilter: string = '';
  usernameFilter: string = '';
  phoneFilter: string = '';
  isAscending: boolean = true;

  constructor(private route: ActivatedRoute, private adminService: AdminService) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.status = params.get('status') || '';
      this.fetchUsers(this.status);
    });
  }

  fetchUsers(status: string) {
    this.adminService.getUsersByStatus(status).subscribe(data => {
      this.users = data;
    });
  }

  // Filter function
  filteredUsers() {
    let filtered = this.users.filter(user =>
      (!this.nameFilter || user.name?.toLowerCase().includes(this.nameFilter.toLowerCase())) &&
      (!this.usernameFilter || user.username?.toLowerCase().includes(this.usernameFilter.toLowerCase())) &&
      (!this.phoneFilter || user.phone?.toString().includes(this.phoneFilter))
    );

    return this.isAscending
      ? filtered
      : [...filtered].reverse();
  }


  // Toggle order
  toggleOrder() {
    this.isAscending = !this.isAscending;
  }

}
