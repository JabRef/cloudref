import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule as AngularFormsModule } from '@angular/forms';
import { AppTranslationModule } from '../../app.translation.module';
import { NgaModule } from '../../theme/nga.module';
import { NgbRatingModule } from '@ng-bootstrap/ng-bootstrap';

import { routing } from './imports.routing';

import { ImportComponent } from './imports.component';
import { ImportManuallyComponent } from './components/manually';
import { ImportFileComponent } from './components/file';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  imports: [
    CommonModule,
    AngularFormsModule,
    AppTranslationModule,
    NgaModule,
    NgbRatingModule,
    routing,
    SharedModule,
  ],
  declarations: [
    ImportComponent,
    ImportManuallyComponent,
    ImportFileComponent,
  ],
})
export class ImportsModule {
}
