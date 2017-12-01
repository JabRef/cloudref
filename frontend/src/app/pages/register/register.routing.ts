import { Routes, RouterModule }  from '@angular/router';

import { Register } from './register.component';
import { AuthGuard } from '../../guards/auth.guard';

// noinspection TypeScriptValidateTypes
const routes: Routes = [
  {
    path: '',
    component: Register, canActivate: [AuthGuard],
  },
];

export const routing = RouterModule.forChild(routes);
