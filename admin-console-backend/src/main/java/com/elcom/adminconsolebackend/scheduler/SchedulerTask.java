package com.elcom.adminconsolebackend.scheduler;

import com.elcom.adminconsolebackend.service.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SchedulerTask {

    private final MonitorService monitorService;

    @Async
    @Scheduled(cron = "0 */3 * * * *")
  //@Scheduled(initialDelay = 3000, fixedDelayString = "10000")
    public void saveWarningMonitor() {
        this.monitorService.saveWarningMonitor();
    }




}
