export interface NotificationMessage {
  id?: string;
  message: string;
  userId: string;
  seen?: boolean;
  timestamp?: string | Date;
}
