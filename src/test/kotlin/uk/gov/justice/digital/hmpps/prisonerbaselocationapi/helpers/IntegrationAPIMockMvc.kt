package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.helpers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers.GOALS_RO

@AutoConfigureMockMvc
class IntegrationAPIMockMvc(
  @Autowired var mockMvc: MockMvc,
) {
  fun performAuthorised(path: String): MvcResult = mockMvc.perform(
    get(path).with(
      jwt().authorities(SimpleGrantedAuthority(GOALS_RO)),
    ),
  ).andReturn()
}
