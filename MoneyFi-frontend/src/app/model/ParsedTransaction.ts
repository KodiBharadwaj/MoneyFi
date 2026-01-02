export interface ParsedTransaction {
  categoryId: number;
  description: string;
  amount: number;
  transactionType: 'CREDIT' | 'DEBIT' | 'CREDIT OR DEBIT';
  transactionDate: string;
  accepted?: boolean;

  categoryName?: string;
}