import {Component, OnDestroy, OnInit} from "@angular/core";
import { FormBuilder, FormGroup } from "@angular/forms";
import { Sport } from "../../models/sport.interface";
import { Reservation } from "../../models/reservation.interface";
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DestroyRef, inject } from '@angular/core';
import { Validators } from '@angular/forms';
import {SportService} from "../../services/sport.service";
import {ReservationService} from "../../services/reservation.service";
import {AuthService} from "../../services/auth.service";
import {NotificationService} from "../../services/notification.service";
import {Subscription} from "rxjs";

@Component({
  selector: "app-dashboard",
  templateUrl: "./dashboard.component.html",
  styleUrls: ["./dashboard.component.css"],
})
export class DashboardComponent implements OnInit {
  scheduleForm!: FormGroup;
  sports: Sport[] = [];
  reservations: Reservation[] = [];
  message: string | null = null;
  messageType: 'success' | 'error' = 'success';
  messageTitle: string = '';
  currentUsername: string = '';
  isAdmin: boolean = false;
  isCreateModalOpen = false;
  createForm!: FormGroup;
  isBlockModalOpen = false;
  isBlockMode = false;
  allUsers: string[] = [];
  selectedSportObj: any;
  maxParticipants: number = 0;
  remainingParticipants: number = 0;
  showMyReservations: boolean = false;
  filteredReservations: Reservation[] = [];
  private destroyRef = inject(DestroyRef);
  ratingForm!: FormGroup;
  isRatingModalOpen: boolean = false;
  ratingReservationId: string | null = null;
  userHasRated: boolean = false;
  showRatings: boolean = false;
  reservationRatings: any[] = [];
  isDeleteModalOpen: boolean = false;
  reservationIdToDelete: string | null = null;

  timeSlots: string[] = [
    "08:00 09:00", "09:00 10:00", "10:00 11:00", "11:00 12:00",
    "12:00 13:00", "13:00 14:00", "14:00 15:00", "15:00 16:00",
    "16:00 17:00", "17:00 18:00", "18:00 19:00", "19:00 20:00"
  ];

  isModalOpen = false;
  modalData: {
    sportId: string;
    sportName: string;
    slot: string;
    reserved: boolean;
    userId?: string;
    username?: string;
    participants?: string[];
    pastSlot?: boolean;
    maxParticipants?: number;
    remainingSlots?: number;
    openForJoin?: boolean;
  } = this.getEmptyModalData();

  constructor(
    private fb: FormBuilder,
    private sportService: SportService,
    private reservationService: ReservationService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.scheduleForm = this.fb.group({
      date: [new Date().toISOString().substring(0, 10)],
      sport: ['']
    });

    this.ratingForm = this.fb.group({
      hygiene: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      equipment: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      atmosphere: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      comment: ['']
    })

    this.createForm = this.fb.group({
      sport: [''],
      date: [new Date().toISOString().substring(0, 10)],
      timeFrom: [''],
      timeTo: [''],
      participants: [[]],
      openForJoin: [false]
    });

    this.createForm.get('timeFrom')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((startTime: string) => {
        if (startTime) {
          const [hours, minutes] = startTime.split(':').map(Number);
          const date = new Date();
          date.setHours(hours, minutes);
          date.setHours(date.getHours() + 1);
          const newHours = date.getHours().toString().padStart(2, '0');
          const newMinutes = date.getMinutes().toString().padStart(2, '0');
          const newTime = `${newHours}:${newMinutes}`;
          this.createForm.get('timeTo')?.setValue(newTime);
        } else {
          this.createForm.get('timeTo')?.setValue('');
        }
      });

    this.createForm.get('participants')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((selected: string[]) => {
        this.onCreateParticipantsChange(selected);
        this.remainingParticipants = this.maxParticipants - (selected ? selected.length : 0);
      });

    this.loadSports();
    this.loadReservations();
    this.loadUsers();

    this.currentUsername = this.authService.getUsername();
    this.isAdmin = this.authService.isAdmin();
  }

  private loadUsers(): void {
    this.authService.getAllUsernames().subscribe({
      next: (users: string[]) => {
        this.allUsers = users;
      },
      error: (err) => console.error('Error loading users', err)
    });
  }

