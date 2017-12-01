import { Component, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Http } from '@angular/http';

@Component({
  selector: ':idSuggestion',
  templateUrl: './suggestion.html',
})

export class SuggestionComponent implements OnDestroy {

  id: string;
  private sub: any;

  constructor(private route: ActivatedRoute, protected http: Http) {

    this.sub = this.route.params.subscribe(params => {
      this.id = params['id'];
    });
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }
}
