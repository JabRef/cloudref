import { NgModule } from '@angular/core';
import { ReferenceViewComponent } from '../pages/references/components/reference/components/referenceview/referenceview.component';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { HighlightPipe } from "./HighlightPipe";

@NgModule({
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgbTooltipModule],
  declarations: [ReferenceViewComponent, HighlightPipe],
  exports: [ReferenceViewComponent, CommonModule, FormsModule, HighlightPipe],
})

export class SharedModule { }