  private getEmptyModalData() {
    return {
      sportId: '',
      sportName: '',
      slot: '',
      reserved: false,
      userId: '',
      username: '',
      participants: [],
      pastSlot: false,
      openForJoin: false
    };
  }

  private getReservation(sportName: string, slot: string): Reservation | undefined {
    const [start, end] = slot.split(" ");
    return this.filteredReservations.find(r => {
      if (!r.startTime || !r.endTime) return false;
      const resDate = r.startTime.split("T")[0];
      const resStart = r.startTime.split("T")[1].substring(0, 5);
      const resEnd = r.endTime.split("T")[1].substring(0, 5);
      return r.sportName === sportName &&
        resDate === this.selectedDate &&
        resStart === start &&
        resEnd === end &&
        r.status !== 'CANCELED';
    });
  }

  private loadSports(): void {
    this.sportService.getAllSports().subscribe({
      next: (data) => (this.sports = data),
      error: (err) => console.error("Greška pri učitavanju sportova", err),
    });
  }

  private loadReservations(): void {
    this.reservationService.getAllReservations().subscribe({
      next: (data) => {
        this.reservations = data;
        this.filterReservations();
      },
      error: (err) => console.error("Greška pri učitavanju rezervacija", err),
    });
  }

  get selectedDate(): string {
    return this.scheduleForm.get("date")?.value;
  }

  get selectedSport(): Sport[] {
    const sportName = this.scheduleForm.get("sport")?.value;
    return sportName ? this.sports.filter(s => s.name === sportName) : this.sports;
  }

  get formattedDate(): string {
    const dateValue = this.scheduleForm.get("date")?.value;
    if (!dateValue) return '';
    const date = new Date(dateValue);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }

  openModal(sport: Sport, slot: string): void {
    const reservation = this.getReservation(sport.name, slot);
    const [start, end] = slot.split(" ");

    const now = new Date();
    const slotStart = new Date(`${this.selectedDate}T${start}:00`);
    const pastSlot = slotStart <= now;

    const maxParticipants = sport.playerCount ? sport.playerCount - 1 : 0;
    const participants = reservation?.participants ? [...reservation.participants] : [];
    const remainingSlots = maxParticipants - participants.length;

    this.modalData = {
      sportId: sport.id,
      sportName: sport.name,
      slot: slot.replace(" ", "-"),
      reserved: !!reservation,
      userId: reservation?.userId,
      username: reservation?.username,
      participants: participants,
      pastSlot: pastSlot,
      maxParticipants: maxParticipants,
      remainingSlots: remainingSlots,
      openForJoin: reservation?.openForJoin || false
    };

    if (reservation) {
      this.ratingReservationId = reservation.id;

      this.reservationService.hasUserRated(reservation.id).subscribe({
        next: (hasRated) => {
          this.userHasRated = hasRated;
          this.isModalOpen = true;
        },
        error: (err) => {
          console.error('Error checking rating status', err);
          this.userHasRated = false;
          this.isModalOpen = true;
        }
      });
    } else {
      this.userHasRated = false;
      this.ratingReservationId = null;
      this.isModalOpen = true;
    }
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.modalData = this.getEmptyModalData();

    this.showRatings = false;
    this.reservationRatings = [];
  }

  isReserved(sportName: string, slot: string): boolean {
    const res = this.getReservation(sportName, slot);
    return !!res && res.status === 'ACTIVE';
  }

  isBlocked(sportName: string, slot: string): boolean {
    const res = this.getReservation(sportName, slot);
    return !!res && res.status === 'BLOCKED';
  }

  isOpen(sportName: string, slot: string): boolean {
    const reservation = this.getReservation(sportName, slot);
    return !!reservation && reservation.status === 'ACTIVE' && reservation.openForJoin === true;
  }

  getSlotLabel(sportName: string, slot: string): string {
    const res = this.getReservation(sportName, slot);
    if (!res) return '';
    if (res.status === 'BLOCKED') return 'Blocked';
    if (this.isOpen(sportName, slot)) return 'Join';
    return 'Busy';
  }

