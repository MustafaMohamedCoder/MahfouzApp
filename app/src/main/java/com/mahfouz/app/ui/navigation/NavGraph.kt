package com.mahfouz.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mahfouz.app.data.repository.InvoiceRepository
import com.mahfouz.app.ui.screens.add_edit.AddEditInvoiceScreen
import com.mahfouz.app.ui.screens.add_edit.AddEditInvoiceViewModel
import com.mahfouz.app.ui.screens.add_edit.AddEditInvoiceViewModelFactory
import com.mahfouz.app.ui.screens.detail.InvoiceDetailScreen
import com.mahfouz.app.ui.screens.groups.GroupsScreen
import com.mahfouz.app.ui.screens.groups.GroupsViewModel
import com.mahfouz.app.ui.screens.groups.GroupsViewModelFactory
import com.mahfouz.app.ui.screens.invoices.InvoicesScreen
import com.mahfouz.app.ui.screens.invoices.InvoicesViewModel
import com.mahfouz.app.ui.screens.invoices.InvoicesViewModelFactory

@Composable
fun MahfouzNavGraph(
    navController: NavHostController,
    repository: InvoiceRepository
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Groups.route
    ) {
        // 1. Groups Screen
        composable(Screen.Groups.route) {
            val viewModel: GroupsViewModel = viewModel(
                factory = GroupsViewModelFactory(repository)
            )
            GroupsScreen(
                viewModel = viewModel,
                onCategoryClick = { categoryId, categoryName ->
                    navController.navigate(Screen.Invoices.createRoute(categoryId, categoryName))
                }
            )
        }

        // 2. Invoices List for a Category
        composable(
            route = Screen.Invoices.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""

            val viewModel: InvoicesViewModel = viewModel(
                factory = InvoicesViewModelFactory(repository, categoryId)
            )

            InvoicesScreen(
                categoryName = categoryName,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onAddInvoiceClick = {
                    navController.navigate(Screen.AddEditInvoice.createRoute(categoryId))
                },
                onInvoiceClick = { invoiceId ->
                    navController.navigate(Screen.InvoiceDetail.createRoute(invoiceId))
                }
            )
        }

        // 3. Add / Edit Invoice
        composable(
            route = Screen.AddEditInvoice.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType },
                navArgument("invoiceId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: 0L
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: -1L

            val viewModel: AddEditInvoiceViewModel = viewModel(
                factory = AddEditInvoiceViewModelFactory(repository, categoryId, invoiceId)
            )

            AddEditInvoiceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 4. Invoice Detail
        composable(
            route = Screen.InvoiceDetail.route,
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getLong("invoiceId") ?: 0L

            InvoiceDetailScreen(
                invoiceId = invoiceId,
                repository = repository,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
