export interface incomeDeleted {
    amount : number;
    source : string;
    category : string;
    date : Date;
    recurring : Boolean;
    daysRemained : number
}