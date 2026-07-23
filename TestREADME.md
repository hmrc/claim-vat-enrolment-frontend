# Claim Vat Enrolment Test End-Points

## Testing

---

1. [Setting up the Feature Switches](TestREADME.md#get-test-onlyfeature-switches)
2. [Allocated Enrolment Feature Switch](TestREADME.md#get-enrolmentshmrc-mtd-vatvrnvatnumberusers)
2. [Query User Call Feature Switch](TestREADME.md#post-groupsgroupidenrolmentsenrolmentkey)
3. [Using the Allocated Enrolment Call stub](TestREADME.md#Using-the-Allocated-Enrolment-Call-stub)
4. [Using the query user Call stub](TestREADME.md#Using-the-query-user-Call-stub)

---

### GET test-only/feature-switches

Shows all feature switches:

1. Claim Vat Enrolment

- Use stub for allocate enrolment call
- Use stub for query user call

---

### GET /enrolments/HMRC-MTD-VAT~VRN~:vatNumber/users

Stub to check a user's enrollment call.

`QueryUserIdStub` must be enabled to use this stubbed endpoint.

#### Request:

Example URI with query parameter:

`/claim-vat-enrolment/test-only/enrolments/HMRC-MTD-VAT~VRN~123456782/users`

#### Stubbed Responses:

| VRN             | Response                                                             |
|-----------------|----------------------------------------------------------------------|
| ```111111111``` | ```OK(200)```                                                        |
| ```333333333``` | ```OK(200)```                                                        |
| ```444444444``` | ```NO_CONTENT(204)```                                                |
| ```555555555``` | ```NO_CONTENT(204)```                                                |
| ```123456789``` | ```NO_CONTENT(204)```                                                |
| ```968501689``` | ```NO_CONTENT(204)```                                                |
| ```other```     | ```INTERNAL_SERVER_ERROR('Error in the QueryUsersStubController')``` |

---

### POST /groups/:groupId/enrolments/:enrolmentKey

Stub to create an allocating enrollment call.

`AllocateEnrolmentStub` must be enabled to use this stubbed endpoint.

#### Request:

Example URI with query parameters:

`/claim-vat-enrolment/test-only/groups/6C59EE2E-BFD8-491A-A5EE-E7A01088D971/enrolments/HMRC-MTD-VAT~VRN~123456782`

Example request body :

```
{
    "userId": "123456",
    "friendlyName":"Making Tax Digital - VAT",
    "type": "principal",
    "verifiers": {
        "VATRegistrationDate":"2021-01-01",
        "Postcode": "AA11AA",
        "BoxFiveValue": "1000.00",
        "LastMonthLatestStagger": "JANUARY"
   }
 }
```

- Group ID is never checked.
- Stubbed VRNs (`111111111` - `444444444`) return a set success or failure response (see below) dependent on the number.
- Unstubbed VRNs will return a CREATED(201), BAD_REQUEST(Invalid Json) or BAD_REQUEST(Incorrect Known Facts) depending
  on the submission body.

#### Responses:

| Expected Response      | Reason                                                   |
|------------------------|----------------------------------------------------------|
| ```CREATED(201)```     | ```Enrolment was successful```                           |
| ```BAD_REQUEST(400)``` | ```Submitted known facts are incorrect```                |
| ```BAD_REQUEST(400)``` | ```Request body has an invalid Json format```            |
| ```CONFLICT(409)```    | ```User credentials already have an MTD-VAT enrolment``` |

#### How to get the right response:

| VRN                                                         | Response                                                                |
|-------------------------------------------------------------|-------------------------------------------------------------------------|
| ```111111111```                                             | ```CREATED```                                                           |
| ```222222222```                                             | ```CONFLICT('code' -> 'MULTIPLE_ENROLMENTS_INVALID')```                 |
| ```333333333```                                             | ```INTERNAL_SERVER_ERROR('Error on the Allocate Enrolment call')```     |
| ```444444444```                                             | ```BAD_REQUEST('code' -> 'INVALID_IDENTIFIERS')```                      |
| ```other```                                                 | ```CREATED```                                                           |
| ```other with invalid json body```                          | ```BAD_REQUEST('code' -> 'INVALID_JSON', 'details' -> 'the details')``` |
| ```123456789 / 968501689 with answers that match (below)``` | ```CREATED```                                                           |
| ```123456789 / 968501689  with incorrect answers```         | ```BAD_REQUEST('code' -> 'INVALID_IDENTIFIERS')```                      |

```
VRN: 123456789
"VATRegistrationDate": "2025-01-01",
"Postcode": "AA1 1AA",
"BoxFiveValue": "123.45",
"LastMonthLatestStagger": "01"
```

```
VRN: 968501689
"VATRegistrationDate": "2025-12-12",
"Postcode": "BA1 1AB",
"FormBundleNumber": "099123456789"
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
