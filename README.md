
# claim-vat-enrolment-frontend

This is a Scala/Play frontend to allow users to claim vat enrolment from HMRC.

## How to run the service locally

---

### From source code on your local machine
> The below instructions are for Mac/Linux only. Windows users will have to use SBT to run the service on port `9936`.

1. Make sure any dependent services are running using the following service-manager command
`sm --start CLAIM_VAT_ENROLMENT_ALL -r`

2. Stop the frontend in service manager using
 `sm --stop CLAIM_VAT_ENROLMENT_FRONTEND`
 
3. Run the frontend locally using
`./run.sh` or 
`sbt 'run 9936 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`and enable the `/test-only` routes for local testing.

### Starting the journey

1. In a browser, navigate to the Auth Login Stub on `http://localhost:9949/auth-login-stub/gg-sign-in`
2. Enter the following information:
   - Redirect URL: ` http://localhost:9936/claim-vat-enrolment/journey/<vrn>?continueUrl=/test/enrolments/<Enrolment Key>/users `
   - Replace `vrn` with the VAT number
   - Replace `Enrolment Key` with the appropriate enrolment key [(See here)](README.md#Using-the-Allocate-Enrolment-Call-stub)
3. Click `Submit` to start the journey.


## End-Points
### GET /journey/:vrn 

---

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


## Test only routes

---

### Set feature switches
The service has two stubs, the ``Use stub for allocate enrolment call`` that calls allocated enrolments and the ```Use stub for query user call```  if the enrolment isn't successful.
To set the feature switches, navigate to `http://localhost:9936/claim-vat-enrolment/test-only/feature-switches`.


### Using the Allocate Enrolment Call stub

This stub returns different responses based on the enrolment key.

| Enrolment Key                        |   Response                |
|--------------------------------------|---------------------------|
| ```HMRC-MTD-VAT~VRN~555555555```     | ```BadRequest```          |
| ```HMRC-MTD-VAT~VRN~444444444```     | ```InternalServerError``` |
| ```HMRC-MTD-VAT~VRN~333333333```     | ```InternalServerError``` |
| ```HMRC-MTD-VAT~VRN~222222222```     | ```Conflict```            |
| ```HMRC-MTD-VAT~VRN~111111111```     | ```BadRequest```          |
| Any other Enrolment Key              | ```Created```             |


### Using the Query User Call stub
This stub returns different responses based on the vatNumber.

| VAT number                              |   Response                |
|-----------------------------------------|---------------------------|
| ```333333333``` or  ```111111111```     | ```Ok```                  |
| ```444444444``` or  ```555555555```     | ```NoContent```           |
| Any other vat number                    | ```InternalServerError``` |


### Unit and Integration tests
To run the unit and integration tests, you can either use ```sbt test it:test``` or ```sbt clean coverage test it:test scalastyle coverageReport```.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").