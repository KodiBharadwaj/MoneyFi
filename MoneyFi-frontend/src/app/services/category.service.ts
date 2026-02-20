import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, of, tap } from "rxjs";
import { Category } from "../model/category-list";

@Injectable({ providedIn: 'root' })
export class CategoryService {

  private readonly STORAGE_KEY = 'CATEGORIES';

  constructor(private http: HttpClient) {}

  getIncomeCategories(): Observable<Category[]> {
  const stored = sessionStorage.getItem(this.STORAGE_KEY);

    if (stored) {
        const categories: Category[] = JSON.parse(stored);
        const income = categories.filter(c => c.type === 'INCOME');

        if (income.length) {
        return of(income);
        }
    }

    return this.http.post<Category[]>(
        `/api/v1/wealth-core/common/category-list/get`,
        ['ALL']
    ).pipe(
        map(res => {
        const withUiProps = res.map(c => ({ ...c, editing: false }));
        sessionStorage.setItem(
            this.STORAGE_KEY,
            JSON.stringify(withUiProps)
        );

        return withUiProps.filter(c => c.type === 'INCOME');
        })
    );
    }


    getExpenseCategories(): Observable<Category[]> {
  const stored = sessionStorage.getItem(this.STORAGE_KEY);

    if (stored) {
        const categories: Category[] = JSON.parse(stored);
        const expense = categories.filter(c => c.type === 'EXPENSE');

        if (expense.length) {
        return of(expense);
        }
    }

    return this.http.post<Category[]>(
        `/api/v1/wealth-core/common/category-list/get`,
        ['ALL']
    ).pipe(
        map(res => {
        const withUiProps = res.map(c => ({ ...c, editing: false }));
        sessionStorage.setItem(
            this.STORAGE_KEY,
            JSON.stringify(withUiProps)
        );

        return withUiProps.filter(c => c.type === 'EXPENSE');
        })
    );
    }


    getIncomeAndExpenseCategories(): Observable<Category[]> {
  const stored = sessionStorage.getItem(this.STORAGE_KEY);

  if (stored) {
    const categories: Category[] = JSON.parse(stored);
    return of(
      categories.filter(
        c => c.type === 'INCOME' || c.type === 'EXPENSE'
      )
    );
  }

  return this.http
    .post<Category[]>(
      `/api/v1/wealth-core/common/category-list/get`,
      ['ALL']
    )
    .pipe(
      map(res =>
        res.filter(
          c => c.type === 'INCOME' || c.type === 'EXPENSE'
        )
      ),
      tap(filtered =>
        sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(filtered))
      )
    );
}


getGoalCategories(): Observable<Category[]> {
  const stored = sessionStorage.getItem(this.STORAGE_KEY);

    if (stored) {
        const categories: Category[] = JSON.parse(stored);
        const goal = categories.filter(c => c.type === 'GOAL');

        if (goal.length) {
        return of(goal);
        }
    }

    return this.http.post<Category[]>(
        `/api/v1/wealth-core/common/category-list/get`,
        ['GOAL']
    ).pipe(
        map(res => {
        const withUiProps = res.map(c => ({ ...c, editing: false }));
        sessionStorage.setItem(
            this.STORAGE_KEY,
            JSON.stringify(withUiProps)
        );

        return withUiProps.filter(c => c.type === 'GOAL');
        })
    );
    }

}
