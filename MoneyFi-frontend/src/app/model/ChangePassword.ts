export interface ChangePassword {
    userId: number;
    currentPassword: string;
    newPassword: string;
    description: string;
}