  createReservation(): void {
    if (this.modalData.reserved) return;

    const [start, end] = this.modalData.slot.split("-");
    const now = new Date();
    const reservationStart = new Date(`${this.selectedDate}T${start}:00`);

    if (reservationStart <= now) {
      this.showPopup('The appointment has already started and it is not possible to book.', 'error', 'Error');
      return;
    }

    const participants = [...(this.modalData.participants || [])];
    const sportObj = this.sports.find(s => s.id === this.modalData.sportId);
    const sportName = sportObj ? sportObj.name : 'Sport';

    const reservationDate = new Date(this.selectedDate);
    const formattedDate = `${String(reservationDate.getDate()).padStart(2,'0')}.${String(reservationDate.getMonth()+1).padStart(2,'0')}.${reservationDate.getFullYear()}`;
    const startEnd = `${start}-${end}`;

    const payload = {
      sportId: this.modalData.sportId,
      startTime: `${this.selectedDate}T${start}:00`,
      endTime: `${this.selectedDate}T${end}:00`,
      participants: participants,
      openForJoin: this.modalData.openForJoin || false
    };

    this.reservationService.createReservation(payload).subscribe({
      next: () => {
        this.loadReservations();
        this.closeModal();

        const creatorUserId = this.authService.getCurrentUserId();
        if (creatorUserId) {
          const creatorMessage = `Reservation successfully created for ${sportName} (${startEnd}) days ${formattedDate}.`;
          this.notificationService.sendNotification(creatorMessage, creatorUserId);
        }

        participants.forEach(username => {
          this.authService.getUserIdByUsername(username).subscribe({
            next: userId => {
              if (userId) {
                const participantMessage = `You have been added to the reservation for ${sportName} (${startEnd}) days ${formattedDate}.`;
                this.notificationService.sendNotification(participantMessage, userId);
              }
            }
          });
        });

        this.showPopup('Reservation created successfully!', 'success', 'Success');
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'There is already a reservation in that period.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }


  cancelReservation(): void {
    const [start, end] = this.modalData.slot.split("-");
    const reservation = this.reservations.find(r =>
      r.userId === this.modalData.userId &&
      r.sportName === this.modalData.sportName &&
      r.startTime.includes(start) &&
      r.endTime.includes(end) &&
      r.status === 'ACTIVE'
    );
    if (!reservation) {
      this.closeModal();
      this.loadReservations();
      return;
    }

    const sportName = reservation.sportName;
    const reservationDate = new Date(reservation.startTime.split('T')[0]);
    const formattedDate = `${String(reservationDate.getDate()).padStart(2,'0')}.${String(reservationDate.getMonth()+1).padStart(2,'0')}.${reservationDate.getFullYear()}`;
    const startEnd = `${start}-${end}`;

    this.reservationService.cancelReservation(reservation.id).subscribe({
      next: () => {
        this.loadReservations();
        this.closeModal();

        const ownerId = reservation.userId;
        const participants = reservation.participants || [];

        if (ownerId) {
          const ownerMessage = `Your reservation for ${sportName} (${startEnd}) days ${formattedDate} was cancelled.`;
          this.notificationService.sendNotification(ownerMessage, ownerId);
        }

        participants
          .filter(username => username !== reservation.username)
          .forEach(username => {
            this.authService.getUserIdByUsername(username).subscribe({
              next: userId => {
                if (userId) {
                  const participantMessage = `Reservation for ${sportName} (${startEnd}) days ${formattedDate} which you belonged to has been cancelled.`;
                  this.notificationService.sendNotification(participantMessage, userId);
                }
              }
            });
          });

        this.showPopup('Reservation canceled successfully!', 'success', 'Success');
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Error canceling reservation.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  showPopup(message: string, type: 'success' | 'error' = 'success', title: string = '') {
    this.message = message;
    this.messageType = type;
    this.messageTitle = title;
  }

  closePopup() {
    this.message = null;
    this.messageTitle = '';
  }

  canCancel(): boolean {
    if (this.isAdmin) return true;
    return this.modalData.username === this.currentUsername;
  }

  blockReservation(): void {
    if (!this.modalData.sportId || !this.modalData.slot) return;
    const [start, end] = this.modalData.slot.split("-");
    const startTime = `${this.selectedDate}T${start}:00`;
    const endTime = `${this.selectedDate}T${end}:00`;

    const existingReservation = this.reservations.find(r =>
      r.sportId === this.modalData.sportId &&
      r.startTime === startTime &&
      r.endTime === endTime &&
      r.status === 'ACTIVE'
    );

    const blockedReservation = {
      sportId: this.modalData.sportId,
      startTime: startTime,
      endTime: endTime,
    };

    this.reservationService.createBlockedReservation(blockedReservation).subscribe({
      next: (res: Reservation) => {
        this.loadReservations();
        this.closeModal();

        const reservationDate = new Date(startTime.split('T')[0]);
        const formattedDate = `${String(reservationDate.getDate()).padStart(2,'0')}.${String(reservationDate.getMonth()+1).padStart(2,'0')}.${reservationDate.getFullYear()}`;
        const startEnd = `${start}-${end}`;

        const blockerMessage = `You have successfully blocked the appointment ${formattedDate} (${startEnd}).`;
        const currentUserId = this.authService.getCurrentUserId();
        if (currentUserId) {
          this.notificationService.sendNotification(blockerMessage, currentUserId);
        }

        if (existingReservation) {
          const sportName = existingReservation.sportName;

          if (existingReservation.userId) {
            const ownerMessage = `Your reservation for ${sportName} (${startEnd}) days ${formattedDate} is blocked.`;
            this.notificationService.sendNotification(ownerMessage, existingReservation.userId);
          }

          (existingReservation.participants || [])
            .map(username => username.trim())
            .filter(username => username && username !== existingReservation.username)
            .forEach(username => {
              this.authService.getUserIdByUsername(username).subscribe({
                next: userId => {
                  if (userId) {
                    const participantMessage = `Reservation for ${sportName} (${startEnd}) days ${formattedDate} which you attended is blocked.`;
                    this.notificationService.sendNotification(participantMessage, userId);
                  }
                }
              });
            });
        }

        this.showPopup('The appointment has been successfully blocked!', 'success', 'Success');
      },
      error: (err) => {
        this.showPopup('An error occurred while blocking the appointment.', 'error', 'Error');
      }
    });
  }

  openCreateModal(isBlock: boolean = false) {
    this.isBlockMode = isBlock;
    this.isCreateModalOpen = true;

    this.createForm.reset({
      sport: '',
      date: new Date().toISOString().substring(0, 10),
      timeFrom: '',
      timeTo: ''
    });
  }

  closeCreateModal() {
    this.isCreateModalOpen = false;
  }

  createNewReservation() {
    const { sport, date, timeFrom, timeTo, participants, openForJoin } = this.createForm.value;

    if (!sport || !date || !timeFrom || !timeTo) {
      this.showPopup('Please fill in all fields.', 'error', 'Error');
      return;
    }

    const payload = {
      sportId: sport,
      startTime: `${date}T${timeFrom}:00`,
      endTime: `${date}T${timeTo}:00`,
      participants: participants || [],
      openForJoin: !!openForJoin
    };

    this.reservationService.createReservation(payload).subscribe({
      next: () => {
        this.loadReservations();
        this.closeCreateModal();
        this.showPopup('Reservation created successfully!', 'success', 'Success');

        const startEnd = `${timeFrom}-${timeTo}`;
        const sportName = this.sports.find(s => s.id === sport)?.name || '';

        const reservationDate = new Date(date);
        const formattedDate = `${String(reservationDate.getDate()).padStart(2, '0')}.${String(reservationDate.getMonth() + 1).padStart(2, '0')}.${reservationDate.getFullYear()}`;

        const ownerUserId = this.authService.getCurrentUserId();
        if (ownerUserId) {
          const ownerMessage = `Reservation successfully created for ${sportName} (${startEnd}) days ${formattedDate}.`;
          this.notificationService.sendNotification(ownerMessage, ownerUserId);
        }

        if (participants && participants.length) {
          participants.forEach((username: string) => {
            this.authService.getUserIdByUsername(username).subscribe({
              next: (userId) => {
                if (userId) {
                  const participantMessage = `You have been added to the reservation for ${sportName} (${startEnd}) days ${formattedDate}.`;
                  this.notificationService.sendNotification(participantMessage, userId);
                }
              }
            });
          });
        }
      },
      error: (err: any) => {
        const errorMessage = err?.error?.message || 'There is already a reservation in that period.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  createBlock() {
    const { sport, date, timeFrom, timeTo } = this.createForm.value;
    if (!sport || !date || !timeFrom || !timeTo) {
      this.showPopup('Please fill in all fields.', 'error', 'Error');
      return;
    }

    const payload = {
      sportId: sport,
      startTime: `${date}T${timeFrom}:00`,
      endTime: `${date}T${timeTo}:00`
    };

    this.reservationService.createBlockedReservation(payload).subscribe({
      next: () => {
        this.loadReservations();
        this.closeCreateModal();
      },
      error: (err: any) => {
        const errorMessage = err?.error?.message || 'An error occurred while blocking the term.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  isPastSlot(date: string, slot: string): boolean {
    const [start] = slot.split(" ");
    const slotStart = new Date(`${date}T${start}:00`);
    const now = new Date();
    return slotStart <= now;
  }

  removeFromReservation(): void {
    const [start, end] = this.modalData.slot.split("-");
    const reservation = this.reservations.find(r =>
      r.sportName === this.modalData.sportName &&
      r.startTime.includes(start) &&
      r.endTime.includes(end) &&
      r.status === 'ACTIVE'
    );

    if (!reservation) {
      this.closeModal();
      this.loadReservations();
      return;
    }

    this.reservationService.removeFromReservation(reservation.id).subscribe({
      next: () => {
        this.loadReservations();
        this.closeModal();

        const leavingUsername = this.currentUsername;

        const ownerUserId = reservation.userId;
        if (ownerUserId) {
          const ownerMessage = `${leavingUsername} has left your reservation for ${reservation.sportName} (${start}-${end}).`;
          this.notificationService.sendNotification(ownerMessage, ownerUserId);
        }

        const userId = this.authService.getCurrentUserId();
        if (userId) {
          const userMessage = `You have successfully left a reservation for ${reservation.sportName} (${start}-${end}).`;
          this.notificationService.sendNotification(userMessage, userId);
        }

        this.showPopup('You have been removed from the reservation.', 'success', 'Success');
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Error removing from reservation.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  onSportChange(event: any) {
    const sportId = this.createForm.get('sport')?.value;
    this.selectedSportObj = this.sports.find(s => s.id === sportId);

    if (this.selectedSportObj?.playerCount) {
      this.maxParticipants = this.selectedSportObj.playerCount - 1;
      this.remainingParticipants = this.maxParticipants;
    } else {
      this.maxParticipants = 0;
      this.remainingParticipants = 0;
    }

    this.createForm.get('participants')?.setValue([]);
  }

  onParticipantsChange(selected: string[]) {
    const max = this.modalData.maxParticipants || 0;

    if ((selected?.length || 0) > max) {
      this.modalData.participants = selected.slice(0, max);
    } else {
      this.modalData.participants = selected;
    }

    this.modalData.remainingSlots = max - (this.modalData.participants?.length || 0);

    if (this.modalData.remainingSlots === 0) {
      this.modalData.openForJoin = false;
    }
  }

  onCreateParticipantsChange(selected: string[]) {
    const max = this.maxParticipants;

    if ((selected?.length || 0) > max) {
      this.createForm.get('participants')?.setValue(selected.slice(0, max));
    }

    this.remainingParticipants = max - (selected?.length || 0);

    const openForJoinControl = this.createForm.get('openForJoin');
    if (this.remainingParticipants === 0) {
      openForJoinControl?.setValue(false);
      openForJoinControl?.disable();
    } else {
      openForJoinControl?.enable();
    }
  }

  joinReservation(): void {
    const [start, end] = this.modalData.slot.split("-");

    const reservation = this.reservations.find(r =>
      r.sportName === this.modalData.sportName &&
      r.startTime.includes(`${this.selectedDate}T${start}`) &&
      r.endTime.includes(`${this.selectedDate}T${end}`) &&
      r.status === 'ACTIVE'
    );

    if (!reservation) {
      this.showPopup('Reservation not found or is no longer available.', 'error', 'Error');
      this.closeModal();
      this.loadReservations();
      return;
    }

    this.reservationService.joinReservation(reservation.id).subscribe({
      next: () => {
        this.loadReservations();
        this.closeModal();

        const joiningUsername = this.currentUsername;

        const ownerUserId = reservation.userId;
        if (ownerUserId) {
          const ownerMessage = `${joiningUsername} joined your reservation for ${reservation.sportName} (${start}-${end}).`;
          this.notificationService.sendNotification(ownerMessage, ownerUserId);
        }

        const userId = this.authService.getCurrentUserId();
        if (userId) {
          const userMessage = `You have successfully joined the reservation for ${reservation.sportName} (${start}-${end}).`;
          this.notificationService.sendNotification(userMessage, userId);
        }

        this.showPopup('You have successfully joined the reservation!', 'success', 'Success');
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'An error occurred while trying to join.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  filterReservations(): void {
    if (this.showMyReservations) {
      this.filteredReservations = this.reservations.filter(r =>
        r.username === this.currentUsername || r.participants?.includes(this.currentUsername)
      );
    } else {
      this.filteredReservations = [...this.reservations];
    }
  }

  rateReservation(): void {
    if (!this.ratingReservationId) return;

    this.isRatingModalOpen = true;
    this.ratingForm.reset({
      hygiene: 5,
      equipment: 5,
      atmosphere: 5,
      comment: ''
    });
  }

  submitRating(): void {
    if (!this.ratingReservationId) return;

    if (this.ratingForm.invalid) {
      this.showPopup('Please fill in all required fields (1-10).', 'error', 'Error');
      return;
    }

    const payload = {
      hygiene: this.ratingForm.value.hygiene,
      equipment: this.ratingForm.value.equipment,
      atmosphere: this.ratingForm.value.atmosphere,
      comment: this.ratingForm.value.comment
    };

    this.reservationService.rateReservation(this.ratingReservationId, payload).subscribe({
      next: (res) => {
        this.showPopup('Rating submitted successfully!', 'success', 'Success');

        const currentUserId = this.authService.getCurrentUserId();
        if (currentUserId) {
          this.notificationService.sendNotification(
            'You have successfully submitted your rating',
            currentUserId
          );
        }

        this.userHasRated = true;

        this.ratingForm.reset({ hygiene: 5, equipment: 5, atmosphere: 5, comment: '' });

        this.reservationService.getRatings(this.ratingReservationId!).subscribe({
          next: (ratings) => {
            this.reservationRatings = ratings;
            this.showRatings = true;
            this.isRatingModalOpen = false;
          },
          error: (err) => console.error('Error fetching ratings after submit:', err)
        });
      },
      error: (err) => {
        const errorMessage = err?.error?.message || 'Error submitting rating.';
        this.showPopup(errorMessage, 'error', 'Error');
      }
    });
  }

  toggleRatings(): void {
    if (!this.ratingReservationId) return;

    if (this.showRatings) {
      this.showRatings = false;
      return;
    }

    this.reservationService.getRatings(this.ratingReservationId).subscribe({
      next: (ratings) => {
        console.log('Ratings from backend:', ratings);
        this.reservationRatings = ratings;
        this.showRatings = true;
      },
      error: (err) => {
        console.error('Error fetching ratings:', err);
        this.showRatings = false;
      }
    });
  }

  deleteRating(reservationId: string | null) {
    if (!reservationId) return;

    this.reservationIdToDelete = reservationId;
    this.isDeleteModalOpen = true;
  }
  confirmDeleteRating() {
    if (!this.reservationIdToDelete) return;

    this.reservationService.deleteRating(this.reservationIdToDelete).subscribe(() => {
      this.reservationRatings = this.reservationRatings.filter(
        r => r.username !== this.currentUsername
      );

      this.userHasRated = false;
      this.ratingForm.reset({
        hygiene: 5,
        equipment: 5,
        atmosphere: 5,
        comment: ''
      });
      this.showRatings = this.reservationRatings.length >= 0;

      this.showPopup('Rating deleted successfully! You can submit a new rating now.', 'success', 'Success');

      const currentUserId = this.authService.getCurrentUserId();
      if (currentUserId) {
        this.notificationService.sendNotification(
          'Your rating has been deleted successfully',
          currentUserId
        );
      }
      this.closeDeleteModal();
    }, error => {
      console.error("Error deleting rating:", error);
      this.showPopup('Error deleting rating.', 'error', 'Error');
      this.closeDeleteModal();
    });
  }

  closeDeleteModal() {
    this.isDeleteModalOpen = false;
    this.reservationIdToDelete = null;
  }

}
