package com.yidont.unimp.modules.demo.ui.main

import androidx.compose.runtime.Composable
import com.yidont.unimp.modules.demo.LoadingBox
import com.yidont.unimp.modules.demo.MainViewModel
import com.yidont.unimp.modules.demo.ui.theme.AppTheme


@Composable
fun AppPage(viewModel: MainViewModel) {
    AppTheme {
        val state = viewModel.state
        MainPage(viewModel)
        if (state.loading) {
            LoadingBox(msg = state.loadText)
        }
    }
}