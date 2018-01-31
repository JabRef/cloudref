import { Component, OnDestroy, ViewChild, ElementRef, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DefaultApi } from '../../../../../../gen/api/DefaultApi';
import { Http } from '@angular/http';
import { BibTeXEntry } from '../../../../../../gen/model/BibTeXEntry';
import { Subscription } from 'rxjs/Subscription';
import { Rating } from '../../../../../../gen/model/Rating';
import UserRatingEnum = Rating.UserRatingEnum;
import { BracesValidator } from '../../../../../../theme/validators/braces.validator';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { getErrorMessage } from '../../../../../../shared/errorHandler';

@Component({
  selector: 'reference-view',
  templateUrl: './referenceview.html',
  styleUrls: ['./referenceview.scss'],
})

export class ReferenceViewComponent implements OnInit, OnDestroy {

  types = [
    'article',
    'book',
    'booklet',
    'conference',
    'inbook',
    'incollection',
    'inproceedings',
    'manual',
    'mastersthesis',
    'misc',
    'phdthesis',
    'proceedings',
    'techreport',
    'unpublished',
    'standard',
  ];

  article = [
    // required fields
    'title',
    'author',
    'journal',
    'year',
  ];

  articleOptional = [
    // optional fields
    'volume',
    'pages',
    'issn',
    'number',
    'month',
    'note',
  ];

  book = [
    // required fields
    'title',
    'publisher',
    'year',
    'author',
    'editor',
  ];

  bookOptional = [
    // optional fields
    'volume',
    'isbn',
    'number',
    'series',
    'address',
    'edition',
    'month',
    'note',
  ];

  inbook = [
    // required fields
    'title',
    'chapter',
    'pages',
    'publisher',
    'year',
    'author',
    'editor',
  ];

  inbookOptional = [
    // optional fields
    'volume',
    'isbn',
    'number',
    'series',
    'type',
    'address',
    'edition',
    'month',
    'note',
  ];

  incollection = [
    // required fields
    'title',
    'booktitle',
    'publisher',
    'year',
    'author',
  ];

  incollectionOptional = [
    // optional fields
    'editor',
    'volume',
    'isbn',
    'number',
    'series',
    'type',
    'chapter',
    'pages',
    'address',
    'edition',
    'month',
    'note',
  ];

  inproceedings = [
    // required fields
    'title',
    'booktitle',
    'year',
    'author',
  ];

  inproceedingsOptional = [
    // optional fields
    'editor',
    'volume',
    'number',
    'series',
    'pages',
    'address',
    'month',
    'organization',
    'publisher',
    'note',
  ];

  manual = [
    // required fields
    'title',
  ];

  manualOptional = [
    // optional fields
    'author',
    'address',
    'edition',
    'month',
    'organization',
    'year',
    'isbn',
    'note',
  ];

  miscOptional = [
    // optional fields
    'author',
    'title',
    'howpublished',
    'month',
    'year',
    'note',
  ];

  phdthesis = [
    // required fields
    'author',
    'title',
    'school',
    'year',
  ];

  phdthesisOptional = [
    // optional fields
    'type',
    'address',
    'month',
    'note',
  ];

  standard = [
    // required fields
    'title',
    'organization',
    'institution',
  ];

  standardOptional = [
    // optional fields
    'author',
    'language',
    'howpublished',
    'type',
    'number',
    'revision',
    'address',
    'month',
    'year',
    'url',
    'note',
  ];

  techreport = [
    // required fields
    'author',
    'title',
    'year',
    'institution',
  ];

  techreportOptional = [
    // optional fields
    'type',
    'number',
    'address',
    'month',
    'note',
  ];

  booklet = [
    // required fields
    'title',
  ];

  bookletOptional = [
    // optional fields
    'author',
    'howpublished',
    'address',
    'month',
    'year',
    'note',
  ];

  conference = [
    // required fields
    'title',
    'booktitle',
    'year',
    'author',
  ];

  conferenceOptional = [
    // optional fields
    'editor',
    'volume',
    'number',
    'series',
    'pages',
    'address',
    'month',
    'organization',
    'publisher',
    'note',
  ];

  mastersthesis = [
    // required fields
    'author',
    'title',
    'school',
    'year',
  ];

  mastersthesisOptional = [
    // optional fields
    'type',
    'address',
    'month',
    'note',
  ];

  proceedings = [
    // required fields
    'title',
    'year',
  ];

