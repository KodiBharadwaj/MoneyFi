import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AdminService } from '../services/AdminService';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css'],
  imports : [CommonModule],
  standalone : true
})
export class AdminUsersComponent implements OnInit {
  status: string = '';
  users: any[] = [];

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
}
