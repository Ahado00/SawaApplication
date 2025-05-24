package com.example.sawaapplication.screens.communities.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.sawaapplication.R
import com.example.sawaapplication.screens.communities.presentation.screens.component.ExploreCommunityCardList
import com.example.sawaapplication.screens.communities.presentation.vmModels.CommunityFilterType
import com.example.sawaapplication.screens.communities.presentation.vmModels.ExploreCommunityViewModel
import com.example.sawaapplication.ui.screenComponent.CustomConfirmationDialog
import com.example.sawaapplication.ui.screenComponent.SearchField


@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreCommunityViewModel = hiltViewModel()
) {

    val searchText = viewModel.searchText
    val filteredList = viewModel.filteredCommunities
    var isFilterMenuExpanded by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    var scrollToTopTrigger by remember { mutableStateOf(0) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }

    val categoryStrings = listOf(
        R.string.artCreativity,
        R.string.booksLiterature,
        R.string.funny,
        R.string.gaming,
        R.string.healthWellness,
        R.string.moviesTVShows,
        R.string.petsAnimals,
        R.string.sports,
        R.string.techGadgets,
        R.string.travelAdventure,
        R.string.other
    ).map { stringResource(it) }

    val allFilters = listOf(
        CommunityFilterType.DEFAULT,
        CommunityFilterType.MOST_POPULAR,
        CommunityFilterType.MOST_RECENT
    ) + categoryStrings.map { CommunityFilterType.Category(it) }

    // Scroll to top when scrollToTopTrigger changes
    LaunchedEffect(scrollToTopTrigger) {
        listState.animateScrollToItem(0)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchField(
                value = searchText,
                onValueChange = viewModel::onSearchTextChange,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(onClick = { isFilterMenuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter"
                    )
                }

                DropdownMenu(
                    expanded = isFilterMenuExpanded,
                    onDismissRequest = { isFilterMenuExpanded = false },

                    // to control visible items number
                    modifier = Modifier.heightIn(max = (4 * integerResource(R.integer.itemHeight)).dp)

                ) {
                    allFilters.forEach { filter ->
                        val label = when (filter) {
                            is CommunityFilterType.DEFAULT -> "Default"
                            is CommunityFilterType.MOST_POPULAR -> "Most Popular"
                            is CommunityFilterType.MOST_RECENT -> "Most Recent"
                            is CommunityFilterType.Category -> filter.categoryName
                        }

                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                viewModel.selectedFilter = filter
                                scrollToTopTrigger++
                                isFilterMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        ExploreCommunityCardList(
            communities = filteredList,
            currentUserId = viewModel.currentUserId,
            listState = listState,
            onCommunityClick = { communityId ->
                navController.navigate("community_screen/$communityId")
            },
            onJoinClick = { communityId ->
                viewModel.joinCommunity(communityId, viewModel.currentUserId)
            },
            onLeaveClick = { communityId ->
                selectedCommunityId = communityId
                showConfirmationDialog = true
            }
        )

        if (showConfirmationDialog && selectedCommunityId != null) {
            CustomConfirmationDialog(
                message = stringResource(R.string.areYouSureCommunity),
                onConfirm = {
                    viewModel.leaveCommunity(selectedCommunityId!!, viewModel.currentUserId)
                    showConfirmationDialog = false
                    selectedCommunityId = null
                },
                onDismiss = {
                    showConfirmationDialog = false
                    selectedCommunityId = null
                }
            )
        }
    }
}