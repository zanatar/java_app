# T-Challenge: Participant Web Client
Web client for T-Challenge participants


## Configuration

Config file reside in `source\src\environments\environment.ts`

```
export const environment = {
    apiBaseUrl: 'http://localhost:4567',
    clientBaseUrl: 'http://localhost:4200',
    production: true
};
```

`apiBaseUrl` - points to backend endpoint<br>
`clientBaseUrl` - on this endpoint this service will work 


## Run 

Tested on `Node.js v14.0.0` and `npm: 6.14.4`

### Locally

Instal NPM dependencies:
```bash
$ cd source
$ npm install
```

run 
```bash
$ npm run start -- --host 0.0.0.0
```

and then application can be accessed on http://localhost:4200


## Smoke test

**T-Challenge Service should also be available on its standard location http://localhost:4567**

Open http://localhost:4200, login page should be present, login with credentials `user@user.com / 12345` should work.

