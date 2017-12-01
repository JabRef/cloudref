import { NgModule } from '@angular/core';
import { ReferenceViewComponent } from '../pages/references/components/reference/components/referenceview/referenceview.component';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';

@NgModule({
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgbTooltipModule],
  declarations: [ReferenceViewComponent],
  exports: [ReferenceViewComponent, CommonModule, FormsModule],
})

export class SharedModule { }