  proceedingsOptional = [
    // optional fields
    'editor',
    'volume',
    'number',
    'series',
    'address',
    'publisher',
    'month',
    'organization',
    'isbn',
    'note',
  ];

  unpublished = [
    // required fields
    'author',
    'title',
    'note',
  ];

  unpublishedOptional = [
    // optional fields
    'month',
    'year',
  ];

  id: string;
  private sub: any;
  addFile: Function;

  saved: boolean = false;
  errorUpload: boolean = false;
  errorNoFile: boolean = false;
  savedCounter: number = 0;
  errorUploadCounter: number = 0;
  errorNoFileCounter: number = 0;

  userRatingEnum = UserRatingEnum;
  ratingUser: UserRatingEnum;
  ratingReference: number;
  confirmedReference: boolean = false;

  httpErrorMsg = null;
  httpErrorMsgFile = null;

  reference: BibTeXEntry = {
    '@class': 'org.jbibtex.BibTeXEntry',
    type: {
      value: 'article',
    },
    key: {
      value: '',
    },
    fields: {
      title: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      author: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      journal: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      publisher: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      editor: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      chapter: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      pages: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      booktitle: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      school: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      organization: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      institution: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      month: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      year: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      crossrefString: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      keywords: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      Pdf: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      doi: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      url: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      comment: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      volume: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      issn: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      number: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      isbn: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      series: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      address: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      edition: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      type: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      howpublished: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      language: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      revision: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      note: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      abstract: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
      review: {
        string: '',
        '@class': 'org.jbibtex.StringValue',
        style: 'BRACED',
      },
    },
    // crossReference: null,
  };

  changedType: boolean = false;

  setChangedType() {
    this.changedType = true;
  }

