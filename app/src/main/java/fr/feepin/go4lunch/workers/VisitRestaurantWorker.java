package fr.feepin.go4lunch.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.data.repos.data.RestaurantRepository;

/**
 * A worker that add the restaurantId (stored in inputData) to user's visited_restaurants collection*
 */
@HiltWorker
public class VisitRestaurantWorker extends Worker {

    private final RestaurantRepository restaurantRepository;
    private final WorkManager workManager;

    @AssistedInject
    public VisitRestaurantWorker(
            @Assisted @NonNull @NotNull Context context,
            @Assisted @NonNull @NotNull WorkerParameters workerParams,
            RestaurantRepository restaurantRepository
    ) {
        super(context, workerParams);
        this.restaurantRepository = restaurantRepository;
        this.workManager = WorkManager.getInstance(context);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {

        String restaurantId = getInputData().getString(Constants.KEY_RESTAURANT_ID);
        restaurantRepository.addVisitedRestaurant(restaurantId).blockingAwait();
        restaurantRepository.leaveRestaurant().blockingAwait();

        workManager.cancelUniqueWork(Constants.NOTIFY_WORKER_TAG);

        return Result.success();
    }
}
