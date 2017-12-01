import { Component, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { DefaultApi } from '../../gen/api/DefaultApi';
import { DatatableComponent } from '@swimlane/ngx-datatable';
import { getErrorMessage } from '../../shared/errorHandler';

@Component({
  selector: 'references',
  templateUrl: './references.html',
  styleUrls: ['./references.scss'],
})

export class ReferencesComponent {

  selected = [];
  openReferenceOfClickedRow: Function;
  references: any[];
  temp: any[];
  loadedReferences: boolean = false;
  @ViewChild(DatatableComponent) table: DatatableComponent;
  searchInputValue: string;
  httpErrorMsg = null;

  constructor(private router: Router, private api: DefaultApi) {

    // get references from backend
    this.api.getReferences().subscribe(val => {
        // map author and editor to one field
        // map journal and booktitle to one field
        this.references = val.map(reference => ({
          pdf: reference.pdf,
          confirmed: reference.confirmed,
          title: reference.title,
          type: reference.type,
          authorEditor: reference.author != null ? reference.author : reference.editor,
          year: reference.year === 0 ? '' : reference.year.toString(),
          journalBooktitle: reference.journal != null ? reference.journal : reference.booktitle,
          bibtexkey: reference.bibtexkey,
        }));

        // cache references for search
        this.temp = [...this.references];

        // remove brackets around title
        this.references.forEach(function (currentValue, index, array) {
          let tempTitle = currentValue.title;
          // remove surrounding brackets
          if (tempTitle.startsWith('{') && tempTitle.endsWith('}')) {
            tempTitle = tempTitle.substring(1, tempTitle.length - 1);
          }
          array[index].title = tempTitle;
        });

        this.loadedReferences = true;
      },
      (err) => {
        this.errorHandler(err);
      });

    // navigate to page of selected reference
    this.openReferenceOfClickedRow = function (reference) {
      this.router.navigate(['references', reference.bibtexkey]);
    };
  }

  updateFilter() {
    const val = this.searchInputValue.toLowerCase();

    // filter references
    const temp = this.temp.filter(function (d) {
      if ((d.title != null && d.title.toLowerCase().indexOf(val) !== -1) ||
        (d.authorEditor != null && d.authorEditor.toLowerCase().indexOf(val) !== -1) ||
        (d.type != null && d.type.toLowerCase().indexOf(val) !== -1) ||
        (d.year != null && d.year.toLowerCase().indexOf(val) !== -1) ||
        (d.journalBooktitle != null && d.journalBooktitle.toLowerCase().indexOf(val) !== -1) ||
        (d.bibtexkey != null && d.bibtexkey.toLowerCase().indexOf(val) !== -1)) {
        // search term found
        return true;
      } else {
        // search term not found
        return false;
      }
    });

    // update references
    this.references = temp;

    if (this.table != null) {
      // Whenever the filter changes, always go back to the first page
      this.table.offset = 0;
    }
  }

  getRowClass(row) {
    return {
      'confirmed': row.confirmed,
    };
  }

  onSelect() {
    this.openReferenceOfClickedRow((this.selected[0]));
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }
}
