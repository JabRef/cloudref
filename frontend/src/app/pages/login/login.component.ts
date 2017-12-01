import { Component } from '@angular/core';
import { FormGroup, AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { DefaultApi } from '../../gen/api/DefaultApi';
import { getErrorMessage } from '../../shared/errorHandler';

@Component({
  selector: 'login',
  templateUrl: './login.html',
  styleUrls: ['./login.scss'],
})

export class Login {

  form: FormGroup;
  username: AbstractControl;
  password: AbstractControl;
  submitted: boolean = false;

  invalidLogin: boolean = false;
  invalidLoginCounter: number = 0;
  httpErrorMsg = null;

  constructor(private router: Router, fb: FormBuilder, protected api: DefaultApi) {
    this.form = fb.group({
      'username': ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(20), Validators.pattern('[a-zA-Z-0-9]*')])],
      'password': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
    });

    this.username = this.form.controls['username'];
    this.password = this.form.controls['password'];
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.form.valid) {

      var object = {
        username: this.username.value,
        password: this.password.value,
        role: null,
      }

      // try to access login
      localStorage.setItem('CloudRefUser', JSON.stringify(object));
      this.api.loginUser().subscribe(
        (data) => {
          if (data != null) {
            // save user information
            object.role = data;
            localStorage.setItem('CloudRefUser', JSON.stringify(object));

            // user is logged in
            this.router.navigate(['references']);
          }
        },
        (err) => {
          // remove user info from local storage
          localStorage.removeItem('CloudRefUser');

          if (err.status === 401) {
            // show alert
            this.showAlert();
          } else {
            this.errorHandler(err);
          }
        },
      );
    } else {
      // no valid input
      this.showAlert();
    }
  }

  showAlert(): void {
    this.invalidLogin = true;

    // show alert
    this.invalidLoginCounter++;
    setTimeout(() => {
      this.invalidLoginCounter--;
      if (this.invalidLoginCounter === 0) {
        this.invalidLogin = false;
      }
    }, 5000);
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }
}
