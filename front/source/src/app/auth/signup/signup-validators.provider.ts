import {AbstractControl, AsyncValidatorFn, FormGroup, ValidatorFn, Validators} from "@angular/forms";
import {SecurityService} from "../../shared/security.service";


export class SignupValidatorsProvider {


    static email: ValidatorFn = Validators.pattern("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$");

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

    static emailNotTaken(securityService: SecurityService) : AsyncValidatorFn {
        return (control: AbstractControl) => {
            return securityService.isEmailFree(control.value)
                .map(valid => {
                    return valid? null : {'emailNotTaken' : 'email already taken'}
                })
        }
    }
}
