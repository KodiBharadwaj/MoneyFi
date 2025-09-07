import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { KeepAliveService } from './keep-alive.service';


@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  title = 'angular-cursor';

  constructor(private keepAlive: KeepAliveService) {}

  ngOnInit() {
    this.keepAlive.startPinging();
  }
}