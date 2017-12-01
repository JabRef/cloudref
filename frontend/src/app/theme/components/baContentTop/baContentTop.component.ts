import { Component } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Location } from '@angular/common';

import { GlobalState } from '../../../global.state';

@Component({
  selector: 'ba-content-top',
  styleUrls: ['./baContentTop.scss'],
  templateUrl: './baContentTop.html',
})
export class BaContentTop {

  public activePageTitle: string = '';
  public currentPageTitle: string = '';
  // public subPage: boolean = false;

  constructor(private _state: GlobalState , router: Router, private location: Location ) {
    this._state.subscribe('menu.activeLink', (activeLink) => {
      if (activeLink) {
        this.activePageTitle = activeLink.title;
      }
    });
    router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {
        this.currentPageTitle = this.getTitle(router.routerState, router.routerState.root).join('-');
        // // check if current page is a sub page
        // if (this.currentPageTitle === this.activePageTitle) {
        //   this.subPage = false;
        //   // console.log('sub page', 'false');
        // } else {
        //   this.subPage = true;
        //   // console.log('sub page', 'true');
        // }
      }
    });
  }

  getTitle(state, parent) {
    var data = [];
    if(parent && parent.snapshot.data && parent.snapshot.data.title) {
      data.push(parent.snapshot.data.title);
    }

    if(state && parent) {
      data.push(... this.getTitle(state, state.firstChild(parent)));
    }
    return data;
  }

  // clicked(event) {
  //   this.location.back();
  // }
}