  containsField(fieldName, tab) {
    switch (this.reference.type.value) {
      case 'article':
        if (tab === 'required' && this.article.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.articleOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'book':
        if (tab === 'required' && this.book.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.bookOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'inbook':
        if (tab === 'required' && this.inbook.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.inbookOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'incollection':
        if (tab === 'required' && this.incollection.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.incollectionOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'inproceedings':
        if (tab === 'required' && this.inproceedings.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.inproceedingsOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'manual':
        if (tab === 'required' && this.manual.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.manualOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'misc':
        if (tab === 'optional' && this.miscOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'phdthesis':
        if (tab === 'required' && this.phdthesis.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.phdthesisOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'standard':
        if (tab === 'required' && this.standard.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.standardOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'techreport':
        if (tab === 'required' && this.techreport.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.techreportOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'booklet':
        if (tab === 'required' && this.booklet.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.bookletOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'conference':
        if (tab === 'required' && this.conference.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.conferenceOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'mastersthesis':
        if (tab === 'required' && this.mastersthesis.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.mastersthesisOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'proceedings':
        if (tab === 'required' && this.proceedings.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.proceedingsOptional.includes(fieldName)) {
          return true;
        }
        break;
      case 'unpublished':
        if (tab === 'required' && this.unpublished.includes(fieldName)) {
          return true;
        } else if (tab === 'optional' && this.unpublishedOptional.includes(fieldName)) {
          return true;
        }
        break;
    }
    return false;
  }

  @Input() defaultValue: string = '';
  @ViewChild('fileInput') fileInput: ElementRef;
  @Input('loadReferences') loadReferences: boolean = false;
  @Input('loadSuggestion') loadSuggestion: boolean = false;
  loading: boolean = true;
  suggestionId: number;

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

  formTabRequired: FormGroup;
  formTabOptional: FormGroup;
  formTabGeneral: FormGroup;
  formTabAbstract: FormGroup;
  formTabReview: FormGroup;
  errorMessageBraces: string = 'Braces do not match! Please adjust your text.';
  errorMessageNumbers: string = 'No valid input! Only numbers are allowed.';

  constructor(private router: Router, private route: ActivatedRoute, private api: DefaultApi, protected http: Http) {

    this.formTabRequired = new FormGroup({
      'bibtexid': new FormControl('', [Validators.pattern('[a-zA-Z-0-9]*')]),
      'chapter': new FormControl('', [BracesValidator.validate]),
      'pages': new FormControl('', [BracesValidator.validate]),
      'title': new FormControl('', [BracesValidator.validate]),
      'booktitle': new FormControl('', [BracesValidator.validate]),
      'publisher': new FormControl('', [BracesValidator.validate]),
      'journal': new FormControl('', [BracesValidator.validate]),
      'school': new FormControl('', [BracesValidator.validate]),
      'year': new FormControl('', [Validators.pattern('[1-9][0-9]*')]),
      'author': new FormControl('', [BracesValidator.validate]),
      'editor': new FormControl('', [BracesValidator.validate]),
      'organization': new FormControl('', [BracesValidator.validate]),
      'institution': new FormControl('', [BracesValidator.validate]),
      'note': new FormControl('', [BracesValidator.validate]),
    });

    this.formTabOptional = new FormGroup({
      'author': new FormControl('', [BracesValidator.validate]),
      'title': new FormControl('', [BracesValidator.validate]),
      'language': new FormControl('', [BracesValidator.validate]),
      'editor': new FormControl('', [BracesValidator.validate]),
      'volume': new FormControl('', [BracesValidator.validate]),
      'number': new FormControl('', [BracesValidator.validate]),
      'revision': new FormControl('', [BracesValidator.validate]),
      'series': new FormControl('', [BracesValidator.validate]),
      'type': new FormControl('', [BracesValidator.validate]),
      'chapter': new FormControl('', [BracesValidator.validate]),
      'pages': new FormControl('', [BracesValidator.validate]),
      'organization': new FormControl('', [BracesValidator.validate]),
      'address': new FormControl('', [BracesValidator.validate]),
      'edition': new FormControl('', [BracesValidator.validate]),
      'howpublished': new FormControl('', [BracesValidator.validate]),
      'month': new FormControl('', [BracesValidator.validate]),
      'year': new FormControl('', [Validators.pattern('[1-9][0-9]*')]),
      'publisher': new FormControl('', [BracesValidator.validate]),
      'issn': new FormControl('', [BracesValidator.validate]),
      'isbn': new FormControl('', [BracesValidator.validate]),
      'note': new FormControl('', [BracesValidator.validate]),
      'url': new FormControl('', [BracesValidator.validate]),
    });

    this.formTabGeneral = new FormGroup({
      'crossref': new FormControl('', [Validators.pattern('[a-zA-Z-0-9]*')]),
      'keywords': new FormControl('', [BracesValidator.validate]),
      'doi': new FormControl('', [BracesValidator.validate]),
      'url': new FormControl('', [BracesValidator.validate]),
      'comment': new FormControl('', [BracesValidator.validate]),
    });

    this.formTabAbstract = new FormGroup({
      'abstract': new FormControl('', [BracesValidator.validate]),
    });

    this.formTabReview = new FormGroup({
      'review': new FormControl('', [BracesValidator.validate]),
    });

    this.sub = this.route.params.subscribe(params => {
      this.reference.key.value = params['id'];
      this.suggestionId = Number(params['idSuggestion']);
    });


    this.addFile = function () {
      const fi = this.fileInput.nativeElement;
      if (fi.files && fi.files[0]) {
        const fileToUpload = fi.files[0];

        this.api.savePdfFile(this.reference.key.value, fileToUpload).subscribe(
          data => {
            if (!this.loadReferences) {
              // navigate user to new reference
              this.router.navigate(['references', this.reference.key.value]);
            } else {
              this.reference.fields.Pdf = {
                string: 'true',
                type: 'org.jbibtex.StringValue',
                style: 'BRACED',
              };
              // saved file, reset form and show success to user
              var form = <HTMLFormElement> document.getElementById('fileUploadFrom');
              form.reset();
              this.defaultValue = '';
              this.savedCounter++;
              this.saved = true;
              setTimeout(() => {
                this.savedCounter--;
                if (this.savedCounter === 0) {
                  this.saved = false;
                }
              }, 5000);
            }
          },
          error => {
            // get error message for user
            this.httpErrorMsgFile = getErrorMessage(error);

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
        if (!this.loadReferences) {
          // navigate user to new reference
          this.router.navigate(['references', this.reference.key.value]);
        } else {
          // show alert if no file is selected
          if (this.loadReferences) {
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
    };
  }

  existsKeyAlreadyATBackend(key: string, crossref: boolean) {
    this.api.getReference(key).subscribe(val => {
        if (!crossref) {
          this.keyExists = true;
        } else {
          this.crossrefExists = true;
        }
      },
      (err) => {
        if (err.status === 404) {
          if (!crossref) {
            this.keyExists = false;
          } else {
            this.crossrefExists = false;
          }
        } else {
          this.errorHandler(err);
        }
      },
    );
  }

  pdfView() {
    this.router.navigate(['references', this.reference.key.value, 'pdf', 'comments']);
  }

  viewSuggestions() {
    this.router.navigate(['references', this.reference.key.value, 'suggestions']);
  }

  keyExists: boolean = false;
  crossrefExists: boolean = true;

  validateBibTeXKey() {

    if (this.formTabRequired.controls['bibtexid'].valid) {
      // check if bibtexkey exists at backend
      this.existsKeyAlreadyATBackend(this.reference.key.value, false);
    }
  }

  existsCrossref(crossref: string) {
    if (this.formTabGeneral.controls['crossref'].valid) {
      this.existsKeyAlreadyATBackend(crossref, true);
    } else {
      // if entered key is not valid it cannot exist at backend
      this.crossrefExists = false;
    }
  }

  private subscription: Subscription;

  saveSuggestion() {
    // check if form content is valid
    if (this.formTabRequired.valid && this.formTabOptional.valid && this.formTabGeneral.valid &&
      this.formTabAbstract.valid && this.formTabReview.valid) {
      if (!this.loadSuggestion) {
        // save suggestion
        this.api.saveSuggestion(this.reference.key.value, this.reference).subscribe(
          data => {
            // navigate user to suggestions
            this.viewSuggestions();
          },
          (err) => {
            this.errorHandler(err);
          },
        );
      } else {
        // update suggestion
        this.api.updateSuggestion(this.reference.key.value, this.suggestionId, this.reference).subscribe(
          data => {
            // navigate user to suggestions
            this.viewSuggestions();
          },
          (err) => {
            this.errorHandler(err);
          },
        );
      }
    } else {
      // jump to tab to show invalid input
      this.jumpToInvalidContent();
    }
  }

  save() {
    // check if form content is valid
    if (!this.keyExists && this.formTabRequired.valid && this.formTabOptional.valid && this.formTabGeneral.valid &&
      this.formTabAbstract.valid && this.formTabReview.valid) {
      // call api to save new reference
      this.subscription = this.api.saveReference(this.reference.key.value, this.reference).subscribe(
        data => {
          // try to upload file if new reference is saved
          this.addFile();
        },
        (err) => {
          this.errorHandler(err);
        },
      );
    } else {
      // jump to tab to show invalid input
      this.jumpToInvalidContent();
    }
  }

  jumpToInvalidContent() {
    // jump to tab to show invalid input
    if (!this.formTabRequired.valid) {
      let link = <HTMLAnchorElement> document.getElementsByClassName('nav-link')[0];
      link.click();
    } else if (!this.formTabOptional.valid) {
      let link = <HTMLAnchorElement> document.getElementsByClassName('nav-link')[1];
      link.click();
    } else if (!this.formTabGeneral.valid) {
      let link = <HTMLAnchorElement> document.getElementsByClassName('nav-link')[2];
      link.click();
    } else if (!this.formTabAbstract.valid) {
      let link = <HTMLAnchorElement> document.getElementsByClassName('nav-link')[3];
      link.click();
    } else if (!this.formTabReview.valid) {
      let link = <HTMLAnchorElement> document.getElementsByClassName('nav-link')[4];
      link.click();
    }
  }

  rateReference(rating: UserRatingEnum) {
    // check if rating changed
    if (this.ratingUser != rating) {
      let rate: Rating = {
        userRating: rating,
      };
      this.api.rateReference(this.reference.key.value, rate).subscribe(val => {
          // change overall rating
          this.ratingReference = val.overallRating;
          // change rating of user
          this.ratingUser = rating;
          // set confirmed
          this.confirmedReference = val.confirmed;
        },
        (err) => {
          this.errorHandler(err);
        },
      );
    }
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  ngOnInit() {
    if (this.loadReferences) {
      // get references from backend
      this.api.getReference(this.reference.key.value).subscribe(val => {

          // get ratings
          if (val.fields.Confirmed != null) {
            var s = JSON.stringify(val.fields.Confirmed);
            if (s.indexOf('true') >= 0) {
              this.confirmedReference = true;
            } else {
              this.confirmedReference = false;
            }
          }
          if (val.fields.RatedByUser != null) {
            var s = JSON.stringify(val.fields.RatedByUser);
            if (s.indexOf('POSITIVE') >= 0) {
              this.ratingUser = UserRatingEnum.POSITIVE;
            } else if (s.indexOf('NEGATIVE') >= 0) {
              this.ratingUser = UserRatingEnum.NEGATIVE;
            }
          }
          if (val.fields.OverallRating != null) {
            let s = JSON.stringify(val.fields.OverallRating);
            if (s.indexOf('string') >= 0) {
              let sub = s.substring(s.indexOf('string'), s.length - 1);
              // get number from JSON
              let array = sub.split('"');
              if (array[2] != '') {
                this.ratingReference = Number(array[2]);
              }
            }
          }

          // update reference fields
          this.updateReferenceValues(val);
        },
        (err) => {
          this.errorHandler(err);
        },
      );
    } else if (this.loadSuggestion) {
      this.api.getSuggestion(this.reference.key.value, this.suggestionId).subscribe(val => {
          // update reference fields
          this.updateReferenceValues(val);
        },
        (err) => {
          this.errorHandler(err);
        },
      );
    }
  }

  updateReferenceValues(val: any) {
    // get reference
    this.reference.type = val.type;
    this.reference.key = val.key;

    if (val.fields.title != null) {
      this.reference.fields.title = val.fields.title;
    }
    if (val.fields.author != null) {
      this.reference.fields.author = val.fields.author;
    }
    if (val.fields.journal != null) {
      this.reference.fields.journal = val.fields.journal;
    }
    if (val.fields.year != null) {
      this.reference.fields.year = val.fields.year;
    }
    if (val.fields.publisher != null) {
      this.reference.fields.publisher = val.fields.publisher;
    }
    if (val.fields.editor != null) {
      this.reference.fields.editor = val.fields.editor;
    }
    if (val.fields.chapter != null) {
      this.reference.fields.chapter = val.fields.chapter;
    }
    if (val.fields.pages != null) {
      this.reference.fields.pages = val.fields.pages;
    }
    if (val.fields.booktitle != null) {
      this.reference.fields.booktitle = val.fields.booktitle;
    }
    if (val.fields.school != null) {
      this.reference.fields.school = val.fields.school;
    }
    if (val.fields.organization != null) {
      this.reference.fields.organization = val.fields.organization;
    }
    if (val.fields.institution != null) {
      this.reference.fields.institution = val.fields.institution;
    }
    if (val.fields.note != null) {
      this.reference.fields.note = val.fields.note;
    }
    if (val.fields.abstract != null) {
      this.reference.fields.abstract = val.fields.abstract;
    }
    if (val.fields.review != null) {
      this.reference.fields.review = val.fields.review;
    }
    if (val.fields.crossrefString != null) {
      this.reference.fields.crossrefString = val.fields.crossrefString;
    }
    if (val.fields.keywords != null) {
      this.reference.fields.keywords = val.fields.keywords;
    }
    if (val.fields.Pdf != null) {
      this.reference.fields.Pdf = val.fields.Pdf;
    }
    if (val.fields.doi != null) {
      this.reference.fields.doi = val.fields.doi;
    }
    if (val.fields.url != null) {
      this.reference.fields.url = val.fields.url;
    }
    if (val.fields.comment != null) {
      this.reference.fields.comment = val.fields.comment;
    }
    if (val.fields.volume != null) {
      this.reference.fields.volume = val.fields.volume;
    }
    if (val.fields.number != null) {
      this.reference.fields.number = val.fields.number;
    }
    if (val.fields.issn != null) {
      this.reference.fields.issn = val.fields.issn;
    }
    if (val.fields.month != null) {
      this.reference.fields.month = val.fields.month;
    }
    if (val.fields.isbn != null) {
      this.reference.fields.isbn = val.fields.isbn;
    }
    if (val.fields.series != null) {
      this.reference.fields.series = val.fields.series;
    }
    if (val.fields.address != null) {
      this.reference.fields.address = val.fields.address;
    }
    if (val.fields.edition != null) {
      this.reference.fields.edition = val.fields.edition;
    }
    if (val.fields.type != null) {
      this.reference.fields.type = val.fields.type;
    }
    if (val.fields.howpublished != null) {
      this.reference.fields.howpublished = val.fields.howpublished;
    }
    if (val.fields.language != null) {
      this.reference.fields.language = val.fields.language;
    }
    if (val.fields.revision != null) {
      this.reference.fields.revision = val.fields.revision;
    }

    // finished loading
    this.loading = false;
  }
}
