import { Component, OnInit } from '@angular/core';
import { ChartData, ChartType } from 'chart.js';
import {SportService} from "../../services/sport.service";

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements OnInit {
  public pieChartType: ChartType = 'pie';

  public pieChartData: ChartData<'pie', number[], string | string[]> = {
    labels: [],
    datasets: [ {
      data: [],
      backgroundColor: [ '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40' ],
      hoverBackgroundColor: [ '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF', '#FF9F40' ]
    } ]
  };

  public pieChartOptions = {
    responsive: true,
  };
  public pieChartLegend = true;
  public pieChartPlugins = [];

  public isLoading = true;

  constructor(private sportService: SportService) { }

  ngOnInit(): void {
    this.fetchStatistics();
  }

  fetchStatistics(): void {
    this.isLoading = true;
    this.sportService.getSportStatistics().subscribe({
      next: (stats) => {

        const labels = stats.map(stat => stat.sportName);
        const data = stats.map(stat => stat.reservationCount);

        this.pieChartData.labels = labels;
        this.pieChartData.datasets[0].data = data;

        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
      }
    });
  }
}
