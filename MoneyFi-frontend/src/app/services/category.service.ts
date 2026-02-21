import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable } from "rxjs";
import { Category } from "../model/category-list";
import { environment } from "../../environments/environment";

@Injectable({ providedIn: 'root' })
export class CategoryService {

  constructor(private http: HttpClient) {}

  baseUrl = environment.BASE_URL;

  getIncomeCategories(): Observable<any[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/api/v1/wealth-core/common/category-list/get`, ['INCOME'])
      .pipe(
        map(res =>
          res.map(item => ({
            ...item,
            editing: false
          }))
        )
      );
  }

  getExpenseCategories(): Observable<any[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/api/v1/wealth-core/common/category-list/get`, ['EXPENSE'])
      .pipe(
        map(res =>
          res.map(item => ({
            ...item,
            editing: false
          }))
        )
      );
  }


  getIncomeAndExpenseCategories(): Observable<Category[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/api/v1/wealth-core/common/category-list/get`, ['INCOME', 'EXPENSE'])
      .pipe(
        map(res =>
          res.map(item => ({
            ...item,
            editing: false
          }))
        )
      );
  }

  getGoalCategories(): Observable<any[]> {
    return this.http
      .post<any[]>(`${this.baseUrl}/api/v1/wealth-core/common/category-list/get`, ['GOAL'])
      .pipe(
        map(res =>
          res.map(item => ({
            ...item,
            editing: false
          }))
        )
      );
  }
}
