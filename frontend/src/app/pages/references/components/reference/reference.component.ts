import { Component, OnDestroy, ViewChild, ElementRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Http } from '@angular/http';

@Component({
  selector: ':id',
  templateUrl: './reference.html',
})

export class ReferenceComponent implements OnDestroy {

  id: string;
  private sub: any;

  @ViewChild('fileInput') fileInput: ElementRef;

  constructor(private route: ActivatedRoute, protected http: Http) {

    this.sub = this.route.params.subscribe(params => {
      this.id = params['id'];
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }
}
