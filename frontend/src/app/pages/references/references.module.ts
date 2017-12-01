import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { NgxDatatableModule } from '@swimlane/ngx-datatable';

import { routing } from './references.routing';
import { NgaModule } from '../../theme/nga.module';

import { ReferenceComponent } from './components/reference/reference.component';
import { ReferencesComponent } from './references.component';
import { PdfViewComponent } from './components/reference/components/pdf/pdf.component';
import { SharedModule } from '../../shared/shared.module';
import { NgbPopoverModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { SuggestionsComponent } from './components/reference/components/suggestions/suggestions.component';
import { SuggestionComponent } from './components/reference/components/suggestions/components/suggestion.component';
import { SearchPipe } from '../../theme/pipes/search/search.pipe';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    NgaModule,
    routing,
    SharedModule,
    NgbPopoverModule,
    NgxDatatableModule,
    NgbTooltipModule,
  ],
  declarations: [
    ReferencesComponent,
    ReferenceComponent,
    PdfViewComponent,
    SuggestionsComponent,
    SuggestionComponent,
    SearchPipe,
  ],
})
export class ReferencesModule {}
