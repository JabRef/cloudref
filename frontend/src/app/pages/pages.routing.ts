import { Routes, RouterModule }  from '@angular/router';
import { Pages } from './pages.component';
import { ModuleWithProviders } from '@angular/core';
import { AuthGuard } from '../guards/auth.guard';
// noinspection TypeScriptValidateTypes

// export function loadChildren(path) { return System.import(path); };

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: 'app/pages/login/login.module#LoginModule',
  },
  {
    path: 'register',
    loadChildren: 'app/pages/register/register.module#RegisterModule',
  },
  {
    path: '',
    component: Pages, canActivate: [AuthGuard],
    children: [
      { path: '', redirectTo: 'references', pathMatch: 'full' },
      { path: 'import', loadChildren: './imports/imports.module#ImportsModule' },
      { path: 'references', loadChildren: './references/references.module#ReferencesModule' },
    ],
  },
];

export const routing: ModuleWithProviders = RouterModule.forChild(routes);
