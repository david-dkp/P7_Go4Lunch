package fr.feepin.go4lunch.others;

import io.reactivex.rxjava3.core.Scheduler;

public interface SchedulerProvider {
    Scheduler io();

    Scheduler ui();
}
