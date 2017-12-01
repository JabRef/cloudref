import { Component, OnInit } from '@angular/core';
import { GlobalState } from '../../../global.state';
import { DefaultApi } from '../../../gen/api/DefaultApi';
var jdenticon = require('jdenticon');

@Component({
  selector: 'ba-page-top',
  templateUrl: './baPageTop.html',
  styleUrls: ['./baPageTop.scss'],
})
export class BaPageTop implements OnInit {

  isScrolled: boolean = false;
  isMenuCollapsed: boolean = false;
  userName: string;

  constructor(private _state: GlobalState, protected api: DefaultApi) {

    let object = localStorage.getItem('CloudRefUser');
    if (object != null) {
      let userInfo = JSON.parse(object);
      this.userName = userInfo.username;
    }

    this._state.subscribe('menu.isCollapsed', (isCollapsed) => {
      this.isMenuCollapsed = isCollapsed;
    });
  }

  toggleMenu() {
    this.isMenuCollapsed = !this.isMenuCollapsed;
    this._state.notifyDataChanged('menu.isCollapsed', this.isMenuCollapsed);
    return false;
  }

  scrolledChanged(isScrolled) {
    this.isScrolled = isScrolled;
  }

  logout() {
    // remove user info from local storage
    localStorage.removeItem('CloudRefUser');
  }

  ngOnInit() {
    // load avatar for user
    jdenticon.update(document.getElementById("userAvatarJdenticon"), this.userName);
  }
}
