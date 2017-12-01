import { Component, ElementRef, Input, ViewChild } from '@angular/core';
import { Http } from '@angular/http';
import { Router } from '@angular/router';
import { getErrorMessage } from '../../../../shared/errorHandler';
import { DefaultApi } from '../../../../gen/api/DefaultApi';

@Component({
  selector: 'file',
  templateUrl: './file.html',
})
export class ImportFileComponent {

  @Input() defaultValue: string = '';
  @ViewChild('fileInput') fileInput: ElementRef;

  errorUpload: boolean = false;
  errorNoFile: boolean = false;
  errorUploadCounter: number = 0;
  errorNoFileCounter: number = 0;
  httpErrorMsg = null;

  loading: boolean = false;

  constructor(private router: Router, protected http: Http, private api: DefaultApi) {
  }

  showFileSelector(): boolean {
    this.fileInput.nativeElement.click();
    return false;
  }

  setFileNameAtInput(): void {
    const fi = this.fileInput.nativeElement;
    if (fi.files && fi.files[0]) {
      this.defaultValue = fi.files[0].name;
    } else {
      this.defaultValue = '';
    }
  }

  addFile() {
    const fi = this.fileInput.nativeElement;
    if (fi.files && fi.files[0]) {
      const fileToUpload = fi.files[0];

      this.loading = null;
      setTimeout(() => {
        if (this.loading === null) {
          this.loading = true;
        }
      }, 20);

      // send file with references to backend
      this.api.saveReferences(fileToUpload).subscribe(
        data => {
          // navigate user to new reference
          this.router.navigate(['references']);
        },
        error => {
          // get error message for user
          this.httpErrorMsg = getErrorMessage(error);

          this.loading = false;
          this.errorUploadCounter++;
          this.errorUpload = true;
          setTimeout(() => {
            this.errorUploadCounter--;
            if (this.errorUploadCounter === 0) {
              this.errorUpload = false;
            }
          }, 5000);
        },
      );
    } else {
      // show alert if no file is selected
      this.errorNoFileCounter++;
      this.errorNoFile = true;
      setTimeout(() => {
        this.errorNoFileCounter--;
        if (this.errorNoFileCounter === 0) {
          this.errorNoFile = false;
        }
      }, 5000);

    }
  }

}
