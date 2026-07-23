# claim-vat-enrolment-frontend

This is a Scala/Play frontend to allow users to claim vat enrolment from HMRC.

---

## How to run the service locally

### From source code on your local machine

> The below instructions are for Mac/Linux only. Windows users will have to use SBT to run the service on port `9936`.

1. Make sure any dependent services are running using the following service-manager command
   `sm2 --start CLAIM_VAT_ENROLMENT_ALL`

2. Stop the frontend in service manager using
   `sm2 --stop CLAIM_VAT_ENROLMENT_FRONTEND`

3. Run the frontend locally using
   `./run.sh` or
   `sbt 'run 9936 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`and enable the `/test-only` routes for local
   testing.

4. Run the Unit and Integration tests
   To run the unit and integration tests, you can either use ```sbt test it/test``` or
   ```sbt clean coverage test it/test scalastyle coverageReport``` or  ```./run-all-tests.sh```.

### Starting the journey

1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
    - Redirect URL:
      ` http://localhost:9936/claim-vat-enrolment/journey/<vrn>?continueUrl=/test/enrolments/<Enrolment Key>/users `
    - Replace `vrn` with the VAT number
    - Replace `Enrolment Key` with the appropriate enrolment
      key [(See here)](README.md#Using-the-Allocate-Enrolment-Call-stub)
3. Click `Submit` to start the journey.

---

## End-Points

### GET /journey/:vrn

This endpoint creates a journey to enable a user to claim a VAT enrolment. The url also
takes the query parameter `continueUrl`. Prior to release 0.91.0
the service would send the user to the nominated continue url after the
successful completion of an enrolment claim. For release 0.91.0 onwards, the service redirects the user to the
business tax account service following a successful enrolment claim. Note, however, although it is no longer used
the `continueUrl` is still mandatory when invoking the initial url /journey/:vrn.

#### Request:

A valid VRN and continueUrl must be sent in the URL

#### Response:

| Expected Response    | Reason                                      |
|----------------------|---------------------------------------------|
| ```SEE_OTHER(301)``` | ```Redirects to VatRegistrationDate page``` |

---

## End of Journey Submission

At the end of the user journey, the user submits their known facts to validate their identity before claiming their VAT
enrolment.

### Submission Attempts & Lockout

To avoid fraudulent activity of churning attempts to impersonate a user,
restrictions have been added to the number of submission attempts.

After 3 failed attempts, a user shall be locked out from the service for 24 hours.

Their attempt data expires after 24h, and the 24h expiry is refreshed at every new attempt.
This means if a user has 2 failed attempts and waits 24h before their 3rd failed attempt,
the new attempt will be stored as their now 1st failed attempt.

### Known Facts Match - Request Body

An example request body submitted from our service:

```
{
  "userId": "0e8189b1-720e-418e-b4da-97c4983eb234",
  "friendlyName": "Making Tax Digital - VAT",
  "type": "principal",
  "verifiers": [
    { "key": "VATRegistrationDate", "value": "2026-01-30" },
    { "key": "Postcode", "value": "AA1 1AA" },
    { "key": "BoxFiveValue", "value": "1000.00" },
    { "key": "LastMonthLatestStagger", "value": "01" },
    { "key": "FormBundleNumber", "value": "123456789101" }
  ]
}
```

Note:

- `Postcode` value is optional
- Either `(BoxFiveValue && LastMonthLatestStagger) || FormBundleNumber` must be provided
- 'FormBundleNumber' and 'VAN' (Vat Application Number) names are used interchangeably

### Known Facts Match - API Call Chain

1. Our service calls the tax-enrolments service's endpoint:

`POST /tax-enrolments/groups/<group-id>/enrolments/HMRC-MTD-VAT~VRN~<vrn>`

2. tax-enrolments calls the enrolment-store-proxy service's ES8 endpoint:

`POST /enrolment-store-proxy/enrolment-store/groups/<group-id>/enrolments/HMRC-MTD-VAT~VRN~<vrn>`

3. enrolment-store-proxy call the IF API endpoint:

`POST /organisations/known-facts/VATC/VRN/<vrn>/match`

### Known Facts Match - enrolment-store-proxy ES8 handling of API call

[Confluence documentation](https://confluence.tools.tax.service.gov.uk/pages/viewpage.action?pageId=79287012&spaceKey=GGWRLS&title=ES8%2B-%2BAllocate%2Ban%2Benrolment%2Bto%2Ba%2Bgroup)
for the ES8 endpoint.

Note that the IF API response for a...

- successful match is `200 - {"outcome": true}`
- unsuccessful match is `200 - {"outcome": false}`
- other failure is `4XX / 5XX`

The ESP service converts an unsuccessful match into a less helpful response:

`400 - {"code": "INVALID_IDENTIFIERS", "message": "The enrolment identifiers provided were invalid"}`

---

## Test Only Routes

See the [Test README](TestREADME.md)

---

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
