package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.gateways

// A method of authenticating via basic authentication
interface IAuthGateway {
  fun getClientToken(service: String): String
}
