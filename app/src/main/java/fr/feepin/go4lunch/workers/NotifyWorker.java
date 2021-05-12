package fr.feepin.go4lunch.workers;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.hilt.work.HiltWorker;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import fr.feepin.go4lunch.Constants;
import fr.feepin.go4lunch.R;
import fr.feepin.go4lunch.data.maps.MapsRepository;
import fr.feepin.go4lunch.data.user.UserRepository;
import fr.feepin.go4lunch.data.user.models.UserInfo;
import fr.feepin.go4lunch.utils.ConnectivityUtils;
import io.reactivex.rxjava3.core.Observable;

import static fr.feepin.go4lunch.Constants.EAT_NOTIFICATION_ID;

/**
 * A Worker that notify the user on where he eats and with who
 */

@HiltWorker
public class NotifyWorker extends Worker {

    private final MapsRepository mapsRepository;
    private final UserRepository userRepository;
    private final Context context;

    @AssistedInject
    public NotifyWorker(
            @Assisted @NonNull @NotNull Context appContext,
            @Assisted @NonNull @NotNull WorkerParameters workerParams,
            MapsRepository mapsRepository,
            UserRepository userRepository
    ) {
        super(appContext, workerParams);
        this.context = appContext;
        this.mapsRepository = mapsRepository;
        this.userRepository = userRepository;
    }

    @NonNull
    @Override
    public Result doWork() {

        boolean isNotificationEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("notifications", true);

        if (!isNotificationEnabled) return Result.failure();

        String restaurantId = getInputData().getString(Constants.KEY_RESTAURANT_ID);
        String restaurantName = getInputData().getString(Constants.KEY_RESTAURANT_NAME);
        String restaurantAddress = getInputData().getString(Constants.KEY_RESTAURANT_ADDRESS);

        if (ConnectivityUtils.hasInternetConnection(context)) {
            return userRepository.getUsersInfo()
                    .flatMapObservable(Observable::fromIterable)
                    .filter(userInfo -> userInfo.getRestaurantChoiceId().equals(restaurantId))
                    .map(UserInfo::getName)
                    .toList()
                    .doOnSuccess(names -> {
                        notifyUser(restaurantName, restaurantAddress, names);
                    })
                    .doOnError(throwable -> {
                        throwable.printStackTrace();
                    })
                    .map(strings -> Result.success()).blockingGet();
        } else {
            notifyUser(restaurantName, restaurantAddress);
            return Result.success();
        }
    }

    private void notifyUser(String restaurantName, String restaurantAddress, List<String> workmatesJoining) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        String andWord = context.getResources().getString(R.string.text_and);

        String contentText;

        if (workmatesJoining.size() >= 2) {
            contentText = context.getString(R.string.eat_notification_context_text, restaurantName, restaurantAddress) + " "
                    + TextUtils.join(", ", workmatesJoining.subList(0, workmatesJoining.size() - 1)) + " " + andWord + " "
                    + workmatesJoining.get(workmatesJoining.size() - 1) + ".";
        } else if (workmatesJoining.size() == 1) {
            contentText = context.getString(R.string.eat_notification_context_text, restaurantName, restaurantAddress) + " "
                    + workmatesJoining.get(0) + ".";
        } else {
            contentText = context.getString(R.string.eat_notification_context_text_no_workmates, restaurantName, restaurantAddress);
        }

        Notification.Builder builder = new Notification.Builder(context)
                .setStyle(new Notification.BigTextStyle())
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_cutlery);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(EAT_NOTIFICATION_ID);
        }

        notificationManager.notify(1, builder.build());
    }

    private void notifyUser(String restaurantName, String restaurantAddress) {
        notifyUser(restaurantName, restaurantAddress, Collections.emptyList());
    }
}
