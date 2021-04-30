package fr.feepin.go4lunch.ui.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
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

    private MainViewModel mainViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWorkmatesBinding.inflate(inflater);
        setHasOptionsMenu(true);

        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupAdapter();
        setupObservers();
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

        int leftOffset = getContext().getResources().getDimensionPixelSize(R.dimen.item_workmate_height);

        binding.rvWorkmates.addItemDecoration(new WorkmatesItemDecoration(getContext(), linearLayoutManager.getOrientation(), leftOffset));
    }

    private void setupObservers() {
        mainViewModel.getWorkmateStates().observe(getViewLifecycleOwner(), states -> {
            workmatesAdapter.submitList(states.getData());
        });
    }
}
