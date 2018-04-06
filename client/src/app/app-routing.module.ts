import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';
import { CdgComponent } from './cdg/cdg.component';

import { HomeComponent } from './pages/home/home.component';
const appRoutes: Routes = [
  {path: "", component:HomeComponent},
  {path: 'cdg', component: CdgComponent},

];

@NgModule({
  imports: [
    RouterModule.forRoot(
    appRoutes
    ),
    CommonModule
  ],
  exports:[RouterModule]
})
export class AppRoutingModule { }
