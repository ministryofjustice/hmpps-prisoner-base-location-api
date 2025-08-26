package uk.gov.justice.digital.hmpps.prisonerbaselocationapi.controllers

// Role Constants
const val GOALS_RO = "ROLE_BASE_LOCATION__GOALS__RO"

// Authority Checks
const val HAS_VIEW_BASE_LOCATION = """hasAuthority('$GOALS_RO')"""
