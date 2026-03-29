/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.wiretap.di

import dev.skymansandy.wiretap.ui.screens.home.WiretapHomeViewModel
import dev.skymansandy.wiretap.ui.screens.http.detail.HttpLogDetailViewModel
import dev.skymansandy.wiretap.ui.screens.http.list.HttpLogListViewModel
import dev.skymansandy.wiretap.ui.screens.rules.create.CreateRuleViewModel
import dev.skymansandy.wiretap.ui.screens.rules.list.RulesListViewModel
import dev.skymansandy.wiretap.ui.screens.rules.view.RuleDetailViewModel
import dev.skymansandy.wiretap.ui.screens.socket.detail.SocketDetailViewModel
import dev.skymansandy.wiretap.ui.screens.socket.list.SocketLogListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val wiretapViewModelModule = module {

    viewModel<WiretapHomeViewModel> { WiretapHomeViewModel() }

    viewModel<HttpLogListViewModel> {
        HttpLogListViewModel(
            httpLogManager = get(),
        )
    }

    viewModel<HttpLogDetailViewModel> { (logId: Long) ->
        HttpLogDetailViewModel(
            logId = logId,
            httpLogManager = get(),
        )
    }

    viewModel<SocketLogListViewModel> {
        SocketLogListViewModel(
            socketLogManager = get(),
        )
    }

    viewModel<SocketDetailViewModel> { (socketId: Long) ->
        SocketDetailViewModel(
            socketId = socketId,
            socketLogManager = get(),
        )
    }

    viewModel<RulesListViewModel> {
        RulesListViewModel(
            ruleRepository = get(),
        )
    }

    viewModel<CreateRuleViewModel> { (existingRuleId: Long, prefillFromLogId: Long) ->
        CreateRuleViewModel(
            httpLogManager = get(),
            findConflictingRules = get(),
            existingRuleId = existingRuleId,
            prefillFromLogId = prefillFromLogId,
            ruleRepository = get(),
        )
    }

    viewModel<RuleDetailViewModel> { (ruleId: Long) ->
        RuleDetailViewModel(
            ruleId = ruleId,
            ruleRepository = get(),
        )
    }
}
