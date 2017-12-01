import { Routes, RouterModule } from '@angular/router';

import { Login } from './login.component';
import { ModuleWithProviders } from '@angular/core';
import { AuthGuard } from '../../guards/auth.guard';

// noinspection TypeScriptValidateTypes
export const routes: Routes = [
  {
    path: '',
    component: Login, canActivate: [AuthGuard],
  },
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
