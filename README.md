
# claim-vat-enrolment-frontend

This is a Scala/Play frontend to allow users to claim vat enrolment from HMRC.

### How to run the service

1. Make sure any dependent services are running using the following service-manager command
`sm --start CLAIM_VAT_ENROLMENT_ALL -r`

2. Stop the frontend in service manager using
 `sm --stop CLAIM_VAT_ENROLMENT_FRONTEND`
 
3. Run the frontend locally using
`./run.sh` or 
`sbt 'run 9936 -Dapplication.router=testOnlyDoNotUseInAppConf.Routes'`

4. Go to the homepage:
http://localhost:9936/claim-vat-enrolment

## End-Points
### GET /journey/:vrn 

---

This endpoint creates a journey to enable a user to claim a VAT enrolment. The url also
takes the query parameter `continueUrl`. Prior to release 0.91.0
the service would send the user to the nominated continue url after the
successful completion of an enrolment claim. For release 0.91.0 onwards, the service will redirect the user to the
business tax account service following a successful enrolment claim. Note, however, although it is no longer used 
the continue url is still mandatory when invoking the initial url /journey/:vrn. 

####Request:

A valid VRN and continueUrl must be sent in the URL

#### Response:
            
| Expected Response                       | Reason  
|-----------------------------------------|------------------------------
|```SEE_OTHER(301)```                     |  ```Redirects to VatRegistrationDate page```       


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").