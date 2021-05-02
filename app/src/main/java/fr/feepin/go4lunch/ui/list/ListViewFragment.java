package fr.feepin.go4lunch.ui.list;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

import fr.feepin.go4lunch.MainViewModel;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.data.Resource;
import fr.feepin.go4lunch.databinding.FragmentListViewBinding;

public class ListViewFragment extends Fragment {

    public static final String TAG = "LIST_VIEW_TAG";

    private FragmentListViewBinding binding;

    private MainViewModel mainViewModel;

    private ListItemAdapter listItemAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentListViewBinding.inflate(inflater);
        setHasOptionsMenu(true);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listItemAdapter = new ListItemAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        binding.rvRestaurants.setAdapter(listItemAdapter);
        binding.rvRestaurants.setLayoutManager(linearLayoutManager);

        mainViewModel.getListViewState().observe(getViewLifecycleOwner(), listViewState -> {
            if (listViewState instanceof Resource.Success) {
                binding.progressBar.hide();
                listItemAdapter.submitList(listViewState.getData().getListItemStates());
            } else if (listViewState instanceof Resource.Loading) {
                binding.progressBar.show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.list_view_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) searchItem.getActionView();
        EditText editText = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        editText.setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.sort) {

//            final SortMethod[] sortMethod = {SortMethod.NONE};
//
//            new MaterialAlertDialogBuilder(requireContext())
//                    .setTitle(R.string.title_sort_by)
//                    .setSingleChoiceItems(R.array.array_sorting_methods, -1, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            switch(which) {
//                                case 0:
//                                    sortMethod[0] = SortMethod.DISTANCE;
//                                    break;
//                                case 1:
//                                    break;
//                                case 2:
//                                    break;
//                            }
//                        }
//                    })
//                    .setPositiveButton(R.string.text_sort, (dialog, which) -> {
//
//                    })
//                    .create()
//                    .show();
        }

        return true;
    }
}
