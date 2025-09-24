export interface Reservation {
  id: string;
  sportId: string;
  sportName: string;
  startTime: string;
  endTime: string;
  status: string;
  userId: string;
  username: string;
  participants: string[];
  openForJoin?: boolean;
}
