export interface Category {
  categoryId: number;
  type: 'INCOME' | 'EXPENSE' | 'GOAL' | 'ALL';
  category: string;
  editing?: boolean;
}