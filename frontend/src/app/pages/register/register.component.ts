import { Component } from '@angular/core';
import { FormGroup, AbstractControl, FormBuilder, Validators } from '@angular/forms';
import { EmailValidator, EqualPasswordsValidator } from '../../theme/validators';
import { DefaultApi } from '../../gen/api/DefaultApi';
import { Router } from '@angular/router';
import { User } from '../../gen/model/User';
import UserRoleEnum = User.UserRoleEnum;
import { getErrorMessage } from '../../shared/errorHandler';

@Component({
  selector: 'register',
  templateUrl: './register.html',
  styleUrls: ['./register.scss'],
})
export class Register {

  form: FormGroup;
  username: AbstractControl;
  name: AbstractControl;
  lastname: AbstractControl;
  email: AbstractControl;
  password: AbstractControl;
  repeatPassword: AbstractControl;
  passwords: FormGroup;
  httpErrorMsg = null;

  private usernameAvailable: boolean = true;
  private takenUsernames: string[] = [];

  submitted: boolean = false;

  constructor(fb: FormBuilder, private api: DefaultApi, private router: Router) {

    this.form = fb.group({
      'username': ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(20), Validators.pattern('[a-zA-Z-0-9]*')])],
      'name': ['', Validators.compose([Validators.required, Validators.minLength(3)])],
      'lastname': ['', Validators.compose([Validators.required, Validators.minLength(3)])],
      'email': ['', Validators.compose([Validators.required, EmailValidator.validate])],
      'passwords': fb.group({
        'password': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
        'repeatPassword': ['', Validators.compose([Validators.required, Validators.minLength(4)])],
      }, {validator: EqualPasswordsValidator.validate('password', 'repeatPassword')}),
    });

    this.username = this.form.controls['username'];
    this.name = this.form.controls['name'];
    this.lastname = this.form.controls['lastname'];
    this.email = this.form.controls['email'];
    this.passwords = <FormGroup> this.form.controls['passwords'];
    this.password = this.passwords.controls['password'];
    this.repeatPassword = this.passwords.controls['repeatPassword'];
  }

  onSubmit(values: Object): void {
    this.submitted = true;
    // check if form is valid and username does not already exist
    if (this.form.valid && this.usernameAvailable) {
      let user: User;
      user = {
        name: this.username.value,
        firstname: this.name.value,
        lastname: this.lastname.value,
        email: this.email.value,
        password: this.password.value,
        userRole: User.UserRoleEnum.USER,
      };
      this.api.saveUser(user.name, user).subscribe(
        (data) => {
          // user successfully added
          // sign user in
          var object = {
            username: this.username.value,
            password: this.password.value,
            role: UserRoleEnum.USER.toString(),
          }
          localStorage.setItem('CloudRefUser', JSON.stringify(object));

          this.router.navigate(['references']);
        },
        (err) => {
          if (err.status === 409) {
            // username is already taken
            this.takenUsernames.push(this.username.value);
            this.usernameAvailable = false;
          } else {
            this.errorHandler(err);
          }
        },
      );
    }
  }

  userNameTaken(): void {
    // test if backend already returned that the name is not available
    let newName: string = this.username.value;
    if (this.takenUsernames.length > 0) {
      if (this.takenUsernames.indexOf(newName) !== -1) {
        this.usernameAvailable = false;
      } else {
        this.usernameAvailable = true;
      }
    }
  }

  private errorHandler(err: any) {
    // show error to user
    this.httpErrorMsg = getErrorMessage(err);
  }
}
