/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.ui.screens.rules.create

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.skymansandy.wiretap.domain.model.HttpLog
import dev.skymansandy.wiretap.domain.model.RuleAction
import dev.skymansandy.wiretap.domain.model.WiretapRule
import dev.skymansandy.wiretap.domain.model.matchers.BodyMatcher
import dev.skymansandy.wiretap.domain.model.matchers.HeaderMatcher
import dev.skymansandy.wiretap.domain.model.matchers.UrlMatcher
import dev.skymansandy.wiretap.domain.orchestrator.HttpLogManager
import dev.skymansandy.wiretap.domain.repository.RuleRepository
import dev.skymansandy.wiretap.domain.usecase.FindConflictingRulesUseCase
import dev.skymansandy.wiretap.testing.MainDispatcherSupport
import dev.skymansandy.wiretap.ui.model.BodyMatchMode
import dev.skymansandy.wiretap.ui.model.HeaderEntry
import dev.skymansandy.wiretap.ui.model.HeaderEntryMode
import dev.skymansandy.wiretap.ui.model.ResponseHeaderEntry
import dev.skymansandy.wiretap.ui.model.ResponseHeadersEditMode
import dev.skymansandy.wiretap.ui.model.ThrottleInputMode
import dev.skymansandy.wiretap.ui.model.ThrottleProfile
import dev.skymansandy.wiretap.ui.model.UrlMatchMode
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateRuleViewModelTest {

    private val httpLogManager = mock<HttpLogManager>(MockMode.autoUnit)
    private val repo = mock<RuleRepository>(MockMode.autoUnit)
    // FindConflictingRulesUseCase is a final class; construct a real one over the mocked repo
    // and steer behavior via the repo's flowAll() (the use case calls flowAll().first()).
    private val findConflicting = FindConflictingRulesUseCase(repo)
    private val io = UnconfinedTestDispatcher()

    private fun stubConflicts(rules: List<WiretapRule>) {
        dev.mokkery.every { repo.flowAll() } returns kotlinx.coroutines.flow.flowOf(rules)
    }

    @BeforeTest fun setUp() { MainDispatcherSupport.setupMain() }
    @AfterTest fun tearDown() { MainDispatcherSupport.teardownMain() }

    private fun newVm(
        existingRuleId: Long = 0,
        prefillConfig: PrefillConfig = PrefillConfig(),
    ) = CreateRuleViewModel(
        existingRuleId = existingRuleId,
        prefillConfig = prefillConfig,
        httpLogManager = httpLogManager,
        findConflictingRules = findConflicting,
        ruleRepository = repo,
        ioDispatcher = io,
    )

    /** Keeps WhileSubscribed stateIn active so .value reflects the latest computed value. */
    private fun <T> TestScope.subscribe(flow: StateFlow<T>) {
        backgroundScope.launch { flow.collect { } }
    }

    // ---- step navigation ---------------------------------------------------

    @Test
    fun `next, prev, and reset move the step counter`() = runTest {
        everySuspend { repo.getById(any()) } returns null

        val vm = newVm()

        vm.step.value shouldBe 1
        vm.nextStep()
        vm.step.value shouldBe 2
        vm.prevStep()
        vm.step.value shouldBe 1
        vm.nextStep()
        vm.nextStep()
        vm.resetStep()
        vm.step.value shouldBe 1
    }

    // ---- isEditing flag ----------------------------------------------------

    @Test
    fun `isEditing reflects whether existingRuleId is positive`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        newVm(existingRuleId = 0).isEditing shouldBe false
        newVm(existingRuleId = 5).isEditing shouldBe true
    }

    // ---- canProceedToResponse ---------------------------------------------

    @Test
    fun `canProceedToResponse is false when no matcher is present`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe false
    }

    @Test
    fun `canProceedToResponse is true when url mode has a non-blank pattern`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/api")
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe true
    }

    @Test
    fun `canProceedToResponse is false when url mode is set but pattern blank`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        vm.updateUrlMode(UrlMatchMode.Exact)
        vm.updateUrlPattern("")
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe false
    }

    @Test
    fun `canProceedToResponse is false when any header has blank key`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/api")
        vm.addHeader() // default HeaderEntry has blank key
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe false
    }

    @Test
    fun `canProceedToResponse allows KeyExists header with blank value`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        vm.addHeader()
        vm.updateHeader(0, HeaderEntry(key = "Authorization", mode = HeaderEntryMode.KeyExists))
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe true
    }

    @Test
    fun `canProceedToResponse rejects ValueExact header with blank value`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canProceedToResponse)
        vm.addHeader()
        vm.updateHeader(0, HeaderEntry(key = "Authorization", value = "", mode = HeaderEntryMode.ValueExact))
        advanceUntilIdle()

        vm.canProceedToResponse.value shouldBe false
    }

    // ---- canSaveRule -------------------------------------------------------

    @Test
    fun `canSaveRule is true when Mock response code is in the 100 to 599 range`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canSaveRule)
        vm.updateAction(RuleAction.Mock())
        vm.updateMockResponseCode("418")
        advanceUntilIdle()

        vm.canSaveRule.value shouldBe true
    }

    @Test
    fun `canSaveRule is false when Mock response code is out of range`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canSaveRule)
        vm.updateAction(RuleAction.Mock())
        vm.updateMockResponseCode("99")
        advanceUntilIdle()

        vm.canSaveRule.value shouldBe false
    }

    @Test
    fun `canSaveRule is true for Throttle action regardless of code`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        subscribe(vm.canSaveRule)
        vm.updateAction(RuleAction.Throttle())
        advanceUntilIdle()

        vm.canSaveRule.value shouldBe true
    }

    // ---- field updates ----------------------------------------------------

    @Test
    fun `updateMethod and matcher mode and pattern setters propagate`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()

        vm.updateMethod("POST")
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/x")
        vm.updateBodyMode(BodyMatchMode.Regex)
        vm.updateBodyPattern("[0-9]+")

        val req = vm.requestState.value
        req.method shouldBe "POST"
        req.urlMode shouldBe UrlMatchMode.Contains
        req.urlPattern shouldBe "/x"
        req.bodyMode shouldBe BodyMatchMode.Regex
        req.bodyPattern shouldBe "[0-9]+"
    }

    @Test
    fun `addHeader, updateHeader and removeHeader mutate the header list`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()

        vm.addHeader()
        vm.addHeader()
        vm.requestState.value.headerEntries.size shouldBe 2

        val entry = HeaderEntry(key = "Authorization", value = "abc", mode = HeaderEntryMode.ValueExact)
        vm.updateHeader(1, entry)
        vm.requestState.value.headerEntries[1] shouldBe entry

        vm.removeHeader(0)
        vm.requestState.value.headerEntries.size shouldBe 1
        vm.requestState.value.headerEntries[0] shouldBe entry
    }

    @Test
    fun `updateMockResponseCode strips non-digit characters`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.updateMockResponseCode("4o4")

        vm.responseState.value.mockResponseCode shouldBe "44"
    }

    @Test
    fun `updateThrottleDelayMs strips non-digit characters`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.updateThrottleDelayMs("1a2b3c")

        vm.responseState.value.throttleDelayMs shouldBe "123"
    }

    @Test
    fun `updateThrottleInputMode to None zeroes the delays`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.updateThrottleDelayMs("500")
        vm.updateThrottleDelayMaxMs("1000")

        vm.updateThrottleInputMode(ThrottleInputMode.None)

        vm.responseState.value.throttleInputMode shouldBe ThrottleInputMode.None
        vm.responseState.value.throttleDelayMs shouldBe "0"
        vm.responseState.value.throttleDelayMaxMs shouldBe "0"
    }

    @Test
    fun `updateThrottleInputMode to non-None preserves delay values`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.updateThrottleDelayMs("500")

        vm.updateThrottleInputMode(ThrottleInputMode.Manual)

        vm.responseState.value.throttleDelayMs shouldBe "500"
        vm.responseState.value.throttleInputMode shouldBe ThrottleInputMode.Manual
    }

    @Test
    fun `updateResponseHeadersMode KeyValue parses the bulk text`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.updateResponseHeadersBulk("Content-Type: application/json\nAccept: */*")
        vm.updateResponseHeadersMode(ResponseHeadersEditMode.KeyValue)

        val entries = vm.responseState.value.responseHeaderEntries
        entries.map { it.key to it.value } shouldBe listOf(
            "Content-Type" to "application/json",
            "Accept" to "*/*",
        )
    }

    @Test
    fun `updateResponseHeadersMode BulkEdit serializes the entries`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.addResponseHeader()
        vm.updateResponseHeader(0, ResponseHeaderEntry("X-A", "1"))

        vm.updateResponseHeadersMode(ResponseHeadersEditMode.BulkEdit)

        vm.responseState.value.responseHeadersBulk shouldBe "X-A: 1"
    }

    @Test
    fun `removeResponseHeader removes the indexed entry`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.addResponseHeader()
        vm.addResponseHeader()
        vm.updateResponseHeader(0, ResponseHeaderEntry("a", "1"))
        vm.updateResponseHeader(1, ResponseHeaderEntry("b", "2"))

        vm.removeResponseHeader(0)

        vm.responseState.value.responseHeaderEntries shouldBe listOf(ResponseHeaderEntry("b", "2"))
    }

    // ---- regex tester ------------------------------------------------------

    @Test
    fun `openRegexTester sets pattern, label and visibility`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()

        vm.openRegexTester(pattern = "[a-z]+", label = "URL")

        vm.regexTesterPattern.value shouldBe "[a-z]+"
        vm.regexTesterLabel.value shouldBe "URL"
        vm.showRegexTester.value shouldBe true
    }

    @Test
    fun `closeRegexTester hides the tester`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val vm = newVm()
        vm.openRegexTester("x", "y")

        vm.closeRegexTester()

        vm.showRegexTester.value shouldBe false
    }

    // ---- conflict ---------------------------------------------------------

    @Test
    fun `saveRule shows conflict dialog when conflicts are found`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val conflict = WiretapRule(id = 99, method = "*", action = RuleAction.Mock())
        stubConflicts(listOf(conflict))

        val vm = newVm()
        vm.updateUrlMode(UrlMatchMode.Exact)
        vm.updateUrlPattern("/api")
        vm.saveRule { }
        advanceUntilIdle()

        vm.showConflictDialog.value shouldBe true
        vm.conflictingRules.value shouldBe listOf(conflict)
    }

    @Test
    fun `dismissConflictDialog hides dialog and clears the list`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        val conflict = WiretapRule(id = 99, method = "*", action = RuleAction.Mock())
        stubConflicts(listOf(conflict))

        val vm = newVm()
        vm.updateUrlMode(UrlMatchMode.Exact)
        vm.updateUrlPattern("/api")
        vm.saveRule { }
        advanceUntilIdle()

        vm.showConflictDialog.value shouldBe true
        vm.dismissConflictDialog()
        vm.showConflictDialog.value shouldBe false
        vm.conflictingRules.value shouldBe emptyList()
    }

    // ---- saveRule paths ---------------------------------------------------

    @Test
    fun `saveRule with no conflicts and existingRuleId zero calls addRule and reports null`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        stubConflicts(emptyList())

        val vm = newVm()
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/api")

        var savedCallback: WiretapRule? = WiretapRule(id = -1, action = RuleAction.Mock())
        vm.saveRule { savedCallback = it }
        advanceUntilIdle()

        verifySuspend {
            repo.addRule(
                matches<WiretapRule> {
                    it.id == 0L &&
                        it.urlMatcher is UrlMatcher.Contains &&
                        (it.urlMatcher as UrlMatcher.Contains).pattern == "/api"
                },
            )
        }
        savedCallback shouldBe null // not editing → callback fires with null
    }

    @Test
    fun `saveRule when editing calls updateRule and reports the saved rule`() = runTest {
        val existing = WiretapRule(
            id = 5,
            method = "GET",
            urlMatcher = UrlMatcher.Contains("/old"),
            action = RuleAction.Mock(),
            createdAt = 12345L,
            enabled = true,
        )
        everySuspend { repo.getById(5) } returns existing
        stubConflicts(emptyList())

        val vm = newVm(existingRuleId = 5)
        advanceUntilIdle()
        vm.updateUrlPattern("/new")

        var saved: WiretapRule? = null
        vm.saveRule { saved = it }
        advanceUntilIdle()

        verifySuspend {
            repo.updateRule(
                matches<WiretapRule> {
                    it.id == 5L &&
                        it.createdAt == 12345L &&
                        (it.urlMatcher as? UrlMatcher.Contains)?.pattern == "/new"
                },
            )
        }
        saved?.id shouldBe 5L
    }

    // ---- buildRuleFromForm via saveRule -----------------------------------

    @Test
    fun `buildRuleFromForm trims method patterns and applies blank-to-wildcard fallback`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        stubConflicts(emptyList())

        val vm = newVm()
        vm.updateMethod("   ") // blank → falls back to "*"
        vm.updateUrlMode(UrlMatchMode.Exact)
        vm.updateUrlPattern("  https://x  ")
        vm.updateBodyMode(BodyMatchMode.Contains)
        vm.updateBodyPattern("  payload  ")

        vm.saveRule { }
        advanceUntilIdle()

        verifySuspend {
            repo.addRule(
                matches<WiretapRule> {
                    it.method == "*" &&
                        (it.urlMatcher as UrlMatcher.Exact).pattern == "https://x" &&
                        (it.bodyMatcher as BodyMatcher.Contains).pattern == "payload"
                },
            )
        }
    }

    @Test
    fun `buildRuleFromForm filters out header entries with blank keys`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        stubConflicts(emptyList())

        val vm = newVm()
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/api")
        vm.addHeader()
        vm.updateHeader(0, HeaderEntry(key = "  ", value = "ignored", mode = HeaderEntryMode.ValueExact))
        vm.addHeader()
        vm.updateHeader(1, HeaderEntry(key = "Authorization", value = "abc", mode = HeaderEntryMode.ValueExact))

        vm.saveRule { }
        advanceUntilIdle()

        verifySuspend {
            repo.addRule(
                matches<WiretapRule> {
                    it.headerMatchers.size == 1 &&
                        (it.headerMatchers[0] as HeaderMatcher.ValueExact).key == "Authorization"
                },
            )
        }
    }

    @Test
    fun `buildRuleFromForm applies Throttle action with default delay 1000 when missing`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        stubConflicts(emptyList())

        val vm = newVm()
        vm.updateUrlMode(UrlMatchMode.Contains)
        vm.updateUrlPattern("/api")
        vm.updateAction(RuleAction.Throttle())
        // throttleDelayMs left empty string

        vm.saveRule { }
        advanceUntilIdle()

        verifySuspend {
            repo.addRule(
                matches<WiretapRule> {
                    val a = it.action as? RuleAction.Throttle
                    a != null && a.delayMs == 1000L
                },
            )
        }
    }

    // ---- prefill from log -------------------------------------------------

    @Test
    fun `init prefills request state from the linked HttpLog`() = runTest {
        everySuspend { repo.getById(any()) } returns null
        everySuspend { httpLogManager.getHttpLogById(42) } returns HttpLog(
            id = 42,
            url = "https://api.example.com/users",
            method = "POST",
            requestHeaders = mapOf("Authorization" to "Bearer abc"),
            requestBody = "payload",
            timestamp = 0,
        )

        val vm = newVm(
            existingRuleId = 0,
            prefillConfig = PrefillConfig(
                logId = 42,
                includeUrl = true,
                includeHeaders = true,
                includeBody = true,
                selectedHeaderKeys = "Authorization",
            ),
        )
        advanceUntilIdle()

        vm.loaded.value shouldBe true
        val req = vm.requestState.value
        req.method shouldBe "POST"
        req.urlMode shouldBe UrlMatchMode.Exact
        req.urlPattern shouldBe "https://api.example.com/users"
        req.headerEntries.size shouldBe 1
        req.headerEntries[0].key shouldBe "Authorization"
        req.bodyMode shouldBe BodyMatchMode.Exact
        req.bodyPattern shouldBe "payload"
    }

    // ---- init from existing rule with MockAndThrottle (covers ThrottleInputMode resolution) --

    @Test
    fun `init from existing MockAndThrottle rule classifies throttle as Profile when matches`() =
        runTest {
            val profile = ThrottleProfile.Slow3g
            val existing = WiretapRule(
                id = 9,
                method = "GET",
                action = RuleAction.MockAndThrottle(
                    responseCode = 503,
                    delayMs = profile.delayMinMs,
                    delayMaxMs = profile.delayMaxMs,
                ),
                createdAt = 1L,
            )
            everySuspend { repo.getById(9) } returns existing

            val vm = newVm(existingRuleId = 9)
            advanceUntilIdle()

            val res = vm.responseState.value
            res.action.shouldBeInstanceOf<RuleAction.MockAndThrottle>()
            res.throttleInputMode shouldBe ThrottleInputMode.Profile
            res.mockResponseCode shouldBe "503"
        }

    @Test
    fun `init from existing pure-Mock rule classifies throttle as None`() = runTest {
        val existing = WiretapRule(
            id = 9,
            method = "GET",
            action = RuleAction.Mock(responseCode = 200),
            createdAt = 1L,
        )
        everySuspend { repo.getById(9) } returns existing

        val vm = newVm(existingRuleId = 9)
        advanceUntilIdle()

        vm.responseState.value.throttleInputMode shouldBe ThrottleInputMode.None
    }
}
