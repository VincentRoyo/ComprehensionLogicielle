export type OTelUser = { email?: string;} | null;

let currentUser: OTelUser = null;

export function setOtelUser(user: OTelUser) {
    currentUser = user;
}

export function getOtelUser(): OTelUser {
    return currentUser;
}
