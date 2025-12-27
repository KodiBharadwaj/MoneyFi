export interface ParsedTransaction {
  category: string;
  description: string;
  amount: number;
  transactionType: 'CREDIT' | 'DEBIT' | 'CREDIT OR DEBIT';
  transactionDate: string;
  accepted?: boolean;
}