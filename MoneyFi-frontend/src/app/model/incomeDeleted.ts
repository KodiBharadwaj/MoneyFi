export interface incomeDeleted {
    id : number;
    amount : number;
    source : string;
    category : string;
    date : Date;
    recurring : Boolean;
    daysRemained : number
}