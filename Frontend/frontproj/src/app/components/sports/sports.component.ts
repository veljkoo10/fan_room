import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Sport } from "../../models/sport.interface";
import {SportService} from "../../services/sport.service";

@Component({
  selector: 'app-sports',
  templateUrl: './sports.component.html',
  styleUrls: ['./sports.component.css']
})
export class SportsComponent implements OnInit {
  @ViewChild('createSportModal') modalRef!: ElementRef;
  @ViewChild('confirmDeleteModal') confirmDeleteModalRef!: ElementRef;

  message: string | null = null;
  popupType: 'success' | 'error' | 'confirm' = 'success';
  popupTitle: string = '';
  sports: Sport[] = [];
  createSportForm!: FormGroup;
  sportToDelete: { id: string, index: number } | null = null;
  isEditMode: boolean = false;
  editingSportId: string | null = null;

  private modalInstance: any;
  private confirmDeleteModalInstance: any;

  constructor(private sportService: SportService, private fb: FormBuilder) { }

  ngOnInit(): void {
    this.loadSports();

    this.createSportForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      playerCount: [null, [Validators.required, Validators.min(1)]]
    });
  }

  ngAfterViewInit(): void {
    this.modalInstance = new (window as any).bootstrap.Modal(this.modalRef.nativeElement);
    this.confirmDeleteModalInstance = new (window as any).bootstrap.Modal(this.confirmDeleteModalRef.nativeElement);
  }

  loadSports(): void {
    this.sportService.getAllSports().subscribe({
      next: (data) => this.sports = data,
      error: (err) => console.error('Error loading sports', err)
    });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.createSportForm.reset();
    this.modalInstance.show();
  }

  closeModal(): void {
    this.modalInstance.hide();
  }

  createSport(): void {
    if (this.createSportForm.invalid) return;

    const sportData = this.createSportForm.value;

    this.sportService.createSport(sportData).subscribe({
      next: (sport) => {
        this.sports.push(sport);
        this.closeModal();
        this.createSportForm.reset();
      },
      error: (err) => {
        this.message = err.error?.message || 'Failed to create sport.';
        this.popupType = 'error';
        this.popupTitle = 'Error!';
      }
    });
  }

  openEditModal(sport: Sport): void {
    this.isEditMode = true;
    this.editingSportId = sport.id;
    this.createSportForm.patchValue({
      name: sport.name,
      description: sport.description,
      playerCount: sport.playerCount
    });
    this.modalInstance.show();
  }

  editSport(): void {
    if (!this.editingSportId || this.createSportForm.invalid) return;

    const updatedSportData = this.createSportForm.value;

    this.sportService.updateSport(this.editingSportId, updatedSportData).subscribe({
      next: (updatedSport) => {
        const index = this.sports.findIndex(s => s.id === this.editingSportId);
        if (index !== -1) this.sports[index] = updatedSport;

        if (updatedSport.message) {
          this.message = updatedSport.message;
          this.popupType = 'success';
          this.popupTitle = 'Info';
        }

        this.closeModalAndReset();
      },
      error: (err) => {
        this.message = err.error?.message || 'Failed to update sport.';
        this.popupType = 'error';
        this.popupTitle = 'Error!';
      }
    });
  }

  deleteSport(id: string, index: number): void {
    this.sportToDelete = { id, index };
    this.message = 'Are you sure you want to delete this sport and all its reservations?';
    this.popupType = 'confirm';
    this.popupTitle = 'Confirm';
    this.confirmDeleteModalInstance.show();
  }

  confirmDelete(): void {
    if (!this.sportToDelete) return;

    const { id, index } = this.sportToDelete;

    this.sportService.deleteSport(id).subscribe({
      next: () => {
        this.sports.splice(index, 1);
        this.message = 'The sport was successfully deleted.';
        this.popupType = 'success';
        this.popupTitle = 'Success!';
        this.sportToDelete = null;
        this.confirmDeleteModalInstance.hide();
      },
      error: (err) => {
        this.message = err.error?.message || 'Failed to delete sport.';
        this.popupType = 'error';
        this.popupTitle = 'Error!';
        this.sportToDelete = null;
        this.confirmDeleteModalInstance.hide();
      }
    });
  }

  closePopup(): void {
    this.message = null;
    this.sportToDelete = null;
    if (this.popupType === 'confirm') this.confirmDeleteModalInstance.hide();
  }

  private closeModalAndReset(): void {
    this.modalInstance.hide();
    this.createSportForm.reset();
    this.isEditMode = false;
    this.editingSportId = null;
  }

  resetForm(): void {
    this.createSportForm.reset();
    this.isEditMode = false;
    this.editingSportId = null;
  }

}
