import { Routes, RouterModule } from '@angular/router';

import { ReferencesComponent } from './references.component';
import { ReferenceComponent } from './components/reference/reference.component';
import { PdfViewComponent } from './components/reference/components/pdf/pdf.component';
import { SuggestionsComponent } from './components/reference/components/suggestions/suggestions.component';
import { SuggestionComponent } from './components/reference/components/suggestions/components/suggestion.component';

const routes: Routes = [
  {
    path: '',
    component: ReferencesComponent,
    data: { title: 'All references' },
  },
  {
    path: ':id',
    children: [
      {
        path: '',
        component: ReferenceComponent,
        data: { title: 'Bibliographical reference' },
      },
      {
        path: 'suggestions',
        children: [
          {
            path: '',
            component: SuggestionsComponent,
            data: { title: 'Suggestions for modification' },
          },
          {
            path: ':idSuggestion',
            component: SuggestionComponent,
            data: { title: 'Suggestion for modification' },
          },
        ],
      },
      {
        path: 'pdf',
        children: [
          {
            path: 'comments',
            component: PdfViewComponent,
            data: { title: 'PDF and Comments' },
          },
        ],
      },
    ],
  },
];

export const routing = RouterModule.forChild(routes);
