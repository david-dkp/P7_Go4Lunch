package fr.feepin.go4lunch.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.user.UserRepository;

/**
 * A worker that add the restaurantId (stored in inputData) to user's visited_restaurants collection*
 */
@HiltWorker
public class VisitRestaurantWorker extends Worker {

    private final UserRepository userRepository;

    @AssistedInject
    public VisitRestaurantWorker(
            @Assisted @NonNull @NotNull Context context,
            @Assisted @NonNull @NotNull WorkerParameters workerParams,
            UserRepository userRepository
    ) {
        super(context, workerParams);
        this.userRepository = userRepository;
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {

        String restaurantId = getInputData().getString(Constants.KEY_RESTAURANT_ID);
        userRepository.addRestaurantToVisited(restaurantId).blockingAwait();
        userRepository.leaveRestaurant().blockingAwait();

        return Result.success();
    }
}
