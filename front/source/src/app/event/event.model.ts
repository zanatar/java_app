export class ForumEvent {

    id: string;
    permalink: string;
    maturities: string[];
    specializationPermalinks: string[];
    caption: string;
    description: string;
    greeting: string;
    validFrom: string;
    validUntil: string;
    congratulations: EventCongratulationMessage[];
    reviewThreshold: number;
}

export class EventCongratulationMessage {
    threshold: number;
    message: string;
}
