package fr.feepin.go4lunch.ui.list;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dagger.hilt.android.AndroidEntryPoint;
import fr.feepin.go4lunch.MainViewModel;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.databinding.FragmentListViewBinding;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;

@AndroidEntryPoint
public class ListViewFragment extends Fragment {

    public static final String TAG = "LIST_VIEW_TAG";

    private FragmentListViewBinding binding;

    private ListViewViewModel viewModel;

    private ListItemAdapter listItemAdapter;

    private MenuItem sortMenuItem;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListViewBinding.inflate(inflater);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(this).get(ListViewViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listItemAdapter = new ListItemAdapter(listItemState -> {
            RestaurantActivity.navigate(getContext(), listItemState.getId(), viewModel.getSessionToken());
            viewModel.destroyAutocompleteSession();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        binding.rvRestaurants.setAdapter(listItemAdapter);
        binding.rvRestaurants.setLayoutManager(linearLayoutManager);

        viewModel.getListViewState().observe(getViewLifecycleOwner(), listViewState -> {
            if (listViewState instanceof Resource.Success) {
                binding.swipeRefreshLayout.setRefreshing(false);
                binding.progressBar.hide();
                listItemAdapter.submitList(listViewState.getData().getListItemStates());
                listItemAdapter.notifyDataSetChanged();

                if (listViewState.getData().isScrollToFirst()) {
                    binding.rvRestaurants.smoothScrollToPosition(0);
                }

            } else if (listViewState instanceof Resource.Loading) {
                binding.progressBar.show();
            }

            if (sortMenuItem != null) {
                sortMenuItem.setEnabled(listViewState.getData().isSortable());
                sortMenuItem.setVisible(listViewState.getData().isSortable());
            }

        });

        //Refreshing
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.onRefresh();
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_view_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        sortMenuItem = menu.findItem(R.id.sort);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.onQuery(newText);
                return true;
            }
        });
        EditText editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sort) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.title_sort_by)
                    .setSingleChoiceItems(R.array.array_sorting_methods, viewModel.getSortMethod().getValue().getPosition(), (dialog, which) -> {
                        switch (which) {
                            case 0:
                                viewModel.setSortMethod(ListItemStateSortMethod.DISTANCE);
                                break;
                            case 1:
                                viewModel.setSortMethod(ListItemStateSortMethod.RATING);
                                break;
                            case 2:
                                viewModel.setSortMethod(ListItemStateSortMethod.WORKMATES);
                                break;
                        }
                        dialog.dismiss();
                    })
                    .create()
                    .show();
        }

        return true;
    }
}
