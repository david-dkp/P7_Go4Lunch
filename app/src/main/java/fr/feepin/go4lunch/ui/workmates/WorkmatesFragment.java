package fr.feepin.go4lunch.ui.workmates;

import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import fr.feepin.go4lunch.MainViewModel;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.databinding.FragmentWorkmatesBinding;
import fr.feepin.go4lunch.ui.restaurant.RestaurantActivity;

public class WorkmatesFragment extends Fragment {

    public static final String TAG = "WORKMATES_TAG";

    private FragmentWorkmatesBinding binding;

    private WorkmatesAdapter workmatesAdapter;

    private WorkmatesViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkmatesBinding.inflate(inflater);
        setHasOptionsMenu(true);

        viewModel = new ViewModelProvider(requireActivity()).get(WorkmatesViewModel.class);

        setupAdapter();
        setupObservers();

        return binding.getRoot();
    }

    private void setupAdapter() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);

        workmatesAdapter = new WorkmatesAdapter(workmateState -> {
            if (!workmateState.getRestaurantId().equals("")) {
                RestaurantActivity.navigate(getContext(), workmateState.getRestaurantId());
            }
        });

        binding.rvWorkmates.setAdapter(workmatesAdapter);
        binding.rvWorkmates.setLayoutManager(linearLayoutManager);

        int leftOffset = requireContext().getResources().getDimensionPixelSize(R.dimen.item_workmate_height);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(new InsetDrawable(dividerItemDecoration.getDrawable(), leftOffset, 0, 0, 0));
        binding.rvWorkmates.addItemDecoration(dividerItemDecoration);
    }

    private void setupObservers() {
        viewModel.getWorkmateStates().observe(getViewLifecycleOwner(), states -> {
            workmatesAdapter.submitList(states.getData());
        });
    }
}
