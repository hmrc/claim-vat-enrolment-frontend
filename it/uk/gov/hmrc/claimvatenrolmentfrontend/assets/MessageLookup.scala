/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.claimvatenrolmentfrontend.assets

object MessageLookup {

  object Base {
    val continue = "Continue"
    val change = "Change"
    val yes = "Yes"
    val no = "No"
    val tryAgain = "Try Again"
    val signOut = "Sign out"

    object Error {
      val title = "There is a problem"
      val error = "Error: "
    }

  }

  object Header {
    val signOut = "Sign out"
  }

  object BetaBanner {
    val title = "This is a new service – your feedback will help us to improve it."
  }

  object CaptureBusinessPostcode {
    val title = "What is the UK postcode where your business is registered for VAT? - Claim VAT Enrolment - GOV.UK"
    val heading = "What is the UK postcode where your business is registered for VAT?"
    val hint = "For example, AB1 2YZ"
    val link_text = "The business does not have a UK postcode"

    object Error {
      val emptyPostcode = "This field is required"
      val invalidPostcode = "Enter the postcode where your business is registered for VAT"
    }

  }

  object CaptureVatRegistrationDate {
    val title = "When did you become VAT registered? - Claim VAT Enrolment - GOV.UK"
    val heading = "When did you become VAT registered?"
    val para = "You can find this date on your VAT registration certificate."
    val hint = "For example, 6 4 2017"

    object Error {
      val invalidDate = "Enter a real date"
      val noDate = "Enter your VAT registration date"
      val futureDate = "VAT registration date must be in the past"
    }

  }

  object CaptureBox5Figure {
    val title = "What is your latest VAT Return total? - Claim VAT Enrolment - GOV.UK"
    val heading = "What is your latest VAT Return total?"
    val line_1 = "You can find this amount in box number 5 on your latest VAT Return submitted to HMRC."
    val line_2 = "The format of this number needs to be two decimal places, for example £123.00"

    object Error {
      val noFigure = "Enter your latest VAT Return total or Box 5 amount"
      val invalidLength = "The Box 5 amount must be less than 14 digits"
      val invalidFormat = "Enter a value to 2 decimal places. For example, £100.00"
    }

  }

  object CaptureSubmittedVATReturn {
    val title = "Are you currently submitting VAT Returns? - Claim VAT Enrolment - GOV.UK"
    val heading = "Are you currently submitting VAT Returns?"

    object Error {
      val errorMessage = "Select yes if you are submitting VAT returns"
    }

  }

  object CaptureLastMonthSubmitted {
    val title = "Select the last month of your latest VAT accounting period - Claim VAT Enrolment - GOV.UK"
    val heading = "Select the last month of your latest VAT accounting period"
    val firstLine = "You can find this in your latest VAT Return submitted to HMRC."
    val panelFirstHeading = "Latest VAT accounting period, example 1"
    val panelFirstText = "You submit your VAT Return quarterly (every three months). In the ‘accounting period’ January to March, the last month in that ‘accounting period’ is March. You must therefore select March."
    val panelSecondHeading = "Latest VAT accounting period, example 2"
    val panelSecondText = "If you submit your VAT Return monthly, the last accounting period you ‘submitted for’ was January. You must select January."
    val januaryLabel = "January"
    val februaryLabel = "February"
    val marchLabel = "March"
    val aprilLabel = "April"
    val mayLabel = "May"
    val juneLabel = "June"
    val julyLabel = "July"
    val augustLabel = "August"
    val septemberLabel = "September"
    val octoberLabel = "October"
    val novemberLabel = "November"
    val decemberLabel = "December"

    object Error {
      val noMonthSelected = "Select a month"
    }

  }

  object CheckYourAnswers {
    val title = "Check Your Answers - Claim VAT Enrolment - GOV.UK"
    val heading = "Check Your Answers"
    val vatNumberRow = "VAT Number"
    val vatRegDateRow = "VAT Registration Date"
    val businessPostcodeRow = "Where your business is registered for VAT"
    val vatReturnsRow = "You are currently submitting VAT returns"
    val boxFiveRow = "Your VAT return total or Box 5 amount"
    val lastReturnMonthRow = "The last month in your latest accounting period"

  }

  object KnownFactsMismatch {
    val title = "We could not confirm your business - Claim VAT Enrolment - GOV.UK"
    val heading = "We could not confirm your business"
    val line_1 = "The information you provided does not match the details we have about your business."

  }

  object InvalidAccountType {
    val title = "You are not authorised to use this service - Claim VAT Enrolment - GOV.UK"
    val heading = "You are not authorised to use this service"
    val line_1 = "Contact the person who set up the account. You need authority to enrol the business for VAT."

  }

  object UnmatchedUserError {
    val title = "You cannot use this service - Claim VAT Enrolment - GOV.UK"
    val heading = "You cannot use this service"
    val link = "manage who can access your taxes, duties and schemes"
    val line_1 = s"Go to your business tax account to $link."

  }

  object EnrolmentAlreadyAllocatedError {
    val title = "You cannot use this service - Claim VAT Enrolment - GOV.UK"
    val heading = "You cannot use this service"
    val line_1 = "Your business is set up for VAT with another Government Gateway user ID. Try signing in again using a different user ID."
    val link = "find or recover your account"
    val line_2 = s"If you have lost your details, $link"
    val button_text = "Go back to your business tax account"
    val link_url = "https://www.access.service.gov.uk/account-recovery/forgot-userid-password/check-your-emails"
  }

  object SignUpCompleteClient {
    val title = "You have added VAT to your business tax account - Claim VAT Enrolment - GOV.UK"
    val heading = "You have added VAT to your business tax account"
    val line_1 = "You can now use your business tax account to:"
    val bullet1 = "see what you owe"
    val bullet2 = "check your deadlines"
    val bullet3 = "pay your VAT"
    val bullet4 = "tell us about any changes to your business"
    val link_url = "https://www.tax.service.gov.uk/business-account"
  }

}