import {AbstractControl, AsyncValidatorFn, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {SecurityService} from "./security.service";
import {Observable} from "rxjs";

export class ValidatorsProvider {
    static email: ValidatorFn = Validators.pattern("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$");

    static validUrl(host?: string): ValidatorFn {
        if (host) {
            return Validators.pattern(
                "^(https?:\\/\\/)?" + host + "\\/\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$"
            );
        }
        return Validators.pattern(
                "^(https?:\\/\\/)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)$"
        );
    }


    static validName: ValidatorFn = Validators.pattern(
        "^[а-яёЁА-Я ',.\\-a-zA-Z]{2,}$"
    );


    static passwordsAreEqual(): ValidatorFn {
        return (group: FormGroup): { [key: string]: any } => {
            if (group.get('password').value === group.get('passwordTwice').value) {
                return null;
            }
            return {
                passwordEq: 'Password are not equal'
            }
        }
    }

    static emailNotTaken(securityService: SecurityService, sourceEmail: string): AsyncValidatorFn {
        return (control: AbstractControl) => {
            if (sourceEmail !== control.value) {
                return securityService.isEmailFree(control.value)
                    .map(valid => {
                        return valid ? null : {'emailNotTaken': 'email already taken'}
                    })
            } else {
                return new Observable(obs => obs.next(null));
            }
        }
    }


}