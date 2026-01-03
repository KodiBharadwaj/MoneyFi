export interface UserNotification {
  notificationId: number;
  subject: string;
  description: string;
  scheduleFrom: string;
  scheduleTo: string;
  read: boolean;
}