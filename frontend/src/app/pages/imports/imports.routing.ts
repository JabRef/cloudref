import { Routes, RouterModule } from '@angular/router';

import { ImportComponent } from './imports.component';
import { ImportManuallyComponent } from './components/manually/manually.component';
import { ImportFileComponent } from './components/file/file.component';

const routes: Routes = [
  {
    path: '',
    children: [
      { path: '', component: ImportComponent, data: { title: 'Add new references' } },
      { path: 'manually', component: ImportManuallyComponent, data: { title: 'Add new reference manually' }  },
      { path: 'file', component: ImportFileComponent, data: { title: 'Import references from file' }  },
    ],
  },
];

export const routing = RouterModule.forChild(routes);
