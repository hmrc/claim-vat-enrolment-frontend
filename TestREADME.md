# Claim Vat Enrolment Test End-Points

## Testing

---

1. [Setting up the Feature Switches](TestREADME.md#get-test-onlyfeature-switches)
2. [Allocated Enrolment Feature Switch](TestREADME.md#get-enrolmentshmrc-mtd-vatvrnvatnumberusers)
2. [Query User Call Feature Switch](TestREADME.md#post-groupsgroupidenrolmentsenrolmentkey)
3. [Using the Allocated Enrolment Call stub](TestREADME.md#Using-the-Allocated-Enrolment-Call-stub)
4. [Using the query user Call stub](TestREADME.md#Using-the-query-user-Call-stub)


### GET test-only/feature-switches

---
Shows all feature switches:

1. Claim Vat Enrolment

 - Use stub for allocate enrolment call
 - Use stub for query user call
 
### GET /enrolments/HMRC-MTD-VAT~VRN~:vatNumber/users

---

Stub to create an allocating enrollment call. This feature switch will need to be enabled.

#### Request:
A valid vatNumber must be sent in the URI or as a query parameter. Example of using the query parameter:

`test-only/enrolments/HMRC-MTD-VAT~VRN~:123456782/users`

#### Response:
Status:

| Expected Response                       | Reason                          |
|-----------------------------------------|---------------------------------|
| ```CREATED(201)```                           | ```EnrolmentSuccess```          |
| ```CONFLICT(409)```                    | ```MULTIPLE_ENROLMENTS_INVALID```   |
| ```BAD_REQUEST(400)```                    | ```InvalidKnownFacts```   |

Example response body :
```
{
    "userId": "123456",
    "friendlyName":"Making Tax Digital - VAT",
    "type": "principal",
    "verifiers": {
        "VATRegistrationDate":"2021-1-1",
        "Postcode": "AA11AA",
        "BoxFiveValue": "1000.00",
        "LastMonthLatestStagger": "JANUARY"
   }
 }

```

### POST /groups/:groupId/enrolments/:enrolmentKey

---

Stub to create an allocating enrollment call when the call is unsuccessful. This feature switch will need to be enabled.

#### Request:
A valid group and enrollment key must be sent in the URI or as a query parameter. Example of using the query parameter:

`test-only/groups/1234567/enrolments/HMRC-MTD-VAT~VRN~:123456782`

#### Request:
No body is required for this request

#### Response:

| Expected Response                       | Reason                          |
|-----------------------------------------|---------------------------------|
| ```OK(200)```                           | ```UsersFound```          |
| ```NoContent(204)```                    | ```UsersNotFound```   |


### Using the Allocated Enrolment Call stub

---

This stub returns different responses based on the VAT Registration Number.

`1111111111` will return a known facts mismatch because the data the user provided does not match the details we have. Upon submitting CYA will redirect the user to an error page.

`2222222222` will return a data mismatch which upon submitting CYA will redirect the user to an error page.

`3333333333` will return a conflict error because the user has already registered for VAT and will be redirected to an error page upon submitting CYA.

`4444444444` will redirect to a technical difficulties error page up upon submitting CYA. 

Any other vatNumber will create an enrolment and redirect to Sign Up Complete page.

| VAT Number                               | Response               |
|-----------------------------------------|------------------------|
| ```1111111111```                              | ```BAD_REQUEST```     |
| ```2222222222```                          | ```FailedDependency``` |
| ```3333333333```                          | ```CONFLICT```  |
| ```4444444444```                          | ```Ok```   |
| Any other vatNumber                     | ```CREATED```               |

### Using the query user Call stub

---

This stub returns different responses based on the VAT Registration Number.

`3333333333` indicates that the enrolment could not be allocated as a user with the specified enrolment already applied to their credential has been found

`4444444444` indicates that the enrolment could not be allocated but no other users have been found with the specified enrolment

Any other vat number throws and InternalServerError.

| VAT Number                               | Response               |
|-----------------------------------------|------------------------|
| ```3333333333```                              | ```Ok```     |
| ```4444444444```                          | ```NoContent``` |
| Any other vatNumber                           | ```InternalServerError```  |


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").