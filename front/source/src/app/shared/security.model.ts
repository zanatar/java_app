export interface SecurityToken {
    roles: string[];
    accountCategory: string;
    payload: string;
}

export interface SecurityVoucher {
    payload: string;
    accountEmail: string;
    accountVerified: boolean;
}