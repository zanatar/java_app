import {Component, OnInit, ViewChild} from '@angular/core';
import {FormControl, FormGroup, NgForm, Validators} from "@angular/forms";
import {ValidatorsProvider} from "../shared/validators.provider";
import {ObjectUtils} from "../shared/object-utils.service";
import {ParticipantPersonality, Personality} from "./user.model";
import {UserService} from "./user.service";

@Component({
    selector: 'app-profile',
    templateUrl: './profile.component.html',
    styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
    personalCtrl: FormGroup;
    credentialsCtrl: FormGroup;
    contactCtrl: FormGroup;
    @ViewChild("personalForm", { static: false })
    personalForm: NgForm;
    @ViewChild("contactForm", { static: false })
    contactForm: NgForm;
    personalityFieldNames = [
        'firstname',
        'lastname',
        'middlename'
    ];
    participantPersonalityFieldNames = [
        'hh',
        'github',
        'bitbucket',
        'linkedin',
        'website'
    ];
    personalityFields = {
        'firstname': {
            label: "Имя",
            placeholder: "Иван",
            needTranslation: 'должно',
            required: false
        },
        'lastname': {
            label: "Фамилия",
            placeholder: "Иванов",
            needTranslation: 'должна',
            required: false
        },
        'middlename': {
            label: "Отчество",
            placeholder: "Александрович",
            needTranslation: 'должно',
            required: false
        },
        'quickname': {
            label: "Никнейм",
            placeholder: "ivan",
            required: true
        },
        'linkedin': {
            label: "linkedin",
            placeholder: "linkedin.com/",
            required: false,
            baseUrl: "([a-zA-Z0-9]+\.)?linkedin\.com"
        },
        'hh': {
            label: "hh",
            placeholder: "spb.hh.ru/resume/",
            required: false,
            baseUrl: "([a-zA-Z0-9]+\.)?hh\.ru"
        },
        'github': {
            label: "Github",
            placeholder: "github.com/tovalds",
            required: false,
            baseUrl: 'github\.com'
        },
        'bitbucket': {
            label: "Bitbucket",
            placeholder: "https://bitbucket.org",
            required: false,
            baseUrl: 'bitbucket\.org'
        },
        'website': {
            label: "Сайт",
            placeholder: "site.example.com",
            required: false
        },
    };
    private sourceUserPersonal: Personality;
    private sourceUserContact: ParticipantPersonality;

    constructor(private userService: UserService) {
    }

    getPersonalField(name: string) {
        return this.personalCtrl.get(name);
    }

    getCredentialsField(name: string) {
        return this.credentialsCtrl.get(name);
    }

    getContactsField(name: string) {
        return this.contactCtrl.get(name);
    }

    ngOnInit() {
        this.userService.getCurrent().subscribe(user => {
            this.sourceUserPersonal = ObjectUtils.clone(user.personality);
            this.sourceUserContact = ObjectUtils.clone(user.participantPersonality);

            this.personalCtrl = new FormGroup({
                quickname: new FormControl('', Validators.required),
                firstname: new FormControl('', ValidatorsProvider.validName),
                lastname: new FormControl('', ValidatorsProvider.validName),
                middlename: new FormControl('', ValidatorsProvider.validName)
            });
            this.contactCtrl = new FormGroup({
                linkedin: new FormControl(''),
                hh: new FormControl(''),
                github: new FormControl(''),
                bitbucket: new FormControl(''),
                website: new FormControl('', ValidatorsProvider.validUrl()),
                essay: new FormControl('')
            });
            this.credentialsCtrl = new FormGroup({
                email: new FormControl({
                    value: '',
                    disabled: true
                }),
                current: new FormControl(
                    '', Validators.required),
                password: new FormControl('',
                    [
                        Validators.required,
                        Validators.minLength(5),
                        Validators.maxLength(30)
                    ]),
                passwordTwice: new FormControl('', [Validators.required])
            }, [ValidatorsProvider.passwordsAreEqual()]);
            this.personalCtrl.patchValue(user.personality);
            this.credentialsCtrl.patchValue(user);
            this.contactCtrl.patchValue(user.participantPersonality);
        });
    }

    setPersonalCtrl(value?: Personality) {
        this.personalCtrl.markAsUntouched();
        this.personalCtrl.markAsPristine();
        let setTo = value ? value : this.sourceUserPersonal;
        console.log("set to", setTo);
        this.sourceUserPersonal = ObjectUtils.clone(setTo);
        this.personalCtrl.patchValue(setTo);
    }

    setContactCtrl(value?: ParticipantPersonality) {
        this.contactCtrl.markAsUntouched();
        this.contactCtrl.markAsPristine();
        let setTo = value ? value : this.sourceUserContact;
        console.log("set to", setTo);
        this.sourceUserContact = ObjectUtils.clone(setTo);
        this.contactCtrl.patchValue(setTo);
    }

    savePersonal() {
        let personality = this.personalForm.value;
        console.log("save personality", personality);
        this.userService.updatePersonal(personality).subscribe(p => {
            console.log("updated personality to", p);
            this.setPersonalCtrl(p);
        })
    }

    saveContact() {
        let contact = this.contactForm.value;
        console.log("save contact", contact);
        this.userService.updateContacts(contact).subscribe(c => {
            console.log("updated contact to", c);
            this.setContactCtrl(c);
        })
    }

    updatePassword(credentialsForm: NgForm) {
        console.log('changing password');
        let payload = credentialsForm.value;
        this.userService.updatePassword(payload.current, payload.password).subscribe();
    }

    canDeactivate(): boolean {
        let personality = this.personalForm.value;
        let contact = this.contactForm.value;
        console.log('Comparing', this.sourceUserPersonal, '\n', personality);
        console.log('Comparing', this.sourceUserContact, '\n', contact);

        return ObjectUtils.equals(this.sourceUserContact, contact)
            && ObjectUtils.equals(this.sourceUserPersonal, personality);
    }
}
