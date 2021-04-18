export class Personality {
    quickname: string;
    firstname: string;
    lastname: string;
    middlename: string;

    constructor() {
        this.firstname = '';
        this.lastname = '';
        this.middlename = '';
        this.quickname = '';
    }
}

export class ParticipantPersonality {
    essay: string;
    linkedin: string;
    hh: string;
    github: string;
    bitbucket: string;
    website: string;

    constructor() {
        this.essay = '';
        this.linkedin = '';
        this.hh = '';
        this.github = '';
        this.bitbucket = '';
        this.website = '';
    }
}

export class User {
    password?: string;
    passwordTwice?: string;

    id?: string;
    email: string;

    category: string;
    status: string;

    roles: string[];
    personality: Personality;
    participantPersonality: ParticipantPersonality;
    registeredAt: string;

    constructor() {
        this.email = '';
        this.category = 'PARTICIPANT';
        this.status = 'APPROVED';
        this.roles = ['PARTICIPANT'];
        this.password = '';
        this.passwordTwice = '';
        this.personality = new Personality();
        this.participantPersonality = new ParticipantPersonality();
        this.registeredAt = '';
    }
}