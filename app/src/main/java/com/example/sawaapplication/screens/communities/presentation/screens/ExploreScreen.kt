package com.example.sawaapplication.screens.communities.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
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
    val listState = rememberLazyListState()
    var scrollToTopTrigger by remember { mutableStateOf(0) }
    var selectedCommunityId by remember { mutableStateOf<String?>(null) }
    var showConfirmationDialog by remember { mutableStateOf(false) }

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

    // Scroll to top when filter changes
    LaunchedEffect(scrollToTopTrigger) {
        listState.animateScrollToItem(0)
    }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(integerResource(R.integer.paddingEventScreen).dp)
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchField(
                    value = searchText,
                    onValueChange = viewModel::onSearchTextChange,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            // Filter pills (Chips)
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allFilters.size) { index ->
                    val filter = allFilters[index]
                    val label = when (filter) {
                        is CommunityFilterType.DEFAULT -> "Default"
                        is CommunityFilterType.MOST_POPULAR -> "Popular"
                        is CommunityFilterType.MOST_RECENT -> "Recent"
                        is CommunityFilterType.Category -> filter.categoryName
                    }

                    val selected = viewModel.selectedFilter == filter

                    FilterChip(
                        selected = selected,
                        onClick = {
                            viewModel.selectedFilter = filter
                            scrollToTopTrigger++
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = MaterialTheme.colorScheme.onTertiary,
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            selectedLabelColor = White,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = if (selected)
                                Color.Transparent
                            else
                                MaterialTheme.colorScheme.onTertiary,
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Community List
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